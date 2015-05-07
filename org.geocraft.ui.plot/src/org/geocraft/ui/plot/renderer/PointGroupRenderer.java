/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.renderer;


import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.geocraft.ui.plot.defs.ShapeType;
import org.geocraft.ui.plot.model.ICoordinateTransform;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPointGroup;
import org.geocraft.ui.plot.object.IPlotShape;


/**
 * This class renders point groups in a given drawing rectangle.
 * Point groups (and their points) are rendered based on their properties
 * (style, color and size).
 */
public class PointGroupRenderer extends PointRenderer implements IShapeRenderer {

  /**
   * Constructs a renderer for drawing point groups based
   * on a given coordinate transform.
   * @param coordTransform the coordinate transform.
   */
  public PointGroupRenderer(final ICoordinateTransform transform) {
    super(transform);
  }

  /**
   * Renders a plot shape in the specified drawing rectangle.
   * The shape type must be a point group, otherwise an IllegalArgumentException is thrown.
   * @param gc the graphics object.
   * @param rectangle the drawing rectangle.
   * @param mask the masking rectangle.
   * @param shape the shape to draw.
   */
  public void render(final GC gc, final Rectangle rectangle, final Rectangle mask, final IPlotShape shape) {
    // Check that the shape type is a point group.
    // If not throw an exception.
    ShapeType shapeType = shape.getShapeType();
    if (!shapeType.equals(ShapeType.POINT_GROUP)) {
      throw new IllegalArgumentException("Invalid shape type (" + shapeType + "). Must be " + ShapeType.POINT_GROUP
          + ".");
    }
    // Otherwise, continue and render the point group.
    render(gc, rectangle, mask, (IPlotPointGroup) shape);
  }

  /**
   * Renders a plot point group in the specified drawing rectangle.
   * @param gc the graphics object.
   * @param rectangle the drawing rectangle.
   * @param mask the masking rectangle.
   * @param pointGroup the point group to draw..
   */
  public void render(final GC gc, final Rectangle rectangle, final Rectangle mask, final IPlotPointGroup pointGroup) {

    // If the point group is not visible, or does not contain at least 1 point, then simple return.
    int pointCount = pointGroup.getPointCount();
    if (!pointGroup.isVisible() || pointCount < 1) {
      return;
    }

    // Turn off anti-aliasing.
    gc.setAntialias(SWT.OFF);
    gc.setTextAntialias(SWT.OFF);
    gc.setAdvanced(false);

    // Draw the point group points based on the point properties.
    for (int i = 0; i < pointCount; i++) {
      IPlotPoint point = pointGroup.getPoint(i);
      point.setInMotion(pointGroup.isInMotion());
      drawPoint(gc, rectangle, mask, point);
    }
  }
}
