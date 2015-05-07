/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.ui.mapviewer.renderer.seismic;


import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.model.IModel;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.ModelDialog;
import org.geocraft.ui.plot.settings.PropertiesSectionFactory;


/**
 * Settings dialog for the map view 3D seismic survey renderer.
 */
public class SeismicSurvey3dRendererDialog extends ModelDialog implements ISeismicSurvey3dRendererConstants {

  /** The survey renderer. */
  private final SeismicSurvey3dRenderer _renderer;

  public SeismicSurvey3dRendererDialog(final Shell shell, String title, final SeismicSurvey3dRenderer renderer) {
    super(shell, title);
    _renderer = renderer;
  }

  @Override
  protected int getNumForms() {
    return 1;
  }

  @Override
  protected void buildModelForms(final IModelForm[] forms) {
    IModelForm form = forms[0];

    // Add the section for editing text properties.
    PropertiesSectionFactory.addTextSection(form, "Text Properties", TEXT_FONT, TEXT_COLOR);

    // Add the section for editing line properties.
    PropertiesSectionFactory.addLineSection(form, "Line Properties", LINE_STYLE, LINE_WIDTH, LINE_COLOR);

    // Add the section for editing point properties.
    PropertiesSectionFactory.addPointSection(form, "Point Properties", POINT_STYLE, POINT_SIZE, POINT_COLOR);
  }

  @Override
  protected IModel createModel() {
    return new SeismicSurvey3dRendererModel(_renderer.getSettingsModel());
  }

  @Override
  public void applySettings() {
    _renderer.updateSettings((SeismicSurvey3dRendererModel) _model);
  }

}
