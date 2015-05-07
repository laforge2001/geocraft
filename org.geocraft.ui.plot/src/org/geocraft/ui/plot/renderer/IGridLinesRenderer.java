/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.renderer;


import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.geocraft.ui.plot.attribute.LineProperties;
import org.geocraft.ui.plot.axis.IAxis;


/**
 * The interface for renderers that draw grid lines in a given rectangle.
 */
public interface IGridLinesRenderer {

  /**
   * Renders the grid lines of an axis into the specified rectangle.
   * @param gc the graphics object.
   * @param rectangle the drawing rectangle.
   * @param axis the axis.
   * @param lineProperties the line display properties.
   */
  void render(GC gc, Rectangle rectangle, IAxis axis, LineProperties lineProperties, int gridDensity);
}
