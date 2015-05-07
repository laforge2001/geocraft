/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.chartviewer.renderer.histogram.well;


import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.color.ColorUtil;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.property.ColorProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.validation.IValidation;


/**
 * The model of rendering properties for a <code>WellLogTrace</code> entity
 * in the histogram view.
 */
public class WellLogTraceRendererModel extends Model {

  /** The key for the color property. */
  public static final String COLOR = "Color";

  /** The key for the # of cells property. */
  public static final String NUMBER_OF_CELLS = "# of Cells";

  /** The property for the histogram color. */
  private ColorProperty _color;

  /** The property for the # of histogram cells. */
  private IntegerProperty _numCells;

  public WellLogTraceRendererModel() {
    super();
    _color = addColorProperty(COLOR, ColorUtil.getCommonRGB());
    _numCells = addIntegerProperty(NUMBER_OF_CELLS, 100);
  }

  public WellLogTraceRendererModel(WellLogTraceRendererModel model) {
    this();
    updateFrom(model);
  }

  public void validate(IValidation results) {
    if (_numCells.get() < 2) {
      results.error(_numCells, "Minimum of 2 cells required for histogram.");
    }
  }

  /**
   * Returns the histogram color to use.
   */
  public RGB getColor() {
    return _color.get();
  }

  /**
   * Returns the # of histogram cells to use.
   */
  public int getNumCells() {
    return _numCells.get();
  }
}
