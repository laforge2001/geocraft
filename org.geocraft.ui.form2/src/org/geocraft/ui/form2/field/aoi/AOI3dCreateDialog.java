/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field.aoi;


import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.model.IModel;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.ModelDialog;
import org.geocraft.ui.form2.field.EntityComboField;


public class AOI3dCreateDialog extends ModelDialog {

  private AreaOfInterest _aoi;

  private EntityComboField _referenceField;

  public AOI3dCreateDialog(Shell shell) {
    super(shell, "Create AOI");
  }

  @Override
  public void createButtonsForButtonBar(final Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
  }

  @Override
  protected void applySettings() {
    _referenceField.unsubscribeFromMessageService();
    AOI3dModel model = (AOI3dModel) _model;
    _aoi = model.createAOI();
  }

  @Override
  protected void undoSettings() {
    // Occurs only when the 'Cancel' button is pressed, so no need to do anything.
  }

  public AreaOfInterest getAOI() {
    return _aoi;
  }

  @Override
  protected void buildModelForms(IModelForm[] forms) {
    IModelForm modelForm = forms[0];

    FormSection inputSection = modelForm.addSection("Reference");
    _referenceField = inputSection.addEntityComboField(AOI3dModel.REFERENCE_ENTITY, new AOI3dReferenceSpecification());

    inputSection.addLabelField(AOI3dModel.REFERENCE_INLINE_START);
    inputSection.addLabelField(AOI3dModel.REFERENCE_INLINE_END);
    inputSection.addLabelField(AOI3dModel.REFERENCE_INLINE_DELTA);
    inputSection.addLabelField(AOI3dModel.REFERENCE_XLINE_START);
    inputSection.addLabelField(AOI3dModel.REFERENCE_XLINE_END);
    inputSection.addLabelField(AOI3dModel.REFERENCE_XLINE_DELTA);

    FormSection outputAoiParam = modelForm.addSection("Output Area of Interest Parameters");
    outputAoiParam.addTextField(AOI3dModel.INLINE_START);
    outputAoiParam.addTextField(AOI3dModel.INLINE_END);
    outputAoiParam.addTextField(AOI3dModel.INLINE_DELTA);
    outputAoiParam.addTextField(AOI3dModel.XLINE_START);
    outputAoiParam.addTextField(AOI3dModel.XLINE_END);
    outputAoiParam.addTextField(AOI3dModel.XLINE_DELTA);

    FormSection outputZRange = modelForm.addSection("Optional Z Range");
    outputZRange.addCheckboxField(AOI3dModel.HAS_Z_RANGE);
    outputZRange.addTextField(AOI3dModel.Z_START);
    outputZRange.addTextField(AOI3dModel.Z_END);
    Unit[] zUnits = { UnitPreferences.getInstance().getTimeUnit(),
        UnitPreferences.getInstance().getVerticalDistanceUnit() };
    outputZRange.addComboField(AOI3dModel.Z_UNIT, zUnits);

    FormSection outputSection = modelForm.addSection("Output");

    outputSection.addTextField(AOI3dModel.OUTPUT_AOI_NAME);
  }

  @Override
  protected IModel createModel() {
    return new AOI3dModel();
  }

  @Override
  protected int getNumForms() {
    return 1;
  }

}
