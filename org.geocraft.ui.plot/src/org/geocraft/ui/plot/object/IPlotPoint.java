/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.ui.plot.object;


import org.eclipse.swt.graphics.Image;
import org.geocraft.ui.plot.listener.IPlotPointListener;


/**
 * The interface for plot point objects.
 */
public interface IPlotPoint extends IPlotObject {

  /**
   * Gets the shape that the point is associated with.
   * @return the shape that the point is associated with.
   */
  IPlotShape getShape();

  /**
   * Sets the shape with which the point is associated.
   * @param shape the shape with which the point is associated.
   */
  void setShape(IPlotShape shape);

  /**
   * Moves the graph point to the x-y coordinates.
   * @param x the delta x-coordinate to move to.
   * @param y the delta y-coordinate to move to.
   */
  void moveTo(double x, double y);

  /**
   * Moves the graph point to the x-y-z coordinates.
   * @param x the delta x-coordinate to move to.
   * @param y the delta y-coordinate to move to.
   * @param z the delta z-coordinate to move to.
   */
  void moveTo(double x, double y, double z);

  /**
   * Gets the x-coordinate of the point.
   * @return the x-coordinate.
   */
  double getX();

  /**
   * Gets the y-coordinate of the point.
   * @return the y-coordinate.
   */
  double getY();

  /**
   * Gets the z-coordinate of the point.
   * @return the z-coordinate.
   */
  double getZ();

  /**
   * Sets the x-coordinate of the point.
   * @param x the x-coordinate.
   */
  void setX(double x);

  /**
   * Sets the y-coordinate of the point.
   * @param y the y-coordinate.
   */
  void setY(double y);

  /**
   * Sets the z-coordinate of the point.
   * @param z the z-coordinate.
   */
  void setZ(double z);

  /**
   * Sets the x,y-coordinates of the point.
   * @param x the x-coordinate.
   * @param y the y-coordinate.
   */
  void setXY(double x, double y);

  /**
   * Sets the x,y,z-coordinates of the point.
   * @param x the x-coordinate.
   * @param y the y-coordinate.
   * @param z the z-coordinate.
   */
  void setXYZ(double x, double y, double z);

  /**
   * Adds a plot point listener.
   * @param listener the listener to add.
   */
  void addPlotPointListener(IPlotPointListener listener);

  /**
   * Removes a plot point listener.
   * @param listener the listener to remove.
   */
  void removePlotPointListener(IPlotPointListener listener);

  /**
   * Gets the property inheritance of the plot point.
   * @return true if properties to be inherited from shape; false to use own properties.
   */
  boolean getPropertyInheritance();

  /**
   * Sets the property inheritance of the plot point.
   * @param propertyInheritance true to use properties inherited from shape; false to use own properties.
   */
  void setPropertyInheritance(boolean propertyInheritance);

  Image getPointImage();

  void setPointImage(Image image);

}