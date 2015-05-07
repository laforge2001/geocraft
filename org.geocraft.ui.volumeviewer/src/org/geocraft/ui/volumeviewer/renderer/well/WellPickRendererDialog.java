/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer.renderer.well;


import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.model.IModel;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.PreferencesModelDialog;


/**
 * Defines the dialog for editing the display settings of a <code>WellPick</code> renderer in the 3D viewer.
 */
public class WellPickRendererDialog extends PreferencesModelDialog implements IWellRendererConstants {

  private final WellPickRenderer _renderer;

  /**
   * The default constructor.
   * 
   * @param shell the parent shell.
   * @param title the dialog title.
   * @param renderer the renderer.
   */
  public WellPickRendererDialog(final Shell shell, final String title, final WellPickRenderer renderer) {
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
    final FormSection section = form.addSection("General");
    section.addSpinnerField(IWellRendererConstants.WELL_PICK_RADIUS, 0, 1000, 0, 1);
    section.addColorField(IWellRendererConstants.WELL_PICK_COLOR);
  }

  @Override
  protected void applySettings() {
    final WellPickRendererModel model = (WellPickRendererModel) _model;
    _renderer.updateRendererModel(model);
  }

  @Override
  public IModel createModel() {
    final WellPickRendererModel model = new WellPickRendererModel(_renderer.getSettingsModel());
    return model;
  }

  @Override
  protected void updatePreferences() {
    final WellPickRendererModel model = (WellPickRendererModel) _model;

    // Update the default renderer settings to the preferences.
    final IPreferenceStore preferences = WellRendererPreferencePage.PREFERENCE_STORE;

    preferences.setValue(WELL_PICK_RADIUS, model.getPickRadius());
    PreferenceConverter.setValue(preferences, WELL_PICK_COLOR, model.getPickColor());
  }
}
