/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.ui.plot.object;


/**
 * The interface for a movable plot shape.
 */
public interface IPlotMovableShape extends IPlotShape {

  /**
   * Moves the specified point by the specified dx-dy values.
   * @param point the point to move.
   * @param dx the delta-x to move by.
   * @param dy the delta-y to move by.
   */
  void movePointBy(IPlotPoint point, double dx, double dy);

  /**
   * Moves the specified point by the specified dx-dy-dz values.
   * @param point the point to move.
   * @param dx the delta-x to move by.
   * @param dy the delta-y to move by.
   * @param dz the delta-z to move by.
   */
  void movePointBy(IPlotPoint point, double dx, double dy, double dz);

  /**
   * Moves the specified point to the specified x-y coordinates.
   * @param point the point to move.
   * @param x the x-coordinate to move to.
   * @param y the y-coordinate to move to.
   */
  void movePointTo(IPlotPoint point, double x, double y);

  /**
   * Moves the specified point to the specified x-y-z coordinates.
   * @param point the point to move.
   * @param x the x-coordinate to move to.
   * @param y the y-coordinate to move to.
   * @param z the z-coordinate to move to.
   */
  void movePointTo(IPlotPoint point, double x, double y, double z);

  /**
   * Moves the shape by the specified dx-dy values.
   * @param dx the delta-x to move by.
   * @param dy the delta-y to move by.
   */
  void moveBy(double dx, double dy);

  /**
   * Moves the shape by the specified dx-dy values.
   * @param dx the delta-x to move by.
   * @param dy the delta-y to move by.
   * @param dz the delta-z to move by.
   */
  void moveBy(double dx, double dy, double dz);
}
