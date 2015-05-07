/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.mapviewer.renderer.seismic;


import org.geocraft.core.color.ColorBar;
import org.geocraft.core.color.map.SpectrumColorMap;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.validation.IValidation;


public class PostStack2dRendererModel extends Model {

  public static final String Z_SLICE = "Z Slice";

  private final IntegerProperty _zSlice;

  private final ColorBar _colorBar;

  /**
   * The default constructor.
   */
  public PostStack2dRendererModel() {
    super();
    _zSlice = addIntegerProperty(Z_SLICE, 0);
    _colorBar = new ColorBar(64, new SpectrumColorMap(), 0, 100, 10);
  }

  /**
   * The copy constructor.
   * @param model the poststack 2d renderer model to copy.
   */
  public PostStack2dRendererModel(final PostStack2dRendererModel model) {
    this();
    updateFrom(model);
    _colorBar.setColors(model.getColorBar().getColors());
  }

  public void validate(final IValidation results) {
    if (_zSlice.get() < 0) {
      results.error(Z_SLICE, "Z slice must be >= 0");
    }
  }

  /**
   * Returns the z slice to render.
   */
  public int getZSlice() {
    return _zSlice.get();
  }

  public ColorBar getColorBar() {
    return _colorBar;
  }

}
