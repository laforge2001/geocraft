/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.mapviewer.renderer.grid;


import org.geocraft.core.color.ColorBar;
import org.geocraft.core.color.map.SpectrumColorMap;
import org.geocraft.core.common.util.Labels;
import org.geocraft.core.model.IModel;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.grid.Grid2d;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.validation.IValidation;


public class Grid2dRendererModel extends Model {

  /** The key for the transparency property. */
  public static final String TRANSPARENCY = "Transparency";

  /** The property for the grid transparency. */
  private final IntegerProperty _transparency;

  /** The associated grid. */
  private final Grid2d _grid;

  /** The colorbar used for display the grid image. */
  private ColorBar _colorBar;

  public Grid2dRendererModel(final Grid2d grid) {
    super();
    _grid = grid;
    _transparency = addIntegerProperty(TRANSPARENCY, 0);
    double minValue = grid.getMinValue();
    double maxValue = grid.getMaxValue();
    double[] labels = Labels.computeLabels(minValue, maxValue, 10);
    _colorBar = new ColorBar(64, new SpectrumColorMap(), minValue, maxValue, labels[2]);
  }

  public Grid2dRendererModel(final Grid2dRendererModel model) {
    this(model.getGrid());
    updateFrom(model);
  }

  @Override
  public void updateFrom(final IModel model) {
    super.updateFrom(model);
    if (model instanceof Grid2dRendererModel) {
      _colorBar = new ColorBar(((Grid2dRendererModel) model).getColorBar());
    }
  }

  public Grid2d getGrid() {
    return _grid;
  }

  public int getTransparency() {
    return _transparency.get();
  }

  public void setTransparency(final int transparency) {
    _transparency.set(transparency);
  }

  public ColorBar getColorBar() {
    return _colorBar;
  }

  public void setColorBar(final ColorBar colorBar) {
    _colorBar = colorBar;
  }

  public void validate(final IValidation results) {
    if (_transparency.get() < 0 && _transparency.get() > 100) {
      results.error(TRANSPARENCY, "Transparency must be in the range 0-100.");
    }
  }

}
