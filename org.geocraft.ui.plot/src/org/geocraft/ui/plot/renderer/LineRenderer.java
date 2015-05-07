/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.renderer;


import java.awt.geom.Point2D;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.geocraft.ui.plot.attribute.LineProperties;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.ShapeType;
import org.geocraft.ui.plot.model.ICoordinateTransform;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.object.IPlotLine;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotShape;


/**
 * This class renders lines in a given drawing rectangle.
 * Lines (and their points) are rendered based on their properties
 * (style, color and size).
 */
public class LineRenderer extends PointRenderer implements IShapeRenderer {

  /**
   * Constructs a renderer for drawing lines based
   * on a given coordinate transform.
   * @param coordTransform the coordinate transform.
   */
  public LineRenderer(final ICoordinateTransform transform) {
    super(transform);
  }

  /**
   * Renders a plot shape in the specified drawing rectangle.
   * The shape type must be a line, otherwise an IllegalArgumentException is thrown.
   * @param gc the graphics object.
   * @param rectangle the drawing rectangle.
   * @param mask the masking rectangle.
   * @param shape the shape to draw.
   */
  public void render(final GC gc, final Rectangle rectangle, final Rectangle mask, final IPlotShape shape) {
    // Check that the shape type is a line.
    // If not throw an exception.
    ShapeType shapeType = shape.getShapeType();
    if (!shapeType.equals(ShapeType.LINE)) {
      throw new IllegalArgumentException("Invalid shape type (" + shapeType + "). Must be " + ShapeType.LINE + ".");
    }
    // Otherwise, continue and render the line.
    render(gc, rectangle, mask, (IPlotLine) shape);
  }

  /**
   * Renders a plot line in the specified drawing rectangle.
   * @param gc the graphics object.
   * @param rectangle the drawing rectangle.
   * @param mask the masking rectangle.
   * @param line the line to draw.
   */
  public void render(final GC gc, final Rectangle rectangle, final Rectangle mask, final IPlotLine line) {

    // If the line is not visible, or does not contain 2 points, then simple return.
    if (!line.isVisible() || line.getPointCount() < 2) {
      return;
    }

    // Turn off anti-aliasing.
    gc.setAntialias(SWT.OFF);
    gc.setTextAntialias(SWT.OFF);
    gc.setAdvanced(false);

    // Allocate temporary arrays for storing point coordinates.
    int[] ixLine = new int[2];
    int[] iyLine = new int[2];

    // Draw the line points based on the point properties.
    int px0 = 0;
    int py0 = 0;
    for (int i = 0; i < 2; i++) {
      IPlotPoint point = line.getPoint(i);
      IModelSpace modelSpace = line.getModelSpace();
      if (modelSpace != null) {
        Point2D.Double pixel = new Point2D.Double(0, 0);
        _coordTransform.transformModelToPixel(modelSpace, point.getX(), point.getY(), pixel);
        ixLine[i] = Math.round((float) (px0 + pixel.x));
        iyLine[i] = Math.round((float) (py0 + pixel.y));
        point.setInMotion(line.isInMotion());
        drawPoint(gc, rectangle, mask, point);
      }
    }

    // Draw the line based on the line properties.
    LineProperties lineProps = line.getLineProperties();
    LineStyle lineStyle = lineProps.getStyle();
    if (lineStyle != LineStyle.NONE) {
      Color lineColor = new Color(gc.getDevice(), lineProps.getColor());
      gc.setForeground(lineColor);
      gc.setLineWidth(lineProps.getWidth());
      gc.setLineJoin(SWT.JOIN_ROUND);
      gc.setLineCap(SWT.CAP_ROUND);
      if (lineStyle.equals(LineStyle.SOLID)) {
        gc.setLineStyle(SWT.LINE_SOLID);
      } else if (line.getLineProperties().getStyle().equals(LineStyle.DASHED)) {
        gc.setLineStyle(SWT.LINE_DASH);
        int[] dashes = { 4 };
        gc.setLineDash(dashes);
      }
      gc.drawLine(ixLine[0], iyLine[0], ixLine[1], iyLine[1]);
      lineColor.dispose();
    }

    // Null out the allocated arrays.
    ixLine = null;
    iyLine = null;
  }
}
