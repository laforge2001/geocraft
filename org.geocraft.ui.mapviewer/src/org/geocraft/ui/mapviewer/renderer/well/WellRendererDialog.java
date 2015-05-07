/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.mapviewer.renderer.well;


import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.model.IModel;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.PreferencesModelDialog;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.PointStyle;


/**
 * Settings dialog for the map view well bore renderer.
 */
public class WellRendererDialog extends PreferencesModelDialog implements IWellRendererConstants {

  /** The well bore renderer. */
  private final WellRenderer _renderer;

  public WellRendererDialog(final Shell shell, final String title, final WellRenderer renderer) {
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

    FormSection boreSection = form.addSection("Well Bore");

    boreSection.addComboField(BORE_LINE_STYLE, LineStyle.values());
    boreSection.addTextField(BORE_LINE_WIDTH);
    boreSection.addColorField(BORE_LINE_COLOR);

    boreSection.addComboField(TOP_HOLE_SYMBOL, PointStyle.values());
    boreSection.addTextField(TOP_HOLE_SYMBOL_SIZE);
    boreSection.addColorField(TOP_HOLE_SYMBOL_COLOR);

    boreSection.addComboField(BOTTOM_HOLE_SYMBOL, PointStyle.values());
    boreSection.addTextField(BOTTOM_HOLE_SYMBOL_SIZE);
    boreSection.addColorField(BOTTOM_HOLE_SYMBOL_COLOR);
  }

  @Override
  protected IModel createModel() {
    return new WellRendererModel(_renderer.getSettingsModel());
  }

  @Override
  public void applySettings() {
    _renderer.updateSettings((WellRendererModel) _model);
  }

  @Override
  protected void updatePreferences() {
    WellRendererModel model = (WellRendererModel) _model;

    // Update the default renderer settings to the preferences.
    IPreferenceStore preferences = WellRendererPreferencePage.PREFERENCE_STORE;

    preferences.setValue(BORE_LINE_WIDTH, model.getBoreLineWidth());
    preferences.setValue(BORE_LINE_STYLE, model.getBoreLineStyle().getName());
    PreferenceConverter.setValue(preferences, BORE_LINE_COLOR, model.getBoreLineColor());
    preferences.setValue(TOP_HOLE_SYMBOL, model.getTopHoleSymbol().getName());
    preferences.setValue(TOP_HOLE_SYMBOL_SIZE, model.getTopHoleSymbolSize());
    PreferenceConverter.setValue(preferences, TOP_HOLE_SYMBOL_COLOR, model.getTopHoleSymbolColor());
    preferences.setValue(BOTTOM_HOLE_SYMBOL, model.getBottomHoleSymbol().getName());
    preferences.setValue(BOTTOM_HOLE_SYMBOL_SIZE, model.getBottomHoleSymbolSize());
    PreferenceConverter.setValue(preferences, BOTTOM_HOLE_SYMBOL_COLOR, model.getBottomHoleSymbolColor());
  }

}
