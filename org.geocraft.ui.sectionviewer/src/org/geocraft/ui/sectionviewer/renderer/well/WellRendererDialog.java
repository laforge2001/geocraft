/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.renderer.well;


import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.model.IModel;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.PreferencesModelDialog;


/**
 * Defines the dialog for editing the display settings of a <code>Well</code> renderer in the section viewer.
 */
public class WellRendererDialog extends PreferencesModelDialog implements IWellRendererConstants {

  private final WellRenderer _renderer;

  /**
   * The default constructor.
   * 
   * @param shell the parent shell.
   * @param title the dialog title.
   * @param renderer the renderer.
   */
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
    final IModelForm form = forms[0];

    FormSection section = form.addSection("Well Bore");
    section.addTextField(DISTANCE_TOLERANCE);
    section.addCheckboxField(SHOW_BORE_LABELS);
    section.addSpinnerField(BORE_LINE_WIDTH, 0, 100, 0, 1);
    section.addColorField(BORE_LINE_COLOR);

    section = form.addSection("Picks");
    section.addCheckboxField(SHOW_PICKS);
    section.addColorField(PICK_SYMBOL_COLOR);
    section.addCheckboxField(SHOW_PICK_LABELS);
    section.addColorField(PICK_LABEL_COLOR);
  }

  @Override
  protected void applySettings() {
    final WellRendererModel model = (WellRendererModel) _model;
    _renderer.updateRendererModel(model);
  }

  @Override
  public IModel createModel() {
    final WellRendererModel model = new WellRendererModel(_renderer.getSettingsModel());
    return model;
  }

  @Override
  protected void updatePreferences() {
    WellRendererModel model = (WellRendererModel) _model;

    // Update the default renderer settings to the preferences.
    IPreferenceStore preferences = WellRendererPreferencePage.PREFERENCE_STORE;

    preferences.setValue(DISTANCE_TOLERANCE, model.getDisplayTolerance());
    preferences.setValue(SHOW_BORE_LABELS, model.getShowBoreLabels());
    preferences.setValue(BORE_LINE_WIDTH, model.getBoreWidth());
    PreferenceConverter.setValue(preferences, BORE_LINE_COLOR, model.getBoreColor());

    preferences.setValue(SHOW_PICKS, model.getShowPicks());
    PreferenceConverter.setValue(preferences, PICK_SYMBOL_COLOR, model.getPickColor());
    preferences.setValue(SHOW_PICK_LABELS, model.getShowPickLabels());
    PreferenceConverter.setValue(preferences, PICK_LABEL_COLOR, model.getPickLabelColor());

    preferences.setValue(SHOW_LOGS, model.getShowLogs());
  }
}
