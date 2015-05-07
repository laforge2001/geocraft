/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.chartviewer.renderer.polar;


import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.color.ColorBar;
import org.geocraft.core.color.ColorMapEvent;
import org.geocraft.core.color.ColorMapListener;
import org.geocraft.core.model.IModel;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.specification.GridGeometrySpecification;
import org.geocraft.core.repository.specification.ISpecification;
import org.geocraft.ui.color.ColorBarEditor;
import org.geocraft.ui.common.GridLayoutHelper;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.PreferencesModelDialog;
import org.geocraft.ui.form2.field.CheckboxField;
import org.geocraft.ui.form2.field.EntityComboField;
import org.geocraft.ui.form2.field.ScaleField;
import org.geocraft.ui.form2.field.TextField;


/**
 * Defines the dialog for editing the display settings of a <code>Grid</code> renderer in the map viewer.
 */
public class PolarChartRendererDialog extends PreferencesModelDialog implements ColorMapListener,
    IPolarChartRendererConstants {

  private static final int RED = 0;

  private static final int GREEN = 1;

  private static final int BLUE = 2;

  private final PolarChartRenderer _renderer;

  private ColorBarEditor _colorBarEditor;

  private final Grid3d _referenceGrid;

  private final PostStack3d _referenceVolume;

  private EntityComboField _secondaryGridField;

  private EntityComboField[] _rgbGridFields;

  private CheckboxField[] _rgbChannelFields;

  private TextField[][] _rgbRangeFields;

  private ScaleField _simpleBlendWeightingField;

  /**
   * The default constructor.
   * 
   * @param shell the parent shell.
   * @param title the dialog title.
   * @param renderer the renderer.
   */
  public PolarChartRendererDialog(final Shell shell, final String title, final PolarChartRenderer renderer, final Grid3d referenceGrid, final PostStack3d referenceVolume) {
    super(shell, title);
    _renderer = renderer;
    _referenceGrid = referenceGrid;
    _referenceVolume = referenceVolume;
  }

  @Override
  protected int getNumForms() {
    return 2;
  }

  @Override
  protected void buildModelForms(final IModelForm[] forms) {
    PolarChartRendererModel model = (PolarChartRendererModel) _model;
    IModelForm form = forms[0];
    FormSection section = form.addSection("General");
    section.addSpinnerField(TRANSPARENCY, 0, 100, 0, 1);
    section.addCheckboxField(SMOOTH_IMAGE);
    section.addCheckboxField(SHADED_RELIEF);

    section = form.addSection("Colors");
    section.addSpinnerField(PERCENTILE, 0, 100, 0, 1);
    Composite colorBarComposite = form.createComposite("Color Bar");
    colorBarComposite.setLayout(GridLayoutHelper.createLayout(1, false));
    _colorBarEditor = new ColorBarEditor(colorBarComposite, model.getColorBar());

    form = forms[1];
    section = form.addSection("Blending");
    section.addComboField(BLENDING_TYPE, PolarChartBlendingType.values());
    ISpecification filter = null;
    if (_referenceGrid != null) {
      filter = new GridGeometrySpecification(_referenceGrid.getGeometry());
    } else if (_referenceVolume != null) {
      filter = new GridGeometrySpecification(_referenceVolume.getSurvey());
    }
    _simpleBlendWeightingField = section.addScaleField(SIMPLE_BLEND_WEIGHTING, 0, 100, 1);

    // Add the fields for 2-grid weighted blending.
    if (filter != null) {
      _secondaryGridField = section.addEntityComboField(SECONDARY_GRID, filter);
    }

    // Add the fields for 3-grid RGB blending.
    _rgbRangeFields = new TextField[3][2];
    _rgbGridFields = new EntityComboField[3];
    _rgbChannelFields = new CheckboxField[3];
    _rgbChannelFields[RED] = section.addCheckboxField(RED_CHANNEL_FLAG);
    _rgbGridFields[RED] = section.addEntityComboField(RED_GRID, filter);
    _rgbRangeFields[RED][0] = section.addTextField(RED_GRID_START);
    _rgbRangeFields[RED][1] = section.addTextField(RED_GRID_END);
    _rgbChannelFields[GREEN] = section.addCheckboxField(GREEN_CHANNEL_FLAG);
    _rgbGridFields[GREEN] = section.addEntityComboField(GREEN_GRID, filter);
    _rgbRangeFields[GREEN][0] = section.addTextField(GREEN_GRID_START);
    _rgbRangeFields[GREEN][1] = section.addTextField(GREEN_GRID_END);
    _rgbChannelFields[BLUE] = section.addCheckboxField(BLUE_CHANNEL_FLAG);
    _rgbGridFields[BLUE] = section.addEntityComboField(BLUE_GRID, filter);
    _rgbRangeFields[BLUE][0] = section.addTextField(BLUE_GRID_START);
    _rgbRangeFields[BLUE][1] = section.addTextField(BLUE_GRID_END);
  }

  @Override
  public void propertyChanged(final String triggerKey) {
    super.propertyChanged(triggerKey);

    // exit if secondary grid field not used
    if (_secondaryGridField == null) {
      return;
    }

    PolarChartRendererModel model = (PolarChartRendererModel) _model;
    if (triggerKey != null) {
      if (triggerKey.equals(BLENDING_TYPE)) {
        PolarChartBlendingType blendingType = model.getBlendingType();
        switch (blendingType) {
          case NONE:
            _colorBarEditor.setEnabled(true);
            _secondaryGridField.setEnabled(false);
            for (int i = 0; i < 3; i++) {
              _rgbChannelFields[i].setEnabled(false);
              _rgbGridFields[i].setEnabled(false);
              for (int j = 0; j < 2; j++) {
                _rgbRangeFields[i][j].setEnabled(false);
              }
            }
            _simpleBlendWeightingField.setEnabled(false);
            break;
          case WEIGHTED:
            _colorBarEditor.setEnabled(true);
            _secondaryGridField.setEnabled(true);
            for (int i = 0; i < 3; i++) {
              _rgbChannelFields[i].setEnabled(false);
              _rgbGridFields[i].setEnabled(false);
              for (int j = 0; j < 2; j++) {
                _rgbRangeFields[i][j].setEnabled(false);
              }
            }
            _simpleBlendWeightingField.setEnabled(true);
            break;
          case RGB_BLENDING:
            _colorBarEditor.setEnabled(false);
            _secondaryGridField.setEnabled(false);
            for (int i = 0; i < 3; i++) {
              _rgbChannelFields[i].setEnabled(true);
              _rgbGridFields[i].setEnabled(true);
              for (int j = 0; j < 2; j++) {
                _rgbRangeFields[i][j].setEnabled(true);
              }
            }
            _simpleBlendWeightingField.setEnabled(false);
            break;
        }
      } else if (triggerKey.equals(RED_GRID) && model.getRedChannelGrid() != null) {
        Grid3d redGrid = model.getRedChannelGrid();
        model.setRedStartValue(redGrid.getMinValue());
        model.setRedEndValue(redGrid.getMaxValue());
      } else if (triggerKey.equals(GREEN_GRID) && model.getGreenChannelGrid() != null) {
        Grid3d greenGrid = model.getGreenChannelGrid();
        model.setGreenStartValue(greenGrid.getMinValue());
        model.setGreenEndValue(greenGrid.getMaxValue());
      } else if (triggerKey.equals(BLUE_GRID) && model.getBlueChannelGrid() != null) {
        Grid3d blueGrid = model.getBlueChannelGrid();
        model.setBlueStartValue(blueGrid.getMinValue());
        model.setBlueEndValue(blueGrid.getMaxValue());
      }
    }
  }

  @Override
  protected void applySettings() {
    PolarChartRendererModel model = (PolarChartRendererModel) _model;
    ColorBar colorBar = model.getColorBar();

    // reset values in case user just enters a return
    double startValue = _colorBarEditor.getStartValue();
    double endValue = _colorBarEditor.getEndValue();
    model.getColorBar().setStartValue(startValue);
    model.getColorBar().setEndValue(endValue);

    _colorBarEditor.setColorBar(colorBar);
    _renderer.updateRendererModel(model);
  }

  @Override
  public IModel createModel() {
    PolarChartRendererModel settingsModel = _renderer.getSettingsModel();
    PolarChartRendererModel model = new PolarChartRendererModel(settingsModel);
    ColorBar colorBar = new ColorBar(settingsModel.getColorBar());
    model.setColorBar(colorBar);
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
    _renderer.colorsChanged(event);
  }

  @Override
  protected void updatePreferences() {
    PolarChartRendererModel model = (PolarChartRendererModel) _model;

    // Update the default renderer settings to the preferences.
    IPreferenceStore preferences = PolarChartRendererPreferencePage.PREFERENCE_STORE;

    preferences.setValue(TRANSPARENCY, model.getTransparency());
    preferences.setValue(SMOOTH_IMAGE, model.getSmoothImage());
    preferences.setValue(SHADED_RELIEF, model.getShadedRelief());
    preferences.setValue(PERCENTILE, model.getPercentile());
    //preferences.setValue(COLOR_MAP, colorMap);
  }
}
