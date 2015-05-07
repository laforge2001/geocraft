/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.renderer;


import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.geocraft.ui.plot.attribute.FillProperties;
import org.geocraft.ui.plot.attribute.LineProperties;
import org.geocraft.ui.plot.defs.FillStyle;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.ShapeType;
import org.geocraft.ui.plot.model.ICoordinateTransform;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPolygon;
import org.geocraft.ui.plot.object.IPlotShape;


/**
 * This class renders polygons in a given drawing rectangle.
 * Polygons (and their points) are rendered based on their properties
 * (style, color, fill and size).
 */
public class PolygonRenderer extends PointRenderer implements IShapeRenderer {

  /**
   * Constructs a renderer for drawing polygons based
   * on a given coordinate transform.
   * @param coordTransform the coordinate transform.
   */
  public PolygonRenderer(final ICoordinateTransform transform) {
    super(transform);
  }

  /**
   * Renders a plot shape in the specified drawing rectangle.
   * The shape type must be a polygon, otherwise an IllegalArgumentException is thrown.
   * @param gc the graphics object.
   * @param rectangle the drawing rectangle.
   * @param mask the masking rectangle.
   * @param shape the shape to draw.
   */
  public void render(final GC gc, final Rectangle rectangle, final Rectangle mask, final IPlotShape shape) {
    // Check that the shape type is a polygon.
    // If not throw an exception.
    ShapeType shapeType = shape.getShapeType();
    if (!shapeType.equals(ShapeType.POLYGON)) {
      throw new IllegalArgumentException("Invalid shape type (" + shapeType + "). Must be " + ShapeType.POLYGON + ".");
    }
    // Otherwise, continue and render the polygon.
    render(gc, rectangle, mask, (IPlotPolygon) shape);
  }

  /**
   * Renders a plot polygon in the specified drawing rectangle.
   * @param gc the graphics object.
   * @param rectangle the drawing rectangle.
   * @param mask the masking rectangle.
   * @param polygon the polygon to draw..
   */
  public void render(final GC gc, final Rectangle rectangle, final Rectangle mask, final IPlotPolygon polygon) {
    int pointCount = polygon.getPointCount();

    // If the polygon is not visible, or does not contain at least 1 point, then simple return.
    if (!polygon.isVisible() || pointCount < 1) {
      return;
    }

    // Turn off anti-aliasing.
    gc.setAntialias(SWT.OFF);
    gc.setTextAntialias(SWT.OFF);
    gc.setAdvanced(false);

    int transparency = (int) (255 * polygon.getTransparency() / 100f);
    int alpha = 255 - transparency;
    gc.setAlpha(alpha);

    // Allocate a temporary for storing point coordinates.
    int[] pointArray = new int[pointCount * 2];

    // Draw the polygon points based on the point properties.//setRenderingHints(graphics);
    int plotX0 = 0;
    int plotY0 = 0;
    for (int i = 0; i < pointCount; i++) {
      IPlotPoint point = polygon.getPoint(i);
      IModelSpace modelSpace = point.getModelSpace();
      if (modelSpace != null) {
        Point2D.Double pixel = new Point2D.Double(0, 0);
        _coordTransform.transformModelToPixel(modelSpace, point.getX(), point.getY(), pixel);
        int index = i * 2;
        int px = Math.round((float) (plotX0 + pixel.x));
        int py = Math.round((float) (plotY0 + pixel.y));

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
        // Defer drawing the points until the end.
      }
    }

    // Draw the polygon outline based on the line properties.
    LineProperties lineProps = polygon.getLineProperties();
    LineStyle lineStyle = lineProps.getStyle();
    Color lineColor = new Color(gc.getDevice(), lineProps.getColor());
    if (!lineStyle.equals(LineStyle.NONE)) {
      gc.setForeground(lineColor);
      gc.setLineWidth(polygon.getLineWidth());
      gc.setLineJoin(SWT.JOIN_ROUND);
      gc.setLineCap(SWT.CAP_ROUND);
      if (lineStyle.equals(LineStyle.SOLID)) {
        gc.setLineStyle(SWT.LINE_SOLID);
      } else if (lineStyle.equals(LineStyle.DASHED)) {
        gc.setLineStyle(SWT.LINE_DASH);
        int[] dashes = { 4 };
        gc.setLineDash(dashes);
      }
    }

    // Draw the polygon fill based on the fill properties.
    // This can only be done if there are 3 or more points.
    if (pointCount >= 3) {
      FillProperties fillProps = polygon.getFillProperties();
      FillStyle fillStyle = fillProps.getStyle();
      BufferedImage fillImage = fillProps.getImage();
      Color fillColor = new Color(gc.getDevice(), fillProps.getRGB());
      if (fillStyle.equals(FillStyle.SOLID) || fillStyle.equals(FillStyle.TEXTURE) && fillImage == null) {
        gc.setBackground(fillColor);
        gc.fillPolygon(pointArray);
        gc.setForeground(lineColor);
      } else if (fillStyle.equals(FillStyle.TEXTURE)) {
        // TODO: Implement the texture fill stle.
        //        BufferedImage img = polygon.getFillImage();
        //        TexturePaint tp = new TexturePaint(img, new Rectangle(0, 0, img.getWidth(), img.getHeight()));
        //        graphics.setPaint(tp);
        //        graphics.fill(polygonShape);
        //        graphics.setPaint(null);
      }
      fillColor.dispose();
    }
    gc.drawPolygon(pointArray);

    // Lastly, draw the polygon points.
    for (int i = 0; i < pointCount; i++) {
      IPlotPoint point = polygon.getPoint(i);
      point.setInMotion(polygon.isInMotion());
      drawPoint(gc, rectangle, mask, point);
    }
    gc.setAlpha(255);

    // Null out the allocated arrays.
    pointArray = null;

    lineColor.dispose();
  }
}
