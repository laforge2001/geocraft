/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer.renderer.seismic;


import java.beans.PropertyChangeEvent;

import org.eclipse.jface.preference.IPreferenceStore;
import org.geocraft.core.color.ColorBar;
import org.geocraft.core.color.ColorMapDescription;
import org.geocraft.core.color.map.GrayscaleColorMap;
import org.geocraft.core.color.map.IColorMap;
import org.geocraft.core.color.map.SeismicColorMap;
import org.geocraft.core.common.math.AGC;
import org.geocraft.core.common.math.MathUtil;
import org.geocraft.core.common.util.Labels;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.ColorBarProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.service.ServiceProvider;


/**
 * The model of display properties for rendering a seismic dataset in 3D view.
 */
public abstract class SeismicDatasetRendererModel extends Model implements ISeismicDatasetRendererConstants {

  /** The key constant for the grid intersections property. */
  public static final String GRID_INTERSECTIONS = "Show Grid Intersections";

  /** The trace interpolation property. */
  private final EnumProperty<InterpolationMethod> _interpolationMethod;

  /** The trace interpolation property. */
  private final EnumProperty<NormalizationMethod> _normalizationMethod;

  /** The color opacity (0-100) property. */
  private final IntegerProperty _transparency;

  /** The clipping percentile property. */
  private final IntegerProperty _percentile;

  /** The polarity reversal property. */
  private final BooleanProperty _reversePolarity;

  /** The color bar property. */
  private final ColorBarProperty _colorBar;

  /** The grid intersections property. */
  private final BooleanProperty _showGridIntersects;

  protected Unit _dataUnit;

  /** The flag indicating whether to recompute start/end ranges based on clipping percentile. */
  private boolean _recomputePercentile = true;

  /**
   * Constructs the renderer model with default settings.
   */
  public SeismicDatasetRendererModel() {
    // Initialize the default renderer settings from the preferences.
    final IPreferenceStore preferences = SeismicDatasetRendererPreferencePage.PREFERENCE_STORE;

    final String interpMethodStr = preferences.getString(INTERPOLATION_METHOD);
    final InterpolationMethod interpMethod = InterpolationMethod.lookup(interpMethodStr);
    final String normMethodStr = preferences.getString(NORMALIZATION_METHOD);
    final NormalizationMethod normMethod = NormalizationMethod.lookup(normMethodStr);
    final int percentile = preferences.getInt(PERCENTILE);
    final int transparency = preferences.getInt(TRANSPARENCY);
    final boolean reversePolarity = preferences.getBoolean(REVERSE_POLARITY);
    final String colorMapName = preferences.getString(OTHER_COLOR_MAP);
    IColorMap colorMap = null;
    for (final ColorMapDescription colorMapDesc : ServiceProvider.getColorMapService().getAll()) {
      if (colorMapName.equals(colorMapDesc.getName())) {
        colorMap = colorMapDesc.createMap();
      }
    }
    if (colorMap == null) {
      colorMap = new SeismicColorMap();
    }
    final boolean agcApply = preferences.getBoolean(AGC_APPLY);
    final String agcTypeStr = preferences.getString(AGC_TYPE);
    final AGC.Type agcType = AGC.Type.lookup(agcTypeStr);
    final float agcWindow = preferences.getFloat(AGC_WINDOW_LENGTH);
    final boolean gainApply = preferences.getBoolean(GEOMETRIC_GAIN_APPLY);
    final float gainT0 = preferences.getFloat(GEOMETRIC_GAIN_T0);
    final float gainN = preferences.getFloat(GEOMETRIC_GAIN_N);
    final float gainTmax = preferences.getFloat(GEOMETRIC_GAIN_TMAX);

    _interpolationMethod = addEnumProperty(INTERPOLATION_METHOD, InterpolationMethod.class, interpMethod);
    _normalizationMethod = addEnumProperty(NORMALIZATION_METHOD, NormalizationMethod.class, normMethod);
    _transparency = addIntegerProperty(TRANSPARENCY, transparency);
    _percentile = addIntegerProperty(PERCENTILE, percentile);
    _reversePolarity = addBooleanProperty(REVERSE_POLARITY, reversePolarity);
    _colorBar = addColorBarProperty(COLOR_BAR, new ColorBar(64, colorMap, 0, 100, 10));
    _colorBar.get().setReversedRange(true);
    _showGridIntersects = addBooleanProperty(GRID_INTERSECTIONS, true);
    _dataUnit = Unit.SEISMIC_AMPLITUDE;
  }

  public Unit getDataUnit() {
    return _dataUnit;
  }

  public void setDataUnit(final Unit dataUnit) {
    _dataUnit = dataUnit;

    // Initialize the default renderer settings from the preferences.
    final IPreferenceStore preferences = SeismicDatasetRendererPreferencePage.PREFERENCE_STORE;

    String colorMapName = preferences.getString(OTHER_COLOR_MAP);

    final Domain dataDomain = dataUnit.getDomain();
    final int numColors = 64;
    double startValue = -100;
    double endValue = 100;
    int percentile = 0;
    if (dataUnit.equals(Unit.SEISMIC_AMPLITUDE)) {
      _normalizationMethod.set(NormalizationMethod.BY_MAXIMUM);
      percentile = 1;
      colorMapName = preferences.getString(SEISMIC_COLOR_MAP);
      startValue = -5000;
      endValue = 5000;
    } else if (dataDomain.equals(Domain.VELOCITY)) {
      _normalizationMethod.set(NormalizationMethod.BY_LIMITS);
      colorMapName = preferences.getString(VELOCITY_COLOR_MAP);
      if (dataUnit.equals(Unit.FEET_PER_SECOND)) {
        startValue = 5000;
        endValue = 15000;
      } else if (dataUnit.equals(Unit.METERS_PER_SECOND)) {
        startValue = 1500;
        endValue = 5000;
      }
    } else {
      startValue = -5000;
      endValue = 5000;
    }

    // Lookup the color map based on its name.
    IColorMap colorMap = null;
    for (final ColorMapDescription colorMapDesc : ServiceProvider.getColorMapService().getAll()) {
      if (colorMapName.equals(colorMapDesc.getName())) {
        colorMap = colorMapDesc.createMap();
      }
    }
    // Default to grayscale if no match found.
    if (colorMap == null) {
      colorMap = new GrayscaleColorMap();
    }

    final Labels labels = new Labels(startValue, endValue, 10);
    _colorBar.set(new ColorBar(numColors, colorMap, startValue, endValue, labels.getIncrement()));
    _colorBar.get().setReversedRange(true);
    setValueObject(PERCENTILE, percentile);
  }

  /**
   * Gets the color bar for rendering.
   * 
   * @return the color bar.
   */
  public final ColorBar getColorBar() {
    return _colorBar.get();
  }

  /**
   * Sets the color bar for rendering.
   * 
   * @param colorBar the color bar.
   */
  public final void setColorBar(final ColorBar colorBar) {
    _colorBar.set(colorBar);
  }

  /**
   * Gets the color bar for rendering, rescaled based on normalization and percentile.
   * 
   * @param normalization the normalization method.
   * @param values the array of data values.
   * @param nullValue the value representing null.
   * @param minValue the minimum value.
   * @param maxValue the maximum value.
   * @return the rescaled color bar.
   */
  public final ColorBar getColorBar(final NormalizationMethod normalization, final float[] values,
      final float nullValue, final float minValue, final float maxValue) {
    //System.out.println("GETTING COLORBAR " + minValue + " " + maxValue + " " + _recomputePercentile + " " + _percentile
    //    + " " + normalization);
    if (_recomputePercentile && _percentile.get() > 0 && normalization.equals(NormalizationMethod.BY_MAXIMUM)) {
      float[] clipped = MathUtil.computePercentiles(values, nullValue, _percentile.get());
      if (MathUtil.isEqual(clipped[0], 0.0f) && MathUtil.isEqual(clipped[1], 0.0f)) {
        _percentile.set(0);
        clipped = MathUtil.computePercentiles(values, nullValue, _percentile.get());
      }
      //System.out.println("Clipped: " + clipped[0] + " " + clipped[1] + " " + minValue + " " + maxValue);
      final float absmax = Math.max(Math.abs(clipped[0]), Math.abs(clipped[1]));
      clipped[0] = -absmax;
      clipped[1] = absmax;
      _colorBar.get().setStartValue(clipped[0]);
      _colorBar.get().setEndValue(clipped[1]);
      _recomputePercentile = false;
    } else if (_recomputePercentile && _percentile.get() == 0) {
      _colorBar.get().setStartValue(minValue);
      _colorBar.get().setEndValue(maxValue);
      _recomputePercentile = false;
    }
    return _colorBar.get();
  }

  /**
   * Gets the percentile for color bar scaling.
   * 
   * @return the percentile.
   */
  public final int getPercentile() {
    return _percentile.get();
  }

  /**
   * Gets the interpolation method.
   * 
   * @return the interpolation method.
   */
  public final InterpolationMethod getInterpolationMethod() {
    return _interpolationMethod.get();
  }

  /**
   * Gets the normalization method.
   * 
   * @return the normalization method.
   */
  public final NormalizationMethod getNormalizationMethod() {
    return _normalizationMethod.get();
  }

  /**
   * Gets the transparency value (0-100).
   * 
   * @return the transparency value.
   */
  public final int getTransparency() {
    return _transparency.get();
  }

  /**
   * Gets the polarity reversal flag.
   * 
   * @return <i>true</i> to reverse polarity; otherwise <i>false</i>.
   */
  public boolean getReversePolarity() {
    return _reversePolarity.get();
  }

  /**
   * Gets the grid intersections flag.
   * 
   * @return <i>true</i> to show grids; otherwise <i>false</i>.
   */
  public boolean getShowGridIntersections() {
    return _showGridIntersects.get();
  }

  public void setRecomputePercentile() {
    _recomputePercentile = true;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    super.propertyChange(event);
    final String triggerKey = event.getPropertyName();
    if (triggerKey == null || triggerKey.length() == 0) {
      return;
    }
    if (triggerKey.equals(PERCENTILE)) {
      _recomputePercentile = true;
    }
  }

  public void validate(final IValidation results) {
    // Validate the transparency is between 0 and 100.
    if (_transparency.get() < 0 || _transparency.get() > 100) {
      results.error(TRANSPARENCY, " transparency must be in the range 0 to 100");
    }

    // Validate the percentile is between 0 and 100.
    if (_percentile.get() < 0 || _percentile.get() > 100) {
      results.error(PERCENTILE, " percentile must be in the range 0 to 100");
    }
  }

}
