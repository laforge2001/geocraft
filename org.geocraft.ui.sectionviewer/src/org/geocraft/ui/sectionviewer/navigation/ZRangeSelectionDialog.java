/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.navigation;


import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.seismic.SeismicDataset;
import org.geocraft.ui.common.GridLayoutHelper;
import org.geocraft.ui.sectionviewer.ISectionViewer;


/**
 * This class defines a dialog for selecting the start and end z range to display for a section viewer.
 */
public class ZRangeSelectionDialog extends FormDialog {

  protected static final int NUMERIC_ENTER = 16777296;

  /** The z range of the section viewer's reference dataset. */
  private FloatRange _zRangeReference;

  /** The z range of the section viewer's current section. */
  //private FloatRange _zRangeSelected;

  /** The text control for displaying the starting z value. */
  private Text _zStartText;

  /** The text control for displaying the ending z value. */
  private Text _zEndText;

  /** The slider control for selecting the starting z value. */
  private Slider _zStartSlider;

  /** The slider control for selecting the ending z value. */
  private Slider _zEndSlider;

  /** The associated section viewer. */
  private final ISectionViewer _viewer;

  /**
   * The constructor.
   * 
   * @param shell the parent shell
   * @param title the dialog title
   */
  public ZRangeSelectionDialog(final Shell shell, final ISectionViewer viewer) {
    super(shell);
    setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    _viewer = viewer;
  }

  @Override
  public void createButtonsForButtonBar(final Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, "Ok", false);
  }

  @Override
  protected void createFormContent(final IManagedForm managedForm) {
    getShell().setText("Time/Depth Range");

    managedForm.getToolkit().decorateFormHeading(managedForm.getForm().getForm());

    Composite body = managedForm.getForm().getBody();
    FillLayout fillLayout = new FillLayout();
    fillLayout.type = SWT.HORIZONTAL | SWT.VERTICAL;
    body.setLayout(fillLayout);

    Composite mainPanel = new Composite(body, SWT.NONE);
    mainPanel.setLayout(new GridLayout(2, false));

    Group startGroup = createGroup(mainPanel);

    Domain domain = _viewer.getTraceSection().getDomain();
    String domainStr = domain.getTitle();
    if (domain.equals(Domain.DISTANCE)) {
      domainStr = "Depth";
    }

    final SeismicDataset seismicDataset = _viewer.getReferenceDataset();
    _zRangeReference = seismicDataset.getZRange();
    //_zRangeSelected = _viewer.getZRangeSelected();
    final int startIndex = 0;
    final int endIndex = _zRangeReference.getNumSteps() - 1;

    _zStartText = addLabeledText(startGroup, "Start " + domainStr, _zRangeReference.getStart() + startIndex
        * _zRangeReference.getDelta());
    _zStartText.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(final KeyEvent e) {
        if (e.keyCode == SWT.CR || e.keyCode == NUMERIC_ENTER) {
          float zStart = seismicDataset.getZStart();
          int zIndex = startIndex;
          try {
            zStart = Float.parseFloat(_zStartText.getText().trim());
            zStart = Math.max(zStart, seismicDataset.getZStart());
            zStart = Math.min(zStart, seismicDataset.getZEnd());
            zIndex = Math.round((zStart - seismicDataset.getZStart()) / seismicDataset.getZDelta());
            zStart = seismicDataset.getZStart() + zIndex * seismicDataset.getZDelta();
          } catch (NumberFormatException ex) {
            zStart = seismicDataset.getZStart();
            zIndex = startIndex;
          }
          _zStartText.setText("" + zStart);
          _zStartSlider.setSelection(zIndex);
        }
      }

    });

    _zStartSlider = createSlider(startGroup, 0, _zRangeReference.getNumSteps() - 1);
    _zStartSlider.setSelection(startIndex);
    _zStartSlider.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event event) {
        // Update the z-start label.
        int index = _zStartSlider.getSelection();
        float start = _zRangeReference.getStart() + index * _zRangeReference.getDelta();
        _zStartText.setText("" + start);

        // If the z-start is greater than the z-end, then shift the z-end.
        int zEndIndex = _zEndSlider.getSelection();
        if (index > zEndIndex) {
          zEndIndex = index;
          _zEndSlider.setSelection(zEndIndex);
          float end = _zRangeReference.getStart() + zEndIndex * _zRangeReference.getDelta();
          _zEndText.setText("" + end);
        }
      }

    });

    Group endGroup = createGroup(mainPanel);

    _zEndText = addLabeledText(endGroup, "End " + domainStr, _zRangeReference.getStart() + endIndex
        * _zRangeReference.getDelta());
    _zEndText.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(final KeyEvent e) {
        if (e.keyCode == SWT.CR || e.keyCode == NUMERIC_ENTER) {
          float zEnd = seismicDataset.getZEnd();
          int zIndex = endIndex;
          try {
            zEnd = Float.parseFloat(_zEndText.getText().trim());
            zEnd = Math.max(zEnd, seismicDataset.getZStart());
            zEnd = Math.min(zEnd, seismicDataset.getZEnd());
            zIndex = Math.round((zEnd - seismicDataset.getZStart()) / seismicDataset.getZDelta());
            zEnd = seismicDataset.getZStart() + zIndex * seismicDataset.getZDelta();
          } catch (NumberFormatException ex) {
            zEnd = seismicDataset.getZEnd();
            zIndex = endIndex;
          }
          _zEndText.setText("" + zEnd);
          _zEndSlider.setSelection(zIndex);
        }
      }

    });

    _zEndSlider = createSlider(endGroup, 0, _zRangeReference.getNumSteps() - 1);
    _zEndSlider.setSelection(endIndex);
    _zEndSlider.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event event) {
        // Update the z-end label.
        int index = _zEndSlider.getSelection();
        float end = _zRangeReference.getStart() + index * _zRangeReference.getDelta();
        _zEndText.setText("" + end);

        // If the z-end is less than the z-start, then shift the z-start.
        int zStartIndex = _zStartSlider.getSelection();
        if (index < zStartIndex) {
          zStartIndex = index;
          _zStartSlider.setSelection(zStartIndex);
          float start = _zRangeReference.getStart() + zStartIndex * _zRangeReference.getDelta();
          _zStartText.setText("" + start);
        }
      }

    });
  }

  @Override
  protected Button createButton(final Composite parent, final int id, final String label, final boolean defaultButton) {
    Button button = super.createButton(parent, id, label, defaultButton);
    Listener[] listeners = button.getListeners(SWT.Selection);
    for (Listener listener : listeners) {
      button.removeListener(SWT.Selection, listener);
    }

    button.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(final SelectionEvent e) {
        if (id == IDialogConstants.OK_ID) {
          // Update the z start and end of the viewer.
          float zStart = _zRangeReference.getStart() + _zStartSlider.getSelection() * _zRangeReference.getDelta();
          float zEnd = _zRangeReference.getStart() + _zEndSlider.getSelection() * _zRangeReference.getDelta();
          _viewer.setZStartAndEnd(zStart, zEnd);
          _viewer.setTraceSection(_viewer.getTraceSection());
          okPressed();
        }
      }
    });
    return button;
  }

  /**
   * Creates a group control in which to put controls for selecting the start or end z values.
   * 
   * @param parent the composite in which to put the group control.
   * @return the group control.
   */
  private Group createGroup(final Composite parent) {
    Group group = new Group(parent, SWT.NONE);
    group.setText("");
    group.setLayout(GridLayoutHelper.createLayout(1, false));
    group.setLayoutData(GridLayoutHelper.createLayoutData(true, true, SWT.FILL, SWT.FILL, 1, 1));
    return group;
  }

  /**
   * Creates a label and text control for displaying the start or end z value.
   * <p>
   * The text field is NOT editable, so the sliders must be used to select the z value.
   * 
   * @param group the group in which to put the text and label controls.
   * @param text the text for the label control.
   * @param value the initial value to put in the text control.
   * @return the text control.
   */
  private Text addLabeledText(final Group group, final String text, final float value) {
    Label label = new Label(group, SWT.NONE);
    label.setText(text);
    label.setAlignment(SWT.CENTER);
    label.setLayoutData(GridLayoutHelper.createLayoutData(true, false, SWT.FILL, SWT.FILL, 1, 1));
    Text textField = new Text(group, SWT.BORDER_SOLID);
    textField.setText("" + value);
    GridData gridData = GridLayoutHelper.createLayoutData(true, false, SWT.FILL, SWT.FILL, 1, 1);
    textField.setLayoutData(gridData);
    textField.setEditable(true);
    return textField;
  }

  /**
   * Creates a slider control for selecting the start or end z value.
   * <p>
   * The slider will be vertically oriented.
   * 
   * @param group the group in which to put the slider control.
   * @param min the minimum index of the slider control.
   * @param max the maximum index of the slider control.
   * @return the slider control.
   */
  private Slider createSlider(final Group group, final int min, final int max) {
    Slider slider = new Slider(group, SWT.VERTICAL);
    int thumb = 1;
    slider.setMinimum(min);
    slider.setMaximum(max + thumb);
    slider.setThumb(thumb);
    GridData gridData = GridLayoutHelper.createLayoutData(true, true, SWT.FILL, SWT.FILL, 1, 1);
    gridData.heightHint = 400;
    slider.setLayoutData(gridData);
    return slider;
  }
}
