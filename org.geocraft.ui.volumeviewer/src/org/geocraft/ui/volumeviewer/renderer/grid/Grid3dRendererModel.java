/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer.renderer.grid;


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
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.service.ServiceProvider;


/**
 * Defines the model of properties used to renderer a <code>Grid3d</code> entity
 * in the 3D viewer.
 */
public final class Grid3dRendererModel extends Model implements IGridRendererConstants {

  /** The transparency of the grid image (0-100%). */
  private final IntegerProperty _transparency;

  /** The clipping percentile (0-100%). */
  private final IntegerProperty _percentile;

  /** The method to use for smoothing the grid image. */
  private final EnumProperty<SmoothingMethod> _smoothingMethod;

  /** The flag for showing the surface grid mesh. */
  private final BooleanProperty _showMesh;

  /** The grid to use for coloring the grid image. */
  private final EntityProperty<Grid3d> _rgbGrid;

  /** The colorbar used for display the grid image. */
  private final ColorBarProperty _colorBar;

  /** The flag indicating whether to recompute start/end ranges based on clipping percentile. */
  private boolean _recomputePercentile = false;

  /**
   * Constructs a renderer model with default settings.
   */
  public Grid3dRendererModel() {
    super();

    // Initialize the default renderer settings from the preferences.
    final IPreferenceStore preferences = Grid3dRendererPreferencePage.PREFERENCE_STORE;

    final int transparency = preferences.getInt(TRANSPARENCY);
    final String smoothMethodStr = preferences.getString(SMOOTHING_METHOD);
    final SmoothingMethod smoothMethod = SmoothingMethod.lookup(smoothMethodStr);
    final boolean showMesh = preferences.getBoolean(SHOW_MESH);
    final int percentile = preferences.getInt(PERCENTILE);
    final String colorMapName = preferences.getString(COLOR_MAP);
    IColorMap colorMap = null;
    for (final ColorMapDescription colorMapDesc : ServiceProvider.getColorMapService().getAll()) {
      if (colorMapName.equals(colorMapDesc.getName())) {
        colorMap = colorMapDesc.createMap();
      }
    }
    if (colorMap == null) {
      colorMap = new SpectrumColorMap();
    }
    _transparency = addIntegerProperty(TRANSPARENCY, transparency);
    _percentile = addIntegerProperty(PERCENTILE, percentile);
    _smoothingMethod = addEnumProperty(SMOOTHING_METHOD, SmoothingMethod.class, smoothMethod);
    _showMesh = addBooleanProperty(SHOW_MESH, showMesh);
    _rgbGrid = addEntityProperty(RGB_GRID, Grid3d.class);
    final double minValue = 0;
    final double maxValue = 100;
    final double[] labels = Labels.computeLabels(minValue, maxValue, 10);
    _colorBar = addColorBarProperty(COLOR_BAR, new ColorBar(64, colorMap, minValue, maxValue, labels[2]));
  }

  /**
   * Constructs a renderer model, copied from another.
   * 
   * @param model the model from which to copy properties.
   */
  public Grid3dRendererModel(final Grid3dRendererModel model) {
    this();
    updateFrom(model);
  }

  /**
   * Updates the color bar range (start,end,delta) based on a grid.
   * 
   * @param grid the grid from which to get the min and max values.
   */
  public final void setColorBarRange(final Grid3d grid) {
    final double minValue = grid.getMinValue();
    final double maxValue = grid.getMaxValue();
    final double[] labels = Labels.computeLabels(minValue, maxValue, 10);
    _colorBar.get().setRange(minValue, maxValue, labels[2]);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    super.propertyChange(event);
    if (event.getPropertyName().equals(PERCENTILE)) {
      _recomputePercentile = true;
    } else if (event.getPropertyName().equals(RGB_GRID)) {
      if (!_rgbGrid.isNull()) {
        final Grid3d hueGrid = _rgbGrid.get();
        final double minValue = hueGrid.getMinValue();
        final double maxValue = hueGrid.getMaxValue();
        _colorBar.get().setRange(minValue, maxValue, 1);
      }
    }
  }

  /**
   * Gets the transparency to use for the rendering the grid.
   * 
   * @return the transparency (0-100).
   */
  public final int getTransparency() {
    return _transparency.get();
  }

  /**
   * Sets the transparency to use for the rendering the grid.
   * 
   * @param transparency the transparency (0-100).
   */
  public final void setTransparency(final int transparency) {
    _transparency.set(transparency);
  }

  /**
   * Sets the clipping percentile to use for rendering the grid.
   * 
   * @return the clipping percentile (0-100).
   */
  public final int getPercentile() {
    return _percentile.get();
  }

  /**
   * Sets the clipping percentile to use for the rendering the grid.
   * 
   * @param percentile the clipping percentile (0-100).
   */
  public final void setPercentile(final int percentile) {
    _percentile.set(percentile);
  }

  /**
   * Gets the method to use for smoothing the grid image.
   * 
   * @return the smoothing method.
   */
  public final SmoothingMethod getSmoothingMethod() {
    return _smoothingMethod.get();
  }

  /**
   * Sets the method to use for smoothing the grid image.
   * 
   * @param smoothMethod the smoothing method.
   */
  public final void setSmoothingMethod(final SmoothingMethod smoothMethod) {
    _smoothingMethod.set(smoothMethod);
  }

  /**
   * Gets the show mesh flag to use for rendering the grid.
   * 
   * @return <i>true</i> to show mesh; otherwise <i>false</i>.
   */
  public final boolean getShowMesh() {
    return _showMesh.get();
  }

  /**
   * Sets the show mesh flag to use for rendering the grid.
   * 
   * @param showMesh the show mesh flag (<i>true</i> to show mesh; otherwise <i>false</i>).
   */
  public final void setShowMesh(final boolean showMesh) {
    _showMesh.set(showMesh);
  }

  /**
   * Gets the grid to use for coloring the grid image.
   * 
   * @return the RGB grid.
   */
  public final Grid3d getRGBGrid() {
    return _rgbGrid.get();
  }

  /**
   * Sets the grid to use for coloring the grid image.
   * 
   * @param grid the RGB grid.
   */
  public final void setRGBGrid(final Grid3d grid) {
    _rgbGrid.set(grid);
  }

  /**
   * Gets the color bar to use rendering the grid.
   * 
   * @return the color bar.
   */
  public final ColorBar getColorBar(final Grid3d grid) {
    final int percentile = _percentile.get();
    final ColorBar colorBar = _colorBar.get();
    if (_recomputePercentile && percentile > 0) {
      final float[] clipped = MathUtil.computePercentiles(grid.getValues(), grid.getNullValue(), percentile);
      colorBar.setStartValue(clipped[0]);
      colorBar.setEndValue(clipped[1]);
      _recomputePercentile = false;
    } else if (_recomputePercentile && percentile == 0) {
      colorBar.setStartValue(grid.getMinValue());
      colorBar.setEndValue(grid.getMaxValue());
      _recomputePercentile = false;
    }
    return colorBar;
  }

  /**
   * Gets the color bar to use when rendering the grid.
   * 
   * @return the color bar.
   */
  public final ColorBar getColorBar() {
    return _colorBar.get();
  }

  /**
   * Sets the color bar to use when rendering the grid.
   * 
   * @param colorBar the color bar.
   */
  public final void setColorBar(final ColorBar colorBar) {
    _colorBar.set(colorBar);
  }

  public void validate(final IValidation results) {
    if (_transparency.get() < 0 || _transparency.get() > 100) {
      results.error(_transparency, "Transparency must be in range 0-100.");
    }
    if (_percentile.get() < 0 || _percentile.get() > 100) {
      results.error(_percentile, "Percentile must be in range 0-100.");
    }
  }
}
