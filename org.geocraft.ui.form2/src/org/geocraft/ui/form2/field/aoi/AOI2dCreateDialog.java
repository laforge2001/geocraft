/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field.aoi;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.geocraft.core.model.IModel;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.aoi.SeismicSurvey2dAOI;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.seismic.SeismicLine2d;
import org.geocraft.core.model.seismic.SeismicSurvey2d;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.ModelDialog;
import org.geocraft.ui.form2.field.EntityComboField;


public class AOI2dCreateDialog extends ModelDialog {

  private SeismicSurvey2dAOI _aoi;

  private EntityComboField _referenceField;

  private ScrolledForm _scrolledForm;

  private Composite _shotpointRangesComposite;

  private Button[] _lineToggleButtons;

  private Text[] _shotpointStartTexts;

  private Text[] _shotpointEndTexts;

  private Model _sourceModel;

  private boolean _inMemory;

  public AOI2dCreateDialog(Shell shell) {
    this(shell, false);
  }

  public AOI2dCreateDialog(final Shell shell, final boolean inMemory) {
    super(shell, "Create AOI");
    _inMemory = inMemory;
    setShellStyle(SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL | SWT.RESIZE);
  }

  @Override
  public void createButtonsForButtonBar(final Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
  }

  @Override
  protected void applySettings() {
    _referenceField.unsubscribeFromMessageService();
    AOI2dModel model = (AOI2dModel) _model;
    Map<String, FloatRange> cdpRanges = new HashMap<String, FloatRange>();
    SeismicSurvey2d survey = model.getReferenceSurvey();
    if (survey != null) {
      for (int i = 0; i < _lineToggleButtons.length; i++) {
        if (_lineToggleButtons[i].getSelection()) {
          String lineName = _lineToggleButtons[i].getText();
          lineName = lineName.substring(5);
          SeismicLine2d seismicLine = survey.getLineByName(lineName);
          float shotpointStart = Float.parseFloat(_shotpointStartTexts[i].getText());
          float shotpointEnd = Float.parseFloat(_shotpointEndTexts[i].getText());
          float[] cdps = seismicLine.transformShotpointsToCDPs(new float[] { shotpointStart, shotpointEnd });
          float traceStart = cdps[0];
          float traceEnd = cdps[1];
          float traceDelta = 1;
          int startIndex = Math.round((traceStart - seismicLine.getCDPStart()) / seismicLine.getCDPDelta());
          int endIndex = Math.round((traceEnd - seismicLine.getCDPStart()) / seismicLine.getCDPDelta());
          int deltaIndex = Math.round(Math.abs(traceDelta / seismicLine.getCDPDelta()));
          startIndex = Math.max(startIndex, 0);
          startIndex = Math.min(startIndex, seismicLine.getNumBins() - 1);
          endIndex = Math.max(endIndex, 0);
          endIndex = Math.min(endIndex, seismicLine.getNumBins() - 1);
          deltaIndex = Math.max(deltaIndex, 1);
          traceStart = seismicLine.getCDPStart() + startIndex * seismicLine.getCDPDelta();
          traceEnd = seismicLine.getCDPStart() + endIndex * seismicLine.getCDPDelta();
          traceDelta = seismicLine.getCDPDelta() * deltaIndex;
          cdpRanges.put(lineName, new FloatRange(traceStart, traceEnd, traceDelta));
        }
      }
      model.setTraceRanges(cdpRanges);
    }
    _aoi = model.createAOI(_inMemory);
  }

  @Override
  protected void undoSettings() {
    // Occurs only when the 'Cancel' button is pressed, so no need to do anything.
  }

  public SeismicSurvey2dAOI getAOI() {
    return _aoi;
  }

  @Override
  protected void buildModelForms(IModelForm[] forms) {
    IModelForm modelForm = forms[0];

    FormSection inputSection = modelForm.addSection("Reference");
    _referenceField = inputSection.addEntityComboField(AOI2dModel.REFERENCE_ENTITY, new AOI2dReferenceSpecification());

    Composite composite = modelForm.createComposite("Trace Ranges", false);
    composite.setLayout(new GridLayout());
    _scrolledForm = new ScrolledForm(composite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.BORDER_SOLID);
    _scrolledForm.setMinHeight(400);
    GridData layoutData = new GridData();
    layoutData.grabExcessHorizontalSpace = true;
    layoutData.grabExcessVerticalSpace = true;
    layoutData.horizontalAlignment = SWT.FILL;
    layoutData.verticalAlignment = SWT.FILL;
    layoutData.heightHint = 400;
    _scrolledForm.setLayoutData(layoutData);
    _scrolledForm.getBody().setLayout(new GridLayout());
    _scrolledForm.setMinHeight(400);
    _scrolledForm.setAlwaysShowScrollBars(true);
    _managedForm.getToolkit().adapt(_scrolledForm.getBody());

    FormSection outputSection = modelForm.addSection("Output");

    outputSection.addTextField(AOI2dModel.OUTPUT_AOI_NAME);

    rebuildLineList();
  }

  @Override
  public void propertyChanged(String triggerKey) {
    super.propertyChanged(triggerKey);
    if (triggerKey.equals(AOI2dModel.REFERENCE_ENTITY)) {
      rebuildLineList();
    }
  }

  /**
   * 
   */
  private void rebuildLineList() {
    if (_shotpointRangesComposite != null) {
      _shotpointRangesComposite.dispose();
      _shotpointRangesComposite = null;
    }
    _shotpointRangesComposite = new Composite(_scrolledForm.getBody(), SWT.NONE);
    GridData layoutData = new GridData();
    layoutData.horizontalSpan = 1;
    layoutData.verticalSpan = 1;
    layoutData.grabExcessHorizontalSpace = true;
    layoutData.grabExcessVerticalSpace = false;
    layoutData.horizontalAlignment = SWT.FILL;
    layoutData.verticalAlignment = SWT.FILL;
    _shotpointRangesComposite.setLayoutData(layoutData);
    GridLayout layout = new GridLayout();
    layout.numColumns = 5;
    layout.makeColumnsEqualWidth = false;
    _shotpointRangesComposite.setLayout(layout);
    AOI2dModel model = (AOI2dModel) _model;
    if (model != null) {
      SeismicSurvey2d survey = model.getReferenceSurvey();
      int[] lineNumbers = model.getReferenceLineNumbers();
      System.out.println("SURVEY=" + survey);
      if (survey != null) {
        System.out.println("LINE NOS: = " + lineNumbers.length);
        int numLines = lineNumbers.length;
        _lineToggleButtons = new Button[numLines];
        _shotpointStartTexts = new Text[numLines];
        _shotpointEndTexts = new Text[numLines];
        for (int i = 0; i < numLines; i++) {
          final int index = i;
          int lineNumber = lineNumbers[i];
          SeismicLine2d seismicLine = survey.getLineByNumber(lineNumber);
          float shotpointStart = seismicLine.getShotpointStart();
          float shotpointEnd = seismicLine.getShotpointEnd();

          _lineToggleButtons[i] = new Button(_shotpointRangesComposite, SWT.CHECK);
          _lineToggleButtons[i].setText("Line " + seismicLine.getDisplayName());
          _lineToggleButtons[i].setLayoutData(createLayoutData(SWT.END));
          _lineToggleButtons[i].setSelection(true);

          final Text startLabel = new Text(_shotpointRangesComposite, SWT.NONE);
          startLabel.setText(" Shotpoints Start");
          startLabel.setEditable(false);
          startLabel.setLayoutData(createLayoutData(SWT.END));
          _shotpointStartTexts[i] = new Text(_shotpointRangesComposite, SWT.BORDER);
          _shotpointStartTexts[i].setText("" + shotpointStart);
          _shotpointStartTexts[i].setLayoutData(createLayoutData(SWT.FILL));

          final Text endLabel = new Text(_shotpointRangesComposite, SWT.NONE);
          endLabel.setText("End");
          endLabel.setEditable(false);
          endLabel.setLayoutData(createLayoutData(SWT.END));
          _shotpointEndTexts[i] = new Text(_shotpointRangesComposite, SWT.BORDER);
          _shotpointEndTexts[i].setText("" + shotpointEnd);
          _shotpointEndTexts[i].setLayoutData(createLayoutData(SWT.FILL));

          _lineToggleButtons[i].addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event event) {
              boolean enabled = _lineToggleButtons[index].getSelection();
              startLabel.setEnabled(enabled);
              endLabel.setEnabled(enabled);
              _shotpointStartTexts[index].setEnabled(enabled);
              _shotpointEndTexts[index].setEnabled(enabled);
            }

          });

          _managedForm.getToolkit().adapt(_lineToggleButtons[i], true, true);
        }
        _managedForm.getToolkit().adapt(_shotpointRangesComposite);
      }
      _scrolledForm.redraw();
      _scrolledForm.update();
      _scrolledForm.setMinHeight(400);
      _scrolledForm.reflow(true);
    }
  }

  private GridData createLayoutData(int horizontalAlignment) {
    GridData layoutData = new GridData();
    layoutData.grabExcessHorizontalSpace = false;
    layoutData.grabExcessVerticalSpace = false;
    layoutData.horizontalSpan = 1;
    layoutData.verticalSpan = 1;
    layoutData.horizontalAlignment = horizontalAlignment;
    layoutData.verticalAlignment = SWT.FILL;
    return layoutData;
  }

  @Override
  protected IModel createModel() {
    AOI2dModel model = new AOI2dModel();
    if (_sourceModel != null) {
      model.updateFrom(_sourceModel);
    }
    return model;
  }

  @Override
  protected int getNumForms() {
    return 1;
  }

  public void setSourceModel(AOI2dModel sourceModel) {
    _sourceModel = sourceModel;
  }

}
