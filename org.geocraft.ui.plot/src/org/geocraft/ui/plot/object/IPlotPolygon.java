/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.ui.plot.object;


/**
 * The interface for a plot polygon.
 */
public interface IPlotPolygon extends IPlotPolyline, IPlotFilledShape {

  /**
   * Checks whether an x,y point is inside the plot polygon.
   * @param x the x-coordinate.
   * @param y the y-coordinate.
   * @return true if the x,y point is inside; false if outside.
   */
  boolean isPointInside(double x, double y);

}
