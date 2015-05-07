package org.geocraft.ui.sectionviewer.renderer.grid;


import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.model.IModel;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.PreferencesModelDialog;
import org.geocraft.ui.plot.defs.LineStyle;


/**
 * Settings dialog for the section view grid renderer.
 */
public class Grid2dRendererDialog extends PreferencesModelDialog implements IGridRendererConstants {

  /** The grid renderer. */
  private final Grid2dRenderer _renderer;

  public Grid2dRendererDialog(final Shell shell, final String title, final Grid2dRenderer renderer) {
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
    FormSection section = form.addSection("Intersection Attributes", false);

    section.addComboField(LINE_STYLE, LineStyle.values());
    section.addTextField(LINE_WIDTH);
    section.addColorField(LINE_COLOR);
  }

  @Override
  protected IModel createModel() {
    return new Grid2dRendererModel(_renderer.getSettingsModel());
  }

  @Override
  protected void applySettings() {
    // Apply the settings to the renderer.
    _renderer.updateSettings((Grid2dRendererModel) _model);
  }

  @Override
  protected void updatePreferences() {
    Grid2dRendererModel model = (Grid2dRendererModel) _model;

    // Update the default renderer settings to the preferences.
    IPreferenceStore preferences = GridRendererPreferencePage.PREFERENCE_STORE;

    preferences.setValue(LINE_WIDTH, model.getLineWidth());
    preferences.setValue(LINE_STYLE, model.getLineStyle().getName());
    PreferenceConverter.setValue(preferences, LINE_COLOR, model.getLineColor());
  }

}
