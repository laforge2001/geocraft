/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot;


import org.geocraft.abavo.crossplot.CrossplotBoundsModel.BoundsType;
import org.geocraft.ui.form2.AbstractModelView;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;


public class CrossplotBoundsView extends AbstractModelView {

  private CrossplotBoundsModel _undoModel;

  public CrossplotBoundsView() {
    // No action.
  }

  public void setModel(CrossplotBoundsModel model) {
    _undoModel = new CrossplotBoundsModel(model);
    super.setModel(model);
  }

  @Override
  public void buildView(IModelForm form) {
    FormSection section = form.addSection("General", false);

    section.addComboField(CrossplotBoundsModel.BOUNDS_TYPE, CrossplotBoundsModel.BoundsType.values());

    FormSection commonSection = form.addSection("Common Bounds", false);

    commonSection.addTextField(CrossplotBoundsModel.COMMON_MINMAX);

    FormSection userSection = form.addSection("User-Defined Bounds", false);

    userSection.addTextField(CrossplotBoundsModel.START_A);

    userSection.addTextField(CrossplotBoundsModel.END_A);

    userSection.addTextField(CrossplotBoundsModel.START_B);

    userSection.addTextField(CrossplotBoundsModel.END_B);

    updateView("");
  }

  public void applyChanges(final ABavoCrossplot crossplot) {
    applyChanges(crossplot, (CrossplotBoundsModel) getModel());
  }

  public void undoChanges(final ABavoCrossplot crossplot) {
    applyChanges(crossplot, _undoModel);
  }

  /**
   * Applies the specified crossplot bounds model to the crossplot.
   * 
   * @param crossplot the crossplot.
   * @param model the model of bounds properties to apply.
   */
  private void applyChanges(final ABavoCrossplot crossplot, final CrossplotBoundsModel model) {
    crossplot.applyBounds(model);
  }

  @Override
  public void updateView(String key) {
    if (key.equals(CrossplotBoundsModel.BOUNDS_TYPE)) {
      CrossplotBoundsModel model = (CrossplotBoundsModel) getModel();
      if (model == null) {
        return;
      }
      BoundsType boundsType = model.getBoundsType();
      boolean commonFlag = boundsType.equals(BoundsType.COMMON_MIN_MAX);
      boolean userFlag = boundsType.equals(BoundsType.USER_DEFINED);
      setFieldEnabled(CrossplotBoundsModel.COMMON_MINMAX, commonFlag);
      setFieldEnabled(CrossplotBoundsModel.START_A, userFlag);
      setFieldEnabled(CrossplotBoundsModel.END_A, userFlag);
      setFieldEnabled(CrossplotBoundsModel.START_B, userFlag);
      setFieldEnabled(CrossplotBoundsModel.END_B, userFlag);
    }
  }

}
