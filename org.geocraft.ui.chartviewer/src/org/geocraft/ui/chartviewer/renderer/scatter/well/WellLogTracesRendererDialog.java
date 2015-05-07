/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.chartviewer.renderer.scatter.well;


import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.model.IModel;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.ModelDialog;
import org.geocraft.ui.plot.defs.PointStyle;


/**
 * Settings dialog for the <code>WellLogTrace</code> renderer
 * in a scatter plot view.
 */
public class WellLogTracesRendererDialog extends ModelDialog {

  /** The grid renderer. */
  private WellLogTracesRenderer _renderer;

  public WellLogTracesRendererDialog(final Shell shell, String title, WellLogTracesRenderer renderer) {
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

    FormSection section = form.addSection("Points", false);

    section.addColorField(WellLogTracesRendererModel.POINT_COLOR);
    section.addComboField(WellLogTracesRendererModel.POINT_STYLE, PointStyle.values());
    section.addSpinnerField(WellLogTracesRendererModel.POINT_SIZE, 1, 20, 0, 1);

    FormSection zSection = form.addSection("Z Bounds", false);

    zSection.addLabelField(WellLogTracesRendererModel.Z_DOMAIN);
    zSection.addTextField(WellLogTracesRendererModel.Z_START);
    zSection.addTextField(WellLogTracesRendererModel.Z_END);
  }

  @Override
  protected IModel createModel() {
    return new WellLogTracesRendererModel(_renderer.getSettingsModel());
  }

  @Override
  protected void applySettings() {
    _renderer.updateSettings((WellLogTracesRendererModel) _model);
  }
}
