/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.renderer;


import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.geocraft.ui.plot.object.IPlotShape;


/**
 * The interface for renderers that render plot shapes in a given drawing rectangle.
 */
public interface IShapeRenderer {

  /**
   * Renders a shape in the specified drawing rectangle.
   * @param gc the graphics object.
   * @param rectangle the drawing rectangle.
   * @param mask the drawing mask rectangle.
   * @param shape the plot shape to draw.
   */
  void render(final GC gc, final Rectangle rectangle, final Rectangle mask, final IPlotShape shape);
}
