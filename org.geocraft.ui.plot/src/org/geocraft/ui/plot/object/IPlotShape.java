/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.ui.plot.object;


import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.geocraft.ui.plot.attribute.TextProperties;
import org.geocraft.ui.plot.defs.ShapeType;
import org.geocraft.ui.plot.defs.TextAnchor;
import org.geocraft.ui.plot.listener.IPlotPointListener;
import org.geocraft.ui.plot.listener.IPlotShapeListener;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.model.ModelSpaceBounds;


/**
 * The interface for a plot shape.
 */
public interface IPlotShape extends IPlotObject, IPlotPointListener {

  /**
   * Gets the shape type.
   * @return the shape type.
   */
  ShapeType getShapeType();

  /**
   * Sets the shape type.
   * @param shapeType the shape type.
   */
  void setShapeType(ShapeType shapeType);

  /**
   * Returns the movable status of the shape.
   * @return true if movable; false if not.
   */
  boolean isMovable();

  /**
   * Gets the fixed point count flag of the shape.
   * @return true if fixed point count (Point, Rectangle, Line); false otherwise (Polyline, Polygon, etc).
   */
  boolean isFixedPointCount();

  /**
   * Sets the fixed-point count flag of the shape.
   * @param isFixedPointCount true if fixed point count (Point, Rectangle, Line); false otherwise (Polyline, Polygon, etc).
   */
  void setFixedPointCount(boolean isFixedPointCount);

  /**
   * Gets the point count of the shape.
   * @return the point count.
   */
  int getPointCount();

  /**
   * Gets the first point of the shape.
   * @return the first point of the shape.
   */
  IPlotPoint getFirstPoint();

  /**
   * Gets the last point of the shape.
   * @return the last point of the shape.
   */
  IPlotPoint getLastPoint();

  /**
   * Gets the previous point relative to the specified point.
   * @param point the point of reference.
   * @return the previous point to the reference point.
   */
  IPlotPoint getPrevPoint(IPlotPoint point);

  /**
   * Gets the next point relative to the specified point.
   * @param point the point of reference.
   * @return the next point to the reference point.
   */
  IPlotPoint getNextPoint(IPlotPoint point);

  /**
   * Gets the index-th point of the shape.
   * @param index the index of the point to get.
   * @return the index-th point of the shape.
   */
  IPlotPoint getPoint(int index);

  /**
   * Gets an array of the points in the shape.
   * @return an array of the points in the shape.
   */
  IPlotPoint[] getPoints();

  /**
   * Selects the specified point in the shape.
   * @param point the point to select.
   */
  void selectPoint(IPlotPoint point);

  /**
   * Deselects the specified point in the shape.
   * @param point the point to deselect.
   */
  void deselectPoint(IPlotPoint point);

  /**
   * Gets the minimum x-value of the shape.
   * @return the minimum x-value of the shape.
   */
  double getMinimumX();

  /**
   * Gets the maximum x-value of the shape.
   * @return the maximum x-value of the shape.
   */
  double getMaximumX();

  /**
   * Gets the minimum y-value of the shape.
   * @return the minimum y-value of the shape.
   */
  double getMinimumY();

  /**
   * Gets the maximum y-value of the shape.
   * @return the maximum y-value of the shape.
   */
  double getMaximumY();

  /**
   * Gets the minimum z-value of the shape.
   * @return the minimum z-value of the shape.
   */
  double getMinimumZ();

  /**
   * Gets the maximum z-value of the shape.
   * @return the maximum z-value of the shape.
   */
  double getMaximumZ();

  /**
   * Gets the rectangular bounds of the shape.
   * @param canvas the model canvas.
   * @return the rectangular bounds of the shape.
   */
  Rectangle getRectangle(final IModelSpaceCanvas canvas);

  ModelSpaceBounds getBounds();

  /**
   * Gets the previous shape.
   * @return the previous shape.
   */
  IPlotShape getPrevShape();

  /**
   * Gets the next shape.
   * @return the next shape.
   */
  IPlotShape getNextShape();

  /**
   * Sets the previous shape.
   * @param prevShape the previous shape.
   */
  void setPrevShape(IPlotShape prevShape);

  /**
   * Sets the next shape.
   * @param nextShape the next shape.
   */
  void setNextShape(IPlotShape nextShape);

  /**
   * Adds a plot shape listener.
   * @param listener the listener to add.
   */
  void addShapeListener(IPlotShapeListener listener);

  /**
   * Removes a plot shape listener.
   * @param listener the listener to remove.
   */
  void removeShapeListener(IPlotShapeListener listener);

  /**
   * Clears the shape (removes all points, etc).
   */
  void clear();

  /**
   * Gets the object text properties associated with the object.
   * The text properties will be used in rendering any text that
   * is associated with the object, such as point labels.
   * @return the object text properties.
   */
  TextProperties getTextProperties();

  /**
   * Gets the text font associated with the object.
   * This is a convenience method for the associated text properties.
   * @return the text font associated with the object.
   */
  Font getTextFont();

  /**
   * Gets the text color associated with the object.
   * This is a convenience method for the associated text properties.
   * @return the text color associated with the object.
   */
  RGB getTextColor();

  /**
   * Gets the text anchor associated with the object.
   * This is a convenience method for the associated text properties.
   * @return the text anchor associated with the object.
   */
  TextAnchor getTextAnchor();

  /**
   * Sets the text font to associate with the object.
   * This is a convenience method for the associated text properties.
   * @param font the text font to associate with the object.
   */
  void setTextFont(final Font font);

  /**
   * Sets the text color to associate with the object.
   * This is a convenience method for the associated text properties.
   * @param color the text color to associate with the object.
   */
  void setTextColor(final RGB color);

  /**
   * Sets the text anchor to associate with the object.
   * This is a convenience method for the associated text properties.
   * @param anchor the text anchor to associate with the object.
   */
  void setTextAnchor(final TextAnchor anchor);

  /**
   * Gets the transparency of the shape.
   * Note: 0=fully opaque, 100=fully transparent.
   * @return the transparency.
   */
  int getTransparency();

  /**
   * Sets the transparency of the shape.
   * Note: 0=fully opaque, 100=fully transparent.
   * @param transparency the transparency.
   */
  void setTransparency(int transparency);
}