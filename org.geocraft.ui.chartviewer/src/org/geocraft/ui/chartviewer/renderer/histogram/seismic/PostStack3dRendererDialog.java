/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.chartviewer.renderer.histogram.seismic;


import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.model.IModel;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.ui.chartviewer.renderer.histogram.seismic.PostStack3dRendererModel.DataBounds;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.ModelDialog;


/**
 * Settings dialog for the <code>PostStack3d</code> renderer
 * in a histogram view.
 */
public class PostStack3dRendererDialog extends ModelDialog {

  /** The grid renderer. */
  private PostStack3dRenderer _renderer;

  public PostStack3dRendererDialog(final Shell shell, String title, PostStack3dRenderer renderer) {
    super(shell, title);
    _renderer = renderer;
  }

  @Override
  protected int getNumForms() {
    return 1;
  }

  @Override
  protected void buildModelForms(IModelForm[] forms) {
    IModelForm form = forms[0];

    FormSection section = form.addSection("Parameters", false);

    section.addColorField(PostStack3dRendererModel.COLOR);
    section.addSpinnerField(PostStack3dRendererModel.NUMBER_OF_CELLS, 2, 100, 0, 1);
    section.addRadioGroupField(PostStack3dRendererModel.DATA_BOUNDS, DataBounds.values());
    section.addEntityComboField(PostStack3dRendererModel.AREA_OF_INTEREST, AreaOfInterest.class);
  }

  @Override
  public void propertyChanged(String triggerKey) {
    super.propertyChanged(triggerKey);
    if (triggerKey.equals(PostStack3dRendererModel.DATA_BOUNDS)) {
      PostStack3dRendererModel model = (PostStack3dRendererModel) _model;
      boolean aoiEnabled = model.getDataBounds().equals(DataBounds.USE_AOI);
      setFieldEnabled(PostStack3dRendererModel.AREA_OF_INTEREST, aoiEnabled);
    }
  }

  @Override
  protected IModel createModel() {
    return new PostStack3dRendererModel(_renderer.getSettingsModel());
  }

  @Override
  protected void applySettings() {
    _renderer.updateSettings((PostStack3dRendererModel) _model);
  }
}
