/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.abavo.ellipse;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.geocraft.abavo.ellipse.EllipseRegionsModel;
import org.geocraft.abavo.ellipse.EllipseRegionsModelDef;
import org.geocraft.core.model.IModel;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.ModelDialog;
import org.geocraft.ui.form2.field.CheckboxField;


/**
 * This class is a dialog for editing an ellipse regions model.
 * The dialog is a simple form dialog that contains a
 * composite for editing the ellipse regions model. The
 * dialog also has 2 custom buttons, one for loading an
 * ellipse regions model from disk, and one for saving
 * the current ellipse regions model to disk.
 */
public class EllipseRegionsModelEditorDialog extends ModelDialog {

  /** The ellipse regions model to edit. */
  private final EllipseRegionsModel _referenceModel;

  /**
   * The private constructor. This dialog must be
   * created using the static convenience method.
   * @param shell the parent shell.
   * @param model the ellipse regions model to edit.
   */
  private EllipseRegionsModelEditorDialog(final Shell shell, final EllipseRegionsModel model) {
    super(shell, "Ellipse Regions Model");
    _referenceModel = model;
  }

  @Override
  protected void applySettings() {
    //_referenceModel.updateFrom(_model);
  }

  @Override
  protected int getNumForms() {
    return 1;
  }

  @Override
  protected void buildModelForms(IModelForm[] forms) {
    IModelForm form = forms[0];

    EllipseRegionsModel model = _referenceModel;

    // Add the general section.
    FormSection generalSection = form.addSection("General", false);

    CheckboxField symmetryField = generalSection.addCheckboxField(EllipseRegionsModel.SYMMETRIC_REGIONS);
    symmetryField.setLabel("Symmetry Lock on Region Paris");

    // Add the ellipses section.
    final Composite composite1 = form.createComposite("Ellipses", false);
    composite1.setLayout(createGridLayout());

    // Create the ellipses table.
    TableViewer ellipseTableViewer = createEllipseTable(composite1, model);
    ellipseTableViewer.getControl().setLayoutData(createGridData());
    ellipseTableViewer.getControl().setSize(800, 250);

    // Add the region boundaries section.
    final Composite composite2 = form.createComposite("Region Boundaries", false);
    composite2.setLayout(createGridLayout());

    // Crate the region boundaries table.
    TableViewer boundsTableViewer = createBoundsTable(composite2, model);
    boundsTableViewer.getControl().setLayoutData(createGridData());
    boundsTableViewer.getControl().setSize(800, 500);
  }

  @Override
  protected IModel createModel() {
    return _referenceModel;
  }

  @Override
  public void createButtonsForButtonBar(final Composite parent) {

    IConfigurationElement[] configElements = Platform.getExtensionRegistry().getConfigurationElementsFor(
        ELLIPSE_REGIONS_MODEL_EXTENSION_POINT_ID());
    final List<EllipseRegionsModelDef> defs = new ArrayList<EllipseRegionsModelDef>();
    for (IConfigurationElement configElement : configElements) {
      String name = configElement.getAttribute("name");
      try {
        EllipseRegionsModelDef def = (EllipseRegionsModelDef) configElement.createExecutableExtension("class");
        defs.add(def);
      } catch (Exception ex) {
        ex.printStackTrace();
        ServiceProvider.getLoggingService().getLogger(EllipseRegionsModelDef.class)
            .error("Error loading ellipse regions model definition: " + name + " " + ex.toString());
      }
    }

    ((GridLayout) parent.getLayout()).numColumns++;
    final Combo combo = createCombo(parent, defs.toArray(new EllipseRegionsModelDef[0]));
    GridData gridData = new GridData();
    gridData.grabExcessHorizontalSpace = false;
    gridData.horizontalAlignment = SWT.FILL;
    gridData.horizontalSpan = 1;
    gridData.grabExcessVerticalSpace = false;
    combo.setLayoutData(gridData);
    combo.addListener(SWT.Selection, new Listener() {

      @Override
      @SuppressWarnings("unused")
      public void handleEvent(final Event event) {
        int index = combo.getSelectionIndex();
        if (index >= 1 && index <= defs.size()) {
          EllipseRegionsModelDef def = defs.get(index - 1);
          def.updateEllipseRegionsModel(_referenceModel);
        }
      }

    });
    combo.select(0);

    // Create the button for loading an ellipse regions model.
    final LoadEllipseRegionsModel loadAction = new LoadEllipseRegionsModel(parent, _referenceModel);

    // Create the button for loading an ellipse regions model.
    // TODO: For now, just use the BACK_ID...
    Button loadButton = createButton(parent, IDialogConstants.BACK_ID, "Load...", false);
    loadButton.setImage(loadAction.getImageDescriptor().createImage());
    loadButton.addListener(SWT.Selection, new Listener() {

      @Override
      @SuppressWarnings("unused")
      public void handleEvent(final Event event) {
        // Run the load action.
        loadAction.run();
      }

    });

    // Create the action for saving an ellipse regions model.
    final SaveEllipseRegionsModel saveAction = new SaveEllipseRegionsModel(parent, _referenceModel);

    // Create the button for saving an ellipse regions model.
    // TODO: For now, just use the NEXT_ID...
    Button saveButton = createButton(parent, IDialogConstants.NEXT_ID, "Save...", false);
    saveButton.setImage(saveAction.getImageDescriptor().createImage());
    saveButton.addListener(SWT.Selection, new Listener() {

      @Override
      @SuppressWarnings("unused")
      public void handleEvent(final Event event) {
        // Run the save action.
        saveAction.run();
      }

    });

    createButton(parent, IDialogConstants.CANCEL_ID, "Close", false);
  }

  /**
   * @return
   */
  private String ELLIPSE_REGIONS_MODEL_EXTENSION_POINT_ID() {
    return "org.geocraft.abavo.ellipseRegionsModel";
  }

  private Combo createCombo(Composite parent, EllipseRegionsModelDef[] ellipseRegionsModelDefs) {
    Combo combo = new Combo(parent, SWT.READ_ONLY | SWT.DROP_DOWN | SWT.SIMPLE);
    String[] items = new String[ellipseRegionsModelDefs.length + 1];
    items[0] = "<Select Predefined Ellipses>";
    for (int i = 0; i < ellipseRegionsModelDefs.length; i++) {
      items[i + 1] = ellipseRegionsModelDefs[i].getName();
    }
    combo.setData(ellipseRegionsModelDefs);
    combo.setItems(items);
    return combo;
  }

  /**
  * Returns a grid layout with common settings.
  * @return a grid layout with common settings
  */
  private GridLayout createGridLayout() {
    GridLayout gridLayout;
    gridLayout = new GridLayout();
    gridLayout.makeColumnsEqualWidth = false;
    gridLayout.horizontalSpacing = 1;
    gridLayout.numColumns = 3;
    return gridLayout;
  }

  /**
   * Returns grid layout data with common settings.
   * @return grid layout data with common settings
   */
  private GridData createGridData() {
    GridData gridData = new GridData();
    gridData.grabExcessHorizontalSpace = false;
    gridData.horizontalAlignment = SWT.FILL;
    gridData.horizontalSpan = 3;
    gridData.grabExcessVerticalSpace = false;
    return gridData;
  }

  /**
   * Creates the ellipses table.
   * @param model the ellipse regions model.
   */
  private TableViewer createEllipseTable(final Composite parent, final EllipseRegionsModel model) {
    // Create the custom content and label providers.
    IContentProvider contentProvider = new EllipseTableContentProvider();
    ITableLabelProvider labelProvider = new EllipseTableLabelProvider();

    // Create the table and columns.
    Table table = new Table(parent, SWT.BORDER);
    String[] columnNames = { "Ellipse", "Slope", "Length", "Width", "Center X", "Center Y" };
    for (int i = 0; i < columnNames.length; i++) {
      TableColumn col = new TableColumn(table, SWT.FILL, i);
      col.setText(columnNames[i]);
      col.setWidth(150);
    }
    table.setLinesVisible(true);
    table.setHeaderVisible(true);

    // Create the table viewer.
    TableViewer tableViewer = new TableViewer(table);
    tableViewer.setContentProvider(contentProvider);
    tableViewer.setLabelProvider(labelProvider);
    tableViewer.setColumnProperties(columnNames);
    tableViewer.setInput(model);
    Point size = tableViewer.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
    tableViewer.getControl().setSize(size);

    return tableViewer;
  }

  /**
   * Creates the region boundaries table.
   * @param model the ellipse regions model.
   */
  private TableViewer createBoundsTable(final Composite parent, final EllipseRegionsModel model) {
    // Create the custom content and label providers.
    IContentProvider contentProvider = new RegionsBoundaryTableContentProvider();
    ITableLabelProvider labelProvider = new RegionsBoundaryTableLabelProvider();

    // Create the table and columns.
    Table table = new Table(parent, SWT.BORDER);
    String[] columnNames = { "Regions Boundary", "Outer X", "Outer Y", "Inner X", "Inner Y" };
    for (int i = 0; i < columnNames.length; i++) {
      TableColumn col = new TableColumn(table, SWT.FILL, i);
      col.setText(columnNames[i]);
      if (i == 0) {
        col.setWidth(400);
      } else {
        col.setWidth(150);
      }
    }
    table.setLinesVisible(true);
    table.setHeaderVisible(true);

    // Create the table viewer.
    TableViewer tableViewer = new TableViewer(table);
    tableViewer.setContentProvider(contentProvider);
    tableViewer.setLabelProvider(labelProvider);
    tableViewer.setColumnProperties(columnNames);
    tableViewer.setInput(model);
    Point size = tableViewer.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
    tableViewer.getControl().setSize(size);
    return tableViewer;
  }

  /**
   * Convenience method to create a dialog for editing
   * an ellipse regions model. This method handles the
   * setup and creation, so that users need only call
   * the open() method.
   * @param model the ellipse regions model.
   * @return the dialog.
   */
  public static EllipseRegionsModelEditorDialog createEditorDialog(Shell shell, final EllipseRegionsModel model) {

    // Create the dialog.
    EllipseRegionsModelEditorDialog dialog = new EllipseRegionsModelEditorDialog(shell, model);
    dialog.setShellStyle(SWT.TITLE | SWT.MODELESS);
    dialog.setBlockOnOpen(true);
    dialog.create();

    // Set the dialog title.
    dialog.getShell().pack();
    dialog.getShell().setText("Ellipse Regions Model");
    Point size = dialog.getShell().computeSize(1000, SWT.DEFAULT);
    dialog.getShell().setSize(size);

    return dialog;
  }

}
