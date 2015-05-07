/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.mapviewer.renderer.grid;


import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.color.ColorMapEvent;
import org.geocraft.core.color.ColorMapListener;
import org.geocraft.core.model.IModel;
import org.geocraft.ui.color.ColorBarEditor;
import org.geocraft.ui.common.GridLayoutHelper;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.PreferencesModelDialog;


public class Grid2dRendererDialog extends PreferencesModelDialog implements ColorMapListener, IGridRendererConstants {

  /** The current grid renderer. */
  private final Grid2dRenderer _renderer;

  /** The grid color bar editor. */
  protected ColorBarEditor _editor;

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

    FormSection generalSection = form.addSection("General", false);

    generalSection.addSpinnerField(Grid2dRendererModel.TRANSPARENCY, 0, 100, 0, 1);

    Composite composite = form.createComposite("Color Bar", false);
    composite.setLayout(new GridLayout());
    _editor = new ColorBarEditor(composite, ((Grid2dRendererModel) _model).getColorBar());
    ((Grid2dRendererModel) _model).getColorBar().addColorMapListener(this);
    GridData layoutData = GridLayoutHelper.createLayoutData(true, true, SWT.FILL, SWT.FILL, 2, 1);
    composite.setLayoutData(layoutData);
  }

  @Override
  protected IModel createModel() {
    return new Grid2dRendererModel(_renderer.getSettingsModel());
  }

  @Override
  protected void applySettings() {
    // Apply the updated settings to the renderer.
    _renderer.updateSettings((Grid2dRendererModel) _model);
  }

  public void colorsChanged(final ColorMapEvent event) {
    // Immediately apply the color changes to the renderer.
    _renderer.colorsChanged(event);
  }

  @Override
  protected void updatePreferences() {
    Grid2dRendererModel model = (Grid2dRendererModel) _model;

    // Update the default renderer settings to the preferences.
    IPreferenceStore preferences = GridRendererPreferencePage.PREFERENCE_STORE;

    preferences.setValue(TRANSPARENCY, model.getTransparency());
  }
}
