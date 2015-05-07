/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.ui.plot.object;


/**
 * The interface for a plot line.
 */
public interface IPlotLine extends IPlotMovableShape, IPlotLinedShape {

  /**
   * Sets the specified points in the line.
   * @param point1 the 1st point to set.
   * @param point2 the 1st point to set.
   */
  void setPoints(IPlotPoint point1, IPlotPoint point2);

}