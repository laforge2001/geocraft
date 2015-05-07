/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.ui.plot.object;


import org.geocraft.ui.plot.defs.PointInsertionMode;


/**
 * The interface for a plot point group.
 */
public interface IPlotPointGroup extends IPlotMovableShape {

  /**
   * Adds the specified point to the group.
   * @param point the point to add.
   * @return the index of the inserted point.
   */
  int addPoint(IPlotPoint point);

  /**
   * Adds the specified point to the group, using the specified mode.
   * @param point the point to add.
   * @param mode the insertion mode (First, Last, ByX, ByY, etc).
   * @return the index of the inserted point.
   */
  int addPoint(IPlotPoint point, PointInsertionMode mode);

  /**
   * Removes the specified point from the group.
   * @param point the point to remove.
   */
  void removePoint(IPlotPoint point);

  void rubberband(PointInsertionMode pointInsertionMode, double x, double y);

  void rubberbandOn();

  void rubberbandOff();

}