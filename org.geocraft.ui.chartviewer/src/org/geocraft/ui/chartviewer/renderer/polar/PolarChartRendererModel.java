/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.chartviewer.renderer.polar;


import java.beans.PropertyChangeEvent;

import org.eclipse.jface.preference.IPreferenceStore;
import org.geocraft.core.color.ColorBar;
import org.geocraft.core.color.ColorMapDescription;
import org.geocraft.core.color.map.IColorMap;
import org.geocraft.core.color.map.SpectrumColorMap;
import org.geocraft.core.common.math.MathUtil;
import org.geocraft.core.common.util.Labels;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.ColorBarProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.chartviewer.data.PolarChartData;


/**
* Defines the model of display settings for the Grid renderer in the map viewer.
*/
public final class PolarChartRendererModel extends Model implements IPolarChartRendererConstants {

  /** The transparency of the grid image (0-100%). */
  private final IntegerProperty _transparency;

  /** The clipping percentile (0-100%). */
  private final IntegerProperty _percentile;

  /** The flag for smoothing the grid image. */
  private final BooleanProperty _smoothImage;

  /** The flag for overlaying shaded relief on the grid image. */
  private final BooleanProperty _shadedRelief;

  /** The blending type for the grid image. */
  private final EnumProperty<PolarChartBlendingType> _blendingType;

  private final EntityProperty<Grid3d> _primaryGrid;

  private final EntityProperty<Grid3d> _secondaryGrid;

  private final IntegerProperty _simpleBlendWeighting;

  private final BooleanProperty _redChannelFlag;

  private final EntityProperty<Grid3d> _redGrid;

  private final FloatProperty _redStartValue;

  private final FloatProperty _redEndValue;

  private final BooleanProperty _greenChannelFlag;

  private final EntityProperty<Grid3d> _greenGrid;

  private final FloatProperty _greenStartValue;

  private final FloatProperty _greenEndValue;

  private final BooleanProperty _blueChannelFlag;

  private final EntityProperty<Grid3d> _blueGrid;

  private final FloatProperty _blueStartValue;

  private final FloatProperty _blueEndValue;

  /** The colorbar used for display the grid image. */
  private final ColorBarProperty _colorBar;

  /** The flag indicating whether to recompute start/end ranges based on clipping percentile. */
  private boolean _recomputePercentile = false;

  /**
   * The default constructor.
   */
  public PolarChartRendererModel() {
    super();

    // Initialize the default renderer settings from the preferences.
    IPreferenceStore preferences = PolarChartRendererPreferencePage.PREFERENCE_STORE;

    int transparency = preferences.getInt(TRANSPARENCY);
    boolean smoothImage = preferences.getBoolean(SMOOTH_IMAGE);
    boolean shadedRelief = preferences.getBoolean(SHADED_RELIEF);
    int percentile = preferences.getInt(PERCENTILE);
    String colorMapName = preferences.getString(COLOR_MAP);
    IColorMap colorMap = null;
    for (ColorMapDescription colorMapDesc : ServiceProvider.getColorMapService().getAll()) {
      if (colorMapName.equals(colorMapDesc.getName())) {
        colorMap = colorMapDesc.createMap();
      }
    }
    if (colorMap == null) {
      colorMap = new SpectrumColorMap();
    }

    _transparency = addIntegerProperty(TRANSPARENCY, transparency);
    _percentile = addIntegerProperty(PERCENTILE, percentile);
    _smoothImage = addBooleanProperty(SMOOTH_IMAGE, smoothImage);
    _shadedRelief = addBooleanProperty(SHADED_RELIEF, shadedRelief);
    _blendingType = addEnumProperty(BLENDING_TYPE, PolarChartBlendingType.class, PolarChartBlendingType.NONE);
    _primaryGrid = addEntityProperty(PRIMARY_GRID, Grid3d.class);
    _secondaryGrid = addEntityProperty(SECONDARY_GRID, Grid3d.class);
    _simpleBlendWeighting = addIntegerProperty(SIMPLE_BLEND_WEIGHTING, 50);
    _redChannelFlag = addBooleanProperty(RED_CHANNEL_FLAG, true);
    _redGrid = addEntityProperty(RED_GRID, Grid3d.class);
    _redStartValue = addFloatProperty(RED_GRID_START, 0);
    _redEndValue = addFloatProperty(RED_GRID_END, 0);
    _greenChannelFlag = addBooleanProperty(GREEN_CHANNEL_FLAG, true);
    _greenGrid = addEntityProperty(GREEN_GRID, Grid3d.class);
    _greenStartValue = addFloatProperty(GREEN_GRID_START, 0);
    _greenEndValue = addFloatProperty(GREEN_GRID_END, 0);
    _blueChannelFlag = addBooleanProperty(BLUE_CHANNEL_FLAG, true);
    _blueGrid = addEntityProperty(BLUE_GRID, Grid3d.class);
    _blueStartValue = addFloatProperty(BLUE_GRID_START, 0);
    _blueEndValue = addFloatProperty(BLUE_GRID_END, 0);
    double minValue = 0;
    double maxValue = 100;
    double[] labels = Labels.computeLabels(minValue, maxValue, 10);
    _colorBar = addColorBarProperty(COLOR_BAR, new ColorBar(64, colorMap, minValue, maxValue, labels[2]));
  }

  /**
   * The copy constructor.
   */
  public PolarChartRendererModel(final PolarChartRendererModel model) {
    this();
    updateFrom(model);
  }

  /**
   * Updates the color bar range (start,end,delta) based on a grid.
   * 
   * @param grid the grid from which to get the min and max values.
   */
  public void setColorBarRange(final PolarChartData polarChartData) {
    double minValue = polarChartData.getAttributeMinimum();
    double maxValue = polarChartData.getAttributeMaximum();
    double[] labels = Labels.computeLabels(minValue, maxValue, 10);
    _colorBar.get().setRange(minValue, maxValue, labels[2]);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    super.propertyChange(event);
    String propertyName = event.getPropertyName();
    if (propertyName.equals(PERCENTILE)) {
      _recomputePercentile = true;
    }
  }

  /**
   * Gets the transparency to use for the rendering the grid.
   * 
   * @return the transparency (0-100).
   */
  public int getTransparency() {
    return _transparency.get();
  }

  /**
   * Sets the transparency to use for the rendering the grid.
   * 
   * @param transparency the transparency (0-100).
   */
  public void setTransparency(final int transparency) {
    _transparency.set(transparency);
  }

  /**
   * Sets the clipping percentile to use for rendering the grid.
   * 
   * @return the clipping percentile (0-100).
   */
  public int getPercentile() {
    return _percentile.get();
  }

  /**
   * Sets the clipping percentile to use for the rendering the grid.
   * 
   * @param percentile the clipping percentile (0-100).
   */
  public void setPercentile(final int percentile) {
    _percentile.set(percentile);
  }

  /**
   * Gets the image smoothing flag to use for rendering the grid.
   * 
   * @return the image smoothing flag.
   */
  public boolean getSmoothImage() {
    return _smoothImage.get();
  }

  /**
   * Sets the image smoothing flag to use for rendering the grid.
   * 
   * @param smoothImage the smoothing flag.
   */
  public void setSmoothImage(final boolean smoothImage) {
    _smoothImage.set(smoothImage);
  }

  /**
   * Gets the shaded relief flag to use for rendering the grid.
   * 
   * @return <i>true</i> to apply shaded relief; otherwise <i>false</i>.
   */
  public boolean getShadedRelief() {
    return _shadedRelief.get();
  }

  /**
   * Sets the shaded relief flag to use for rendering the grid.
   * 
   * @param shadedRelief the shaded relief flag (<i>true</i> to apply shaded relief; otherwise <i>false</i>).
   */
  public void setShadedRelief(final boolean shadedRelief) {
    _shadedRelief.set(shadedRelief);
  }

  /**
   * Gets the blending type to use for rendering the grid.
   * 
   * @return the blending type.
   */
  public PolarChartBlendingType getBlendingType() {
    return _blendingType.get();
  }

  /**
   * Sets the blending type to use for rendering the grid.
   * 
   * @param blendingType the blending type.
   */
  public void setBlendingType(final PolarChartBlendingType blendingType) {
    _blendingType.set(blendingType);
  }

  /**
   * Gets the primary grid.
   * 
   * @return the primary grid.
   */
  public Grid3d getPrimaryGrid() {
    return _primaryGrid.get();
  }

  /**
   * Sets the primary grid.
   * 
   * @param grid the primary grid.
   */
  public void setPrimaryGrid(final Grid3d grid) {
    _primaryGrid.set(grid);
  }

  /**
   * Gets the secondary grid.
   * 
   * @return the secondary grid.
   */
  public Grid3d getSecondaryGrid() {
    return _secondaryGrid.get();
  }

  /**
   * Gets the weighting to use for simple blending of 2 grids.
   * 
   * @return the weighting factor (0-100).
   */
  public int getSimpleBlendWeighting() {
    return _simpleBlendWeighting.get();
  }

  /**
   * Gets the grid to use for the red channel in RGB blending.
   * 
   * @return the red channel grid.
   */
  public Grid3d getRedChannelGrid() {
    return _redGrid.get();
  }

  /**
   * Gets the flag for using for the red channel in RGB blending.
   * 
   * @return <i>true</i> if using the red channel; <i>false</i> if not..
   */
  public boolean getRedChannelFlag() {
    return _redChannelFlag.get();
  }

  /**
   * Gets the flag for using for the red channel in RGB blending.
   * 
   * @return the red channel flag.
   */
  public void setRedChannelFlag(final boolean flag) {
    _redChannelFlag.set(flag);
  }

  /**
   * Gets the start value for the red channel.
   * 
   * @return the red channel start value.
   */
  public float getRedStartValue() {
    return _redStartValue.get();
  }

  /**
   * Sets the start value for the red channel.
   * 
   * @param value the red channel start value.
   */
  public void setRedStartValue(final float value) {
    _redStartValue.set(value);
  }

  /**
   * Gets the end value for the red channel.
   * 
   * @return the red channel end value.
   */
  public float getRedEndValue() {
    return _redEndValue.get();
  }

  /**
   * Sets the end value for the red channel.
   * 
   * @param value the red channel end value.
   */
  public void setRedEndValue(final float value) {
    _redEndValue.set(value);
  }

  /**
   * Gets the grid to use for the green channel in RGB blending.
   * 
   * @return the green channel grid.
   */
  public Grid3d getGreenChannelGrid() {
    return _greenGrid.get();
  }

  /**
   * Gets the flag for using for the green channel in RGB blending.
   * 
   * @return <i>true</i> if using the green channel; <i>false</i> if not..
   */
  public boolean getGreenChannelFlag() {
    return _greenChannelFlag.get();
  }

  /**
   * Sets the flag for using the green channel in RGB blending.
   * 
   * @param <i>true</i> to use the green channel; otherwise <i>false</i>.
   */
  public void setGreenChannelFlag(final boolean flag) {
    _greenChannelFlag.set(flag);
  }

  /**
   * Gets the start value for the green channel.
   * 
   * @return the green channel start value.
   */
  public float getGreenStartValue() {
    return _greenStartValue.get();
  }

  /**
   * Sets the start value for the green channel.
   * 
   * @param value the green channel start value.
   */
  public void setGreenStartValue(final float value) {
    _greenStartValue.set(value);
  }

  /**
   * Gets the end value for the green channel.
   * 
   * @return the green channel end value.
   */
  public float getGreenEndValue() {
    return _greenEndValue.get();
  }

  /**
   * Sets the end value for the green channel.
   * 
   * @param value the green channel end value.
   */
  public void setGreenEndValue(final float value) {
    _greenEndValue.set(value);
  }

  /**
   * Gets the grid to use for the blue channel in RGB blending.
   * 
   * @return the blue channel grid.
   */
  public Grid3d getBlueChannelGrid() {
    return _blueGrid.get();
  }

  /**
   * Gets the flag for using for the blue channel in RGB blending.
   * 
   * @return <i>true</i> if using the blue channel; <i>false</i> if not..
   */
  public boolean getBlueChannelFlag() {
    return _blueChannelFlag.get();
  }

  /**
   * Sets the flag for using the blue channel in RGB blending.
   * 
   * @param <i>true</i> to use the blue channel; otherwise <i>false</i>.
   */
  public void setBlueChannelFlag(final boolean flag) {
    _blueChannelFlag.set(flag);
  }

  /**
   * Gets the start value for the blue channel.
   * 
   * @return the blue channel start value.
   */
  public float getBlueStartValue() {
    return _blueStartValue.get();
  }

  /**
   * Sets the start value for the blue channel.
   * 
   * @param value the blue channel start value.
   */
  public void setBlueStartValue(final float value) {
    _blueStartValue.set(value);
  }

  /**
   * Gets the end value for the blue channel.
   * 
   * @return the blue channel end value.
   */
  public float getBlueEndValue() {
    return _blueEndValue.get();
  }

  /**
   * Sets the start value for the blue channel.
   * 
   * @param value the blue channel start value.
   */
  public void setBlueEndValue(final float value) {
    _blueEndValue.set(value);
  }

  /**
   * Gets the color bar to use rendering the grid.
   * 
   * @return the color bar.
   */
  public ColorBar getColorBar(final PolarChartData polarChartData) {
    int percentile = _percentile.get();
    ColorBar colorBar = _colorBar.get();
    if (_recomputePercentile && percentile > 0) {
      float[] clipped = MathUtil.computePercentiles(polarChartData.getValues(), polarChartData.getNullValue(),
          percentile);
      colorBar.setStartValue(clipped[0]);
      colorBar.setEndValue(clipped[1]);
      _recomputePercentile = false;
    } else if (_recomputePercentile && percentile == 0) {
      colorBar.setStartValue(polarChartData.getAttributeMinimum());
      colorBar.setEndValue(polarChartData.getAttributeMaximum());
      _recomputePercentile = false;
    }
    return colorBar;
  }

  /**
   * Gets the color bar to use when rendering the grid.
   * 
   * @return the color bar.
   */
  public ColorBar getColorBar() {
    return _colorBar.get();
  }

  /**
   * Sets the color bar to use when rendering the grid.
   * 
   * @param colorBar the color bar.
   */
  public void setColorBar(final ColorBar colorBar) {
    _colorBar.set(colorBar);
  }

  public void validate(final IValidation results) {
    if (_transparency.get() < 0 || _transparency.get() > 100) {
      results.error(_transparency, "Transparency must be in range 0-100.");
    }
    if (_percentile.get() < 0 || _percentile.get() > 100) {
      results.error(_percentile, "Percentile must be in range 0-100.");
    }
    //    if (_primaryGrid.isNull()) {
    //      results.error(_primaryGrid, "No primary grid specified.");
    //    }
    if (_blendingType.isNull()) {
      results.error(_blendingType, "No blending type specified.");
    } else {
      switch (_blendingType.get()) {
        case NONE:
          break;
        case WEIGHTED:
          if (_secondaryGrid.isNull()) {
            results.error(_secondaryGrid, "No secondary grid specified.");
          }
          int simpleBlendWeighting = _simpleBlendWeighting.get();
          if (simpleBlendWeighting < 0 || simpleBlendWeighting > 100) {
            results.error(_simpleBlendWeighting, "Invalid blend weighting: " + simpleBlendWeighting);
          }
          break;
        case RGB_BLENDING:
          if (_redChannelFlag.get() && _redGrid.isNull()) {
            results.error(_redGrid, "No Red grid specified.");
          }
          if (_greenChannelFlag.get() && _greenGrid.isNull()) {
            results.error(_greenGrid, "No Green grid specified.");
          }
          if (_blueChannelFlag.get() && _blueGrid.isNull()) {
            results.error(_blueGrid, "No Blue grid specified.");
          }
          break;
      }
    }
  }

  public Object getReferenceGrid() {
    // TODO Auto-generated method stub
    return null;
  }
}
