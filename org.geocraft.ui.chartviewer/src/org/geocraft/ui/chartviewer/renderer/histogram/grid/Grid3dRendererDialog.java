/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.chartviewer.renderer.histogram.grid;


import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.model.IModel;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.ui.chartviewer.renderer.histogram.grid.Grid3dRendererModel.DataBounds;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.ModelDialog;


/**
 * Settings dialog for the <code>Grid3d</code> renderer
 * in a histogram view.
 */
public class Grid3dRendererDialog extends ModelDialog {

  /** The grid renderer. */
  private Grid3dRenderer _renderer;

  public Grid3dRendererDialog(final Shell shell, String title, Grid3dRenderer renderer) {
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

    section.addColorField(Grid3dRendererModel.COLOR);
    section.addSpinnerField(Grid3dRendererModel.NUMBER_OF_CELLS, 2, 100, 0, 1);
    section.addRadioGroupField(Grid3dRendererModel.DATA_BOUNDS, DataBounds.values());
    section.addEntityComboField(Grid3dRendererModel.AREA_OF_INTEREST, AreaOfInterest.class);
  }

  @Override
  public void propertyChanged(String triggerKey) {
    super.propertyChanged(triggerKey);
    if (triggerKey.equals(Grid3dRendererModel.DATA_BOUNDS)) {
      Grid3dRendererModel model = (Grid3dRendererModel) _model;
      boolean aoiEnabled = model.getDataBounds().equals(DataBounds.USE_AOI);
      setFieldEnabled(Grid3dRendererModel.AREA_OF_INTEREST, aoiEnabled);
    }
  }

  @Override
  protected IModel createModel() {
    return new Grid3dRendererModel(_renderer.getSettingsModel());
  }

  @Override
  protected void applySettings() {
    _renderer.updateSettings((Grid3dRendererModel) _model);
  }
}
