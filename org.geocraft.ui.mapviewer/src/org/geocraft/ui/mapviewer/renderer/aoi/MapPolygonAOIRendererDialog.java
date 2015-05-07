/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.mapviewer.renderer.aoi;


import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.model.IModel;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.ModelDialog;
import org.geocraft.ui.plot.settings.PropertiesSectionFactory;


public class MapPolygonAOIRendererDialog extends ModelDialog implements IAOIRendererConstants {

  /** The AOI renderer. */
  private final MapPolygonAOIRenderer _renderer;

  public MapPolygonAOIRendererDialog(final Shell shell, String title, final MapPolygonAOIRenderer renderer) {
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

    FormSection generalSection = form.addSection("General", false);

    generalSection.addSpinnerField(TRANSPARENCY, 0, 100, 0, 1);

    // Add the section for editing inclusive fill properties.
    PropertiesSectionFactory.addFillSection(form, "Inclusive Fill Properties", INCLUSIVE_FILL_STYLE,
        INCLUSIVE_FILL_COLOR);

    // Add the section for editing inclusive line properties.
    PropertiesSectionFactory.addLineSection(form, "Inclusive Line Properties", INCLUSIVE_LINE_STYLE,
        INCLUSIVE_LINE_WIDTH, INCLUSIVE_LINE_COLOR);

    // Add the section for editing exclusive fill properties.
    PropertiesSectionFactory.addFillSection(form, "Exclusive Fill Properties", EXCLUSIVE_FILL_STYLE,
        EXCLUSIVE_FILL_COLOR);

    // Add the section for editing exclusive line properties.
    PropertiesSectionFactory.addLineSection(form, "Exclusive Line Properties", EXCLUSIVE_LINE_STYLE,
        EXCLUSIVE_LINE_WIDTH, EXCLUSIVE_LINE_COLOR);

    // Add the section for editing point properties.
    PropertiesSectionFactory.addPointSection(form, "Point Properties", POINT_STYLE, POINT_SIZE, POINT_COLOR);

  }

  @Override
  protected IModel createModel() {
    return new MapPolygonAOIRendererModel(_renderer.getSettingsModel());
  }

  @Override
  public void applySettings() {
    _renderer.updateSettings((MapPolygonAOIRendererModel) _model);
  }

}
