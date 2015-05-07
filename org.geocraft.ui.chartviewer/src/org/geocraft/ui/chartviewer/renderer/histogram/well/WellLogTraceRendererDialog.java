/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.chartviewer.renderer.histogram.well;


import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.model.IModel;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.ModelDialog;


/**
 * Settings dialog for the <code>WellLogTrace</code> renderer
 * in a histogram view.
 */
public class WellLogTraceRendererDialog extends ModelDialog {

  /** The grid renderer. */
  private WellLogTraceRenderer _renderer;

  public WellLogTraceRendererDialog(final Shell shell, String title, WellLogTraceRenderer renderer) {
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

    section.addColorField(WellLogTraceRendererModel.COLOR);
    section.addSpinnerField(WellLogTraceRendererModel.NUMBER_OF_CELLS, 2, 100, 0, 1);
  }

  @Override
  protected IModel createModel() {
    return new WellLogTraceRendererModel(_renderer.getSettingsModel());
  }

  @Override
  protected void applySettings() {
    _renderer.updateSettings((WellLogTraceRendererModel) _model);
  }
}
