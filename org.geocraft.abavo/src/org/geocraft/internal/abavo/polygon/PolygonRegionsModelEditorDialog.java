/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.abavo.polygon;


import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.geocraft.abavo.polygon.PolygonModel;
import org.geocraft.abavo.polygon.PolygonRegionsModel;
import org.geocraft.abavo.polygon.PolygonRegionsModelEvent;
import org.geocraft.abavo.polygon.PolygonRegionsModelListener;
import org.geocraft.abavo.polygon.PolygonRegionsModelEvent.Type;
import org.geocraft.core.model.IModel;
import org.geocraft.core.model.IModelListener;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.ModelDialog;
import org.geocraft.ui.form2.field.CheckboxField;
import org.geocraft.ui.form2.field.TextField;


/**
 * This class is a dialog for editing an polygon regions model.
 * The dialog is a simple form dialog that contains a
 * composite for editing the polygon regions model. The
 * dialog also has 2 custom buttons, one for loading an
 * polygon regions model from disk, and one for saving
 * the current polygon regions model to disk.
 * 
 * The editor consists of 3 sections...
 * 1) General - contains the region symmetry and polygon fill properties.
 * 3) Normalization - contains the normalization factory property.
 * 2) Polygons - contains a table showing properties of the polygons.
 * Buttons are provided for creating polygons in the crossplot and
 * also for deleting polygons from the crossplot.
 */
public class PolygonRegionsModelEditorDialog extends ModelDialog implements PolygonRegionsModelListener {

  /** The polygon regions model to edit. */
  private final PolygonRegionsModel _model;

  /** The array of custom buttons for polygon visibility. */
  private Button[] _visibleButtons;

  /** The array of custom text fields for polygon names. */
  private Text[] _nameTexts;

  /** The array of custom labels for polygon values. */
  private Label[] _valueLabels;

  /** The array of custom labels for polygon colors. */
  private Label[] _colorLabels;

  /**
   * The private constructor. This dialog must be created using the static convenience method.
   * 
   * @param shell the parent shell.
   * @param model the polygon regions model to edit.
   */
  private PolygonRegionsModelEditorDialog(final Shell shell, final PolygonRegionsModel model) {
    super(shell, "Polygon RegionsModel");
    model.addPolygonModelListener(this);
    _model = model;
  }

  @Override
  protected void applySettings() {
    // TODO Auto-generated method stub

  }

  @Override
  protected int getNumForms() {
    return 1;
  }

  @Override
  protected void buildModelForms(IModelForm[] forms) {
    IModelForm form = forms[0];

    // Add the general section.
    FormSection generalSection = form.addSection("General", false);

    CheckboxField symmetryLock = generalSection.addCheckboxField(PolygonRegionsModel.SYMMETRIC_REGIONS);
    symmetryLock.setLabel("Symmetry Lock on Polygon Pairs");
    symmetryLock.setTooltip("Enforces symmetry between opposite polygons (e.g. #1 and #64, #2 and #63, etc)");

    CheckboxField fillStyle = generalSection.addCheckboxField(PolygonRegionsModel.POLYGONS_FILLED);
    fillStyle.setLabel("Fill Polygons");
    fillStyle.setTooltip("Toggles the crossplot polygons between filled and un-filled");

    // Add the normalization section.
    FormSection normalizationSection = form.addSection("Normalization", false);

    TextField normalizationFactor = normalizationSection.addTextField(PolygonRegionsModel.NORMALIZATION_FACTOR);
    normalizationFactor.setLabel("Normalization Factor");
    normalizationFactor.setTooltip("The normalization factor to apply to the polygon class values");

    // Add the polygons section.
    FormSection polygonsSection = form.addSection("Polygons", false);

    // Create a custom composite.
    final Composite client = polygonsSection.getComposite();
    GridLayout gridLayout = new GridLayout();
    gridLayout.makeColumnsEqualWidth = false;
    gridLayout.horizontalSpacing = 1;
    gridLayout.numColumns = 3;
    client.setLayout(gridLayout);

    // Create a scrolled composite to contain the
    // table-like arrangement of controls.
    ScrolledComposite scroll = new ScrolledComposite(client, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
    scroll.setAlwaysShowScrollBars(true);

    Composite composite = new Composite(scroll, SWT.BORDER);
    gridLayout = new GridLayout();
    gridLayout.makeColumnsEqualWidth = true;
    gridLayout.horizontalSpacing = 2;
    gridLayout.verticalSpacing = 1;
    gridLayout.numColumns = 6;
    composite.setLayout(gridLayout);
    form.getToolkit().adapt(composite);

    // Create the controls for polygon visibility, value and color.
    // Also create 2 button controls for polygon creation/deletion.
    PolygonModel[] polygonModels = _model.getPolygonModels();
    PolygonModel[] polygonModelsRev = new PolygonModel[PolygonRegionsModel.NUMBER_OF_POLYGONS];
    for (int i = 0; i < PolygonRegionsModel.NUMBER_OF_POLYGONS / 2; i++) {
      int j = PolygonRegionsModel.NUMBER_OF_POLYGONS - 1 - i;
      polygonModelsRev[i] = polygonModels[j];
      polygonModelsRev[j] = polygonModels[i];
    }
    _visibleButtons = new Button[PolygonRegionsModel.NUMBER_OF_POLYGONS];
    _nameTexts = new Text[PolygonRegionsModel.NUMBER_OF_POLYGONS];
    _valueLabels = new Label[PolygonRegionsModel.NUMBER_OF_POLYGONS];
    _colorLabels = new Label[PolygonRegionsModel.NUMBER_OF_POLYGONS];
    for (int i = 0; i < PolygonRegionsModel.NUMBER_OF_POLYGONS; i++) {
      final PolygonModel polygonModel = polygonModelsRev[i];
      int index = PolygonRegionsModel.NUMBER_OF_POLYGONS - 1 - i;

      _visibleButtons[index] = createVisibilityButton(composite, polygonModel);

      _nameTexts[index] = createNameTextField(composite, polygonModel);

      _valueLabels[index] = createValueLabel(composite, polygonModel);

      _colorLabels[index] = createColorLabel(composite, polygonModel);

      final Button createButton = new Button(composite, SWT.PUSH);
      createButton.setText("Create");
      createButton.setToolTipText("Create polygon #" + polygonModel.getId() + " in crossplot");
      createButton.setLayoutData(createGridData());
      createButton.setEnabled(!polygonModel.getExists());
      final Listener createListener = new Listener() {

        @SuppressWarnings("unused")
        public void handleEvent(final Event event) {
          polygonModel.setExists(true);
        }
      };
      createButton.addListener(SWT.Selection, createListener);
      createButton.addDisposeListener(new DisposeListener() {

        @SuppressWarnings("unused")
        public void widgetDisposed(final DisposeEvent event) {
          createButton.removeListener(SWT.Selection, createListener);
        }

      });

      final Button deleteButton = new Button(composite, SWT.PUSH);
      deleteButton.setText("Delete");
      deleteButton.setToolTipText("Delete polygon #" + polygonModel.getId() + " from crossplot");
      deleteButton.setEnabled(polygonModel.getExists());
      deleteButton.setLayoutData(createGridData());
      final Listener deleteListener = new Listener() {

        @SuppressWarnings("unused")
        public void handleEvent(final Event event) {
          polygonModel.setExists(false);
        }
      };
      deleteButton.addListener(SWT.Selection, deleteListener);
      deleteButton.addDisposeListener(new DisposeListener() {

        @SuppressWarnings("unused")
        public void widgetDisposed(final DisposeEvent event) {
          deleteButton.removeListener(SWT.Selection, deleteListener);
        }

      });

      final IModelListener propListener = new IModelListener() {

        public void propertyChanged(final String key) {
          if (key.equals(PolygonModel.EXISTS)) {
            createButton.setEnabled(!polygonModel.getExists());
            deleteButton.setEnabled(polygonModel.getExists());
          }
        }
      };

      polygonModel.addListener(propListener);
      createButton.addDisposeListener(new DisposeListener() {

        @SuppressWarnings("unused")
        public void widgetDisposed(final DisposeEvent event) {
          polygonModel.removeListener(propListener);
        }
      });
    }
    scroll.setMinSize(600, 600);
    scroll.setExpandHorizontal(true);
    scroll.setContent(composite);

    GridData gridData = new GridData();
    gridData.grabExcessHorizontalSpace = true;
    gridData.horizontalAlignment = SWT.FILL;
    gridData.horizontalSpan = 4;
    gridData.grabExcessVerticalSpace = false;
    gridData.widthHint = 600;
    gridData.heightHint = 400;
    scroll.setLayoutData(gridData);
    composite.setSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
  }

  @Override
  protected IModel createModel() {
    return _model;
  }

  @Override
  public void createButtonsForButtonBar(final Composite parent) {
    // Create the button for loading an polygon regions model.
    final LoadPolygonRegionsModel loadAction = new LoadPolygonRegionsModel(parent, _model);

    // Create the button for loading an polygon regions model.
    // TODO: For now, just use the BACK_ID...
    Button loadButton = createButton(parent, IDialogConstants.BACK_ID, "Load...", false);
    loadButton.setImage(loadAction.getImageDescriptor().createImage());
    loadButton.addListener(SWT.Selection, new Listener() {

      @Override
      @SuppressWarnings("unused")
      public void handleEvent(final Event event) {
        loadAction.run();
      }

    });

    // Create the action for saving an polygon regions model.
    final SavePolygonRegionsModel saveAction = new SavePolygonRegionsModel(parent, _model);

    // Create the button for saving an polygon regions model.
    // TODO: For now, just use the NEXT_ID...
    Button saveButton = createButton(parent, IDialogConstants.NEXT_ID, "Save...", false);
    saveButton.setImage(saveAction.getImageDescriptor().createImage());
    saveButton.addListener(SWT.Selection, new Listener() {

      @Override
      @SuppressWarnings("unused")
      public void handleEvent(final Event event) {
        saveAction.run();
      }

    });

    createButton(parent, IDialogConstants.CANCEL_ID, "Close", false);
  }

  @Override
  public boolean close() {
    _model.removePolygonModelListener(this);
    return super.close();
  }

  /**
   * Creates a toggle button for the polygon visibility.
   * @param composite the parent composite.
   * @param polygonModel the associated polygon model.
   */
  private Button createVisibilityButton(final Composite composite, final PolygonModel polygonModel) {
    final Button button = new Button(composite, SWT.CHECK);
    button.setText("Polygon #" + polygonModel.getId());
    button.setToolTipText("Toggle visibility for polygon #" + polygonModel.getId());
    button.setSelection(polygonModel.getVisible());
    button.setLayoutData(createGridData());
    button.setBackground(composite.getBackground());
    final Listener selectionListener = new Listener() {

      @SuppressWarnings("unused")
      public void handleEvent(final Event event) {
        polygonModel.setVisible(button.getSelection());
      }
    };
    button.addListener(SWT.Selection, selectionListener);
    button.addDisposeListener(new DisposeListener() {

      @SuppressWarnings("unused")
      public void widgetDisposed(final DisposeEvent event) {
        button.removeListener(SWT.Selection, selectionListener);
      }
    });
    return button;
  }

  /**
   * Creates a text field for the polygon name.
   * @param composite the parent composite.
   * @param polygonModel the associated polygon model.
   */
  private Text createNameTextField(final Composite composite, final PolygonModel polygonModel) {
    final Text text = new Text(composite, SWT.BORDER);
    text.setText(polygonModel.getText());
    text.setToolTipText("Set the class name for polygon #" + polygonModel.getId());
    text.setLayoutData(createGridData());
    final KeyListener keyListener = new KeyListener() {

      @SuppressWarnings("unused")
      public void keyPressed(final KeyEvent event) {
        // Action is on key released, not pressed.
      }

      @SuppressWarnings("unused")
      public void keyReleased(final KeyEvent event) {
        polygonModel.setText(text.getText());
      }
    };
    text.addKeyListener(keyListener);
    text.addDisposeListener(new DisposeListener() {

      @SuppressWarnings("unused")
      public void widgetDisposed(final DisposeEvent event) {
        text.removeKeyListener(keyListener);
      }
    });
    return text;
  }

  /**
   * Creates a label for the polygon value.
   * @param composite the parent composite.
   * @param polygonModel the associated polygon model.
   */
  private Label createValueLabel(final Composite composite, final PolygonModel polygonModel) {
    final Label label = new Label(composite, SWT.BORDER);
    label.setText(Float.toString(polygonModel.getValue()));
    label.setAlignment(SWT.CENTER);
    //text.setEditable(false);
    label.setToolTipText("The class value for polygon #" + polygonModel.getId());
    label.setLayoutData(createGridData());
    final KeyListener keyListener = new KeyListener() {

      @SuppressWarnings("unused")
      public void keyPressed(final KeyEvent event) {
        // Action is on key released, not pressed.
      }

      @SuppressWarnings("unused")
      public void keyReleased(final KeyEvent event) {
        try {
          polygonModel.setValue(Float.parseFloat(label.getText()));
        } catch (NumberFormatException e) {
          polygonModel.setValue(Float.NaN);
        }
      }
    };
    label.addKeyListener(keyListener);
    label.addDisposeListener(new DisposeListener() {

      @SuppressWarnings("unused")
      public void widgetDisposed(final DisposeEvent event) {
        label.removeKeyListener(keyListener);
      }
    });
    return label;
  }

  /**
   * Creates a push button for the polygon color.
   * @param composite the parent composite.
   * @param polygonModel the associated polygon model.
   */
  private Label createColorLabel(final Composite composite, final PolygonModel polygonModel) {
    final Label label = new Label(composite, SWT.BORDER);
    Color color = new Color(null, polygonModel.getColor());
    label.setBackground(color);
    label.setForeground(color);
    color.dispose();
    label.setLayoutData(createGridData());
    final IModelListener listener = new IModelListener() {

      public void propertyChanged(String key) {
        if (key.equals(PolygonModel.COLOR)) {
          Color color2 = new Color(null, polygonModel.getColor());
          label.setBackground(color2);
          label.setForeground(color2);
          color2.dispose();
        }
      }
    };
    polygonModel.addListener(listener);
    label.addDisposeListener(new DisposeListener() {

      @SuppressWarnings("unused")
      public void widgetDisposed(final DisposeEvent event) {
        polygonModel.removeListener(listener);
      }
    });
    return label;
  }

  /**
   * Returns grid layout data with common settings.
   * @return grid layout data with common settings
   */
  private GridData createGridData() {
    GridData gridData = new GridData();
    gridData.grabExcessHorizontalSpace = false;
    gridData.horizontalAlignment = SWT.FILL;
    gridData.horizontalSpan = 1;
    gridData.grabExcessVerticalSpace = false;
    gridData.verticalAlignment = SWT.FILL;
    gridData.verticalSpan = 1;
    return gridData;
  }

  public void polygonModelUpdated(final PolygonRegionsModelEvent event) {
    PolygonRegionsModel model = event.getPolygonRegionsModel();
    Type type = event.getType();
    if (type.equals(PolygonRegionsModelEvent.Type.PolygonsUpdated)) {
      int[] indices = event.getPolygonIndices();
      if (indices != null) {
        for (int index : indices) {
          PolygonModel polygonModel = model.getPolygonModel(index);
          _visibleButtons[index].setSelection(polygonModel.getVisible());
          _nameTexts[index].setText(polygonModel.getText());
          _valueLabels[index].setText(Double.toString(polygonModel.getValue()));
          Color color = new Color(null, polygonModel.getColor());
          _colorLabels[index].setBackground(color);
          _colorLabels[index].setForeground(color);
          color.dispose();
          //          model.getPolygonModel(index).setType(polygonModel.getType());
          //          model.getPolygonModel(index).setExists(polygonModel.getExists());
          //          model.getPolygonModel(index).setVisible(polygonModel.getVisible());
          //          model.getPolygonModel(index).setId(polygonModel.getId());
          //          model.getPolygonModel(index).setName(polygonModel.getName());
          //          model.getPolygonModel(index).setValue(polygonModel.getValue());
          //          model.getPolygonModel(index).setColor(polygonModel.getColor());
        }
      }
      //      model.setNormalizationFactor(model.getNormalizationFactor());
      //      model.setPolygonsFilled(model.getPolygonsFilled());
    }
  }

  /**
   * Convenience method to create a dialog for editing
   * an polygon regions model. This method handles the
   * setup and creation, so that users need only call
   * the open() method.
   * @param model the polygon regions model.
   * @return the dialog.
   */
  public static PolygonRegionsModelEditorDialog createEditorDialog(Shell shell, final PolygonRegionsModel model) {

    // Create the dialog.
    PolygonRegionsModelEditorDialog dialog = new PolygonRegionsModelEditorDialog(shell, model);
    dialog.setShellStyle(SWT.TITLE | SWT.MODELESS);
    dialog.setBlockOnOpen(true);
    dialog.create();

    // Set the dialog title.
    dialog.getShell().pack();
    dialog.getShell().setText("Polygon Regions Model");
    Point size = dialog.getShell().computeSize(800, SWT.DEFAULT);
    dialog.getShell().setSize(size);

    return dialog;
  }
}
