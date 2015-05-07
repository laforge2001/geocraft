/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer.renderer.grid;


import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.color.ColorMapEvent;
import org.geocraft.core.color.ColorMapListener;
import org.geocraft.core.model.IModel;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.ui.color.ColorBarEditor;
import org.geocraft.ui.common.GridLayoutHelper;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.PreferencesModelDialog;


/**
 * Defines the dialog for editing the display properties
 * of a <code>Grid3d</code> renderer in a 3D viewer.
 */
public class Grid3dRendererDialog extends PreferencesModelDialog implements ColorMapListener, IGridRendererConstants {

  /** The grid renderer. */
  private final Grid3dRenderer _renderer;

  /** The grid being rendered. */
  private final Grid3d _grid;

  /** The color bar editor. */
  protected ColorBarEditor _colorBarEditor;

  /**
   * Constructs a dialog for editing the display properties of
   * a <code>Grid3d</code> renderer.
   * 
   * @param shell the parent shell.
   * @param title the dialog title.
   * @param renderer the renderer.
   * @param grid the grid being rendered.
   */
  public Grid3dRendererDialog(final Shell shell, final String title, final Grid3dRenderer renderer, final Grid3d grid) {
    super(shell, title);
    _renderer = renderer;
    _grid = grid;
  }

  @Override
  protected int getNumForms() {
    return 1;
  }

  @Override
  protected void buildModelForms(final IModelForm[] forms) {
    final Grid3dRendererModel model = (Grid3dRendererModel) _model;
    final IModelForm form = forms[0];
    final FormSection section = form.addSection("General");
    section.addSpinnerField(TRANSPARENCY, 0, 100, 0, 1);
    section.addSpinnerField(PERCENTILE, 0, 100, 0, 1);
    section.addRadioGroupField(SMOOTHING_METHOD, SmoothingMethod.values());
    section.addCheckboxField(SHOW_MESH);
    section.addEntityComboField(RGB_GRID, Grid3d.class).setFilter(new Grid3dSpecification(_grid));

    final Composite colorBarComposite = form.createComposite("Color Bar");
    colorBarComposite.setLayout(GridLayoutHelper.createLayout(1, false));
    _colorBarEditor = new ColorBarEditor(colorBarComposite, model.getColorBar());
  }

  @Override
  protected void applySettings() {
    final Grid3dRendererModel model = (Grid3dRendererModel) _model;
    _colorBarEditor.setColorBar(model.getColorBar());
    _renderer.updateRendererModel(model);
  }

  @Override
  public IModel createModel() {
    final Grid3dRendererModel model = new Grid3dRendererModel(_renderer.getSettingsModel());
    model.getColorBar().addColorMapListener(this);
    return model;
  }

  @Override
  public boolean close() {
    if (_colorBarEditor != null) {
      _colorBarEditor.dispose();
    }
    return super.close();
  }

  public void colorsChanged(final ColorMapEvent event) {
    _renderer.updateColors(event.getColorMapModel());
  }

  @Override
  protected void updatePreferences() {
    final Grid3dRendererModel model = (Grid3dRendererModel) _model;

    // Update the default renderer settings to the preferences.
    final IPreferenceStore preferences = Grid3dRendererPreferencePage.PREFERENCE_STORE;

    preferences.setValue(TRANSPARENCY, model.getTransparency());
    preferences.setValue(SMOOTHING_METHOD, model.getSmoothingMethod().getName());
    preferences.setValue(SHOW_MESH, model.getShowMesh());
    preferences.setValue(PERCENTILE, model.getPercentile());
    //preferences.setValue(COLOR_MAP, colorMap);
  }
}
