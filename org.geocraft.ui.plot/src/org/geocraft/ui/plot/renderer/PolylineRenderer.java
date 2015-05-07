/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.renderer;


import java.awt.geom.Point2D;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.ShapeType;
import org.geocraft.ui.plot.model.ICoordinateTransform;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPolyline;
import org.geocraft.ui.plot.object.IPlotShape;


/**
 * This class renders polylines in a given drawing rectangle.
 * Polylines (and their points) are rendered based on their properties
 * (style, color and size).
 */
public class PolylineRenderer extends PointRenderer implements IShapeRenderer {

  /**
   * Constructs a renderer for drawing polylines based
   * on a given coordinate transform.
   * @param coordTransform the coordinate transform.
   */
  public PolylineRenderer(final ICoordinateTransform transform) {
    super(transform);
  }

  /**
   * Renders a plot shape in the specified drawing rectangle.
   * The shape type must be a polyline, otherwise an IllegalArgumentException is thrown.
   * @param gc the graphics object.
   * @param rectangle the drawing rectangle.
   * @param mask the masking rectangle.
   * @param shape the shape to draw.
   */
  public void render(final GC gc, final Rectangle rectangle, final Rectangle mask, final IPlotShape shape) {
    // Check that the shape type is a polyline.
    // If not throw an exception.
    ShapeType shapeType = shape.getShapeType();
    if (!shapeType.equals(ShapeType.POLYLINE)) {
      throw new IllegalArgumentException("Invalid shape type (" + shapeType + "). Must be " + ShapeType.POLYLINE + ".");
    }
    // Otherwise, continue and render the polyline.
    render(gc, rectangle, mask, (IPlotPolyline) shape);
  }

  /**
   * Renders a plot polyline in the specified drawing rectangle.
   * @param gc the graphics object.
   * @param rectangle the drawing rectangle.
   * @param mask the masking rectangle.
   * @param polyline the polyline to draw..
   */
  public void render(final GC gc, final Rectangle rectangle, final Rectangle mask, final IPlotPolyline polyline) {
    int pointCount = polyline.getPointCount();

    // If the polyline is not visible, or does not contain at least 1 point, then simple return.
    if (!polyline.isVisible() || pointCount < 1) {
      return;
    }

    // Turn off anti-aliasing.
    gc.setAntialias(SWT.OFF);
    gc.setTextAntialias(SWT.OFF);
    gc.setAdvanced(false);

    // Allocate a temporary array for storing point coordinates.
    int[] pointArray = new int[pointCount * 2];

    // Draw the polyline points based on the point properties.
    int px0 = 0;
    int py0 = 0;
    for (int i = 0; i < pointCount; i++) {
      IPlotPoint point = polyline.getPoint(i);
      IModelSpace modelSpace = point.getModelSpace();
      if (modelSpace != null) {
        Point2D.Double pixel = new Point2D.Double(0, 0);
        _coordTransform.transformModelToPixel(modelSpace, point.getX(), point.getY(), pixel);
        int index = i * 2;
        int px = Math.round((float) (px0 + pixel.x));
        int py = Math.round((float) (py0 + pixel.y));

        // There seems to be a 32767 limit, so scale down
        // both the x and y if that limit is reached.
        int maxp = Math.max(Math.abs(px), Math.abs(py));
        if (maxp >= 32767) {
          float scalar = 32767f / maxp;
          px = (int) (px * scalar);
          py = (int) (py * scalar);
        }
        pointArray[index] = px;
        pointArray[index + 1] = py;
        point.setInMotion(polyline.isInMotion());
        drawPoint(gc, rectangle, mask, point);
      }
    }

    // Draw the polyline based on the line properties.
    LineStyle lineStyle = polyline.getLineStyle();
    if (!lineStyle.equals(LineStyle.NONE)) {
      Color lineColor = new Color(gc.getDevice(), polyline.getLineColor());
      gc.setForeground(lineColor);
      gc.setLineWidth(polyline.getLineWidth());
      gc.setLineJoin(SWT.JOIN_ROUND);
      gc.setLineCap(SWT.CAP_ROUND);
      if (polyline.getLineStyle().equals(LineStyle.SOLID)) {
        gc.setLineStyle(SWT.LINE_SOLID);
      } else if (polyline.getLineStyle().equals(LineStyle.DASHED)) {
        gc.setLineStyle(SWT.LINE_DASH);
        int[] dashes = { 4 };
        gc.setLineDash(dashes);
      }
      gc.drawPolyline(pointArray);
      lineColor.dispose();
    }

    // Null out the allocated arrays.
    pointArray = null;
  }
}
