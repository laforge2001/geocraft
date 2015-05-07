/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.renderer;


import java.awt.geom.Point2D;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.defs.TextAnchor;
import org.geocraft.ui.plot.model.ICoordinateTransform;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotShape;


/**
 * This class renders points in a given drawing rectangle.
 * Points are rendered based on their properties (style, color and size),
 * or the properties of their parent shape if property inheritance
 * is set for the point.
 * This class also serves as the base class for the other shape
 * renderers, such as polylines, polygons, etc.
 */
public class PointRenderer {

  /** The coordinate transform to use in rendering. */
  protected final ICoordinateTransform _coordTransform;

  /**
   * Constructs a renderer for drawing points based
   * on a given coordinate transform.
   * @param coordTransform the coordinate transform.
   */
  public PointRenderer(final ICoordinateTransform coordTransform) {
    _coordTransform = coordTransform;
  }

  /**
   * Renders a plot point in the specified drawing rectangle.
   * @param gc the graphics object.
   * @param rectangle the drawing rectangle.
   * @param mask the masking rectangle.
   * @param point the point to draw.
   */
  public void drawPoint(final GC gc, final Rectangle rectangle, final Rectangle mask, final IPlotPoint point) {
    // Check that the parent shape is not null.
    IPlotShape shape = point.getShape();
    if (shape == null || !point.isVisible()) {
      return;
    }
    int pixelX0 = 0;
    int pixelY0 = 0;
    int pixelX1 = pixelX0 + rectangle.width - 1;
    int pixelY1 = pixelY0 + rectangle.height - 1;
    int maskX0;
    int maskY0;
    int maskX1;
    int maskY1;

    // Set the masking bounds.
    if (mask != null) {
      maskX0 = mask.x;
      maskX1 = mask.x + mask.width - 1;
      maskY0 = mask.y;
      maskY1 = mask.y + mask.height - 1;
    } else {
      maskX0 = pixelX0;
      maskX1 = pixelX1;
      maskY0 = pixelY0;
      maskY1 = pixelY1;
    }

    // Get the point properties.
    PointStyle pointStyle = point.getPointStyle();
    Image pointImage = point.getPointImage();
    int pointSize = point.getPointSize();
    RGB pointRGB = point.getPointColor();
    Font textFont = shape.getTextFont();

    // Override the point properties if inheriting from
    // the parent shape.
    if (point.getPropertyInheritance()) {
      pointStyle = shape.getPointStyle();
      pointSize = shape.getPointSize();
      pointRGB = shape.getPointColor();
    }

    // If point is selected, double its size.
    if (point.isSelected()) {
      pointSize *= 2;
      if (pointStyle.equals(PointStyle.NONE)) {
        pointStyle = PointStyle.FILLED_SQUARE;
      }
      // Ensure a minimum point size of 5.
      pointSize = Math.max(pointSize, 5);
    }
    int pointSizeHalf = pointSize / 2;
    pointSize = 1 + pointSizeHalf * 2;

    // If point color is null, default to black.
    if (pointRGB == null) {
      pointRGB = new RGB(0, 0, 0);
    }

    // If font is null, set a default.
    if (textFont == null) {
      textFont = new Font(null, "SansSerif", 8, SWT.NORMAL);
    }
    gc.setFont(textFont);

    // Transform the model coordinates to pixel coordinates.
    Point2D.Double p = new Point2D.Double(0, 0);
    _coordTransform.transformModelToPixel(shape.getModelSpace(), point.getX(), point.getY(), p);
    int px = (int) (pixelX0 + p.x);
    int py = (int) (pixelY0 + p.y);

    // Only draw the point text if the point is not in motion.
    if (!point.isInMotion()) {
      drawPointTextString(gc, shape, point, px, py);
    }

    // Set the background and foreground colors.
    Color pointColor = new Color(gc.getDevice(), pointRGB);
    gc.setBackground(pointColor);
    gc.setForeground(pointColor);
    gc.setLineStyle(SWT.LINE_SOLID);
    gc.setLineWidth(0);
    gc.setAdvanced(false);
    pointColor.dispose();

    if (pointImage != null) {
      gc.drawImage(pointImage, px - 8, py - 8);
      return;
    }

    // Draw the point symbol based on the various styles.
    if (px + pointSizeHalf >= maskX0 && px - pointSizeHalf <= maskX1 && py + pointSizeHalf >= maskY0
        && py - pointSizeHalf <= maskY1) {
      if (pointStyle.equals(PointStyle.X)) {
        gc.drawLine(px - pointSizeHalf, py - pointSizeHalf, px + pointSizeHalf, py + pointSizeHalf);
        gc.drawLine(px - pointSizeHalf, py + pointSizeHalf, px + pointSizeHalf, py - pointSizeHalf);
      } else if (pointStyle.equals(PointStyle.HORIZONTAL_TICK)) {
        int lineWidth = gc.getLineWidth();
        gc.setLineWidth(2);
        gc.drawLine(px - pointSizeHalf, py, px + pointSizeHalf, py);
        gc.setLineWidth(lineWidth);
      } else if (pointStyle.equals(PointStyle.VERTICAL_TICK)) {
        int lineWidth = gc.getLineWidth();
        gc.setLineWidth(2);
        gc.drawLine(px, py - pointSizeHalf, px, py + pointSizeHalf);
        gc.setLineWidth(lineWidth);
      } else if (pointStyle.equals(PointStyle.CROSS)) {
        gc.drawLine(px - pointSizeHalf, py, px + pointSizeHalf, py);
        gc.drawLine(px, py - pointSizeHalf, px, py + pointSizeHalf);
      } else if (pointStyle.equals(PointStyle.CIRCLE)) {
        gc.drawArc(px - pointSizeHalf, py - pointSizeHalf, pointSize, pointSize, 0, 360);
      } else if (pointStyle.equals(PointStyle.SQUARE)) {
        gc.drawRectangle(px - pointSizeHalf, py - pointSizeHalf, pointSize - 1, pointSize - 1);
      } else if (pointStyle.equals(PointStyle.DIAMOND)) {
        int[] pointArray = new int[8];
        pointArray[0] = px;
        pointArray[1] = py - pointSizeHalf;
        pointArray[2] = px - pointSizeHalf;
        pointArray[3] = py;
        pointArray[4] = px;
        pointArray[5] = py + pointSizeHalf;
        pointArray[6] = px + pointSizeHalf;
        pointArray[7] = py;
        gc.drawPolygon(pointArray);
      } else if (pointStyle.equals(PointStyle.TRIANGLE)) {
        int[] pointArray = new int[6];
        pointArray[0] = px;
        pointArray[1] = py - pointSizeHalf;
        pointArray[2] = px - pointSizeHalf;
        pointArray[3] = py + pointSizeHalf;
        pointArray[4] = px + pointSizeHalf;
        pointArray[5] = py + pointSizeHalf;
        gc.drawPolygon(pointArray);
      } else if (pointStyle.equals(PointStyle.FILLED_CIRCLE)) {
        gc.drawOval(px - pointSizeHalf, py - pointSizeHalf, pointSize, pointSize);
        gc.fillOval(px - pointSizeHalf, py - pointSizeHalf, pointSize, pointSize);
      } else if (pointStyle.equals(PointStyle.FILLED_SQUARE)) {
        gc.drawRectangle(px - pointSizeHalf, py - pointSizeHalf, pointSize - 1, pointSize - 1);
        gc.fillRectangle(px - pointSizeHalf, py - pointSizeHalf, pointSize, pointSize);
      } else if (pointStyle.equals(PointStyle.FILLED_DIAMOND)) {
        int[] pointArray = new int[8];
        pointArray[0] = px;
        pointArray[1] = py - pointSizeHalf;
        pointArray[2] = px - pointSizeHalf;
        pointArray[3] = py;
        pointArray[4] = px;
        pointArray[5] = py + pointSizeHalf;
        pointArray[6] = px + pointSizeHalf;
        pointArray[7] = py;
        gc.drawPolygon(pointArray);
        gc.fillPolygon(pointArray);
      } else if (pointStyle.equals(PointStyle.FILLED_TRIANGLE)) {
        int[] pointArray = new int[6];
        pointArray[0] = px;
        pointArray[1] = py - pointSizeHalf;
        pointArray[2] = px - pointSizeHalf;
        pointArray[3] = py + pointSizeHalf;
        pointArray[4] = px + pointSizeHalf;
        pointArray[5] = py + pointSizeHalf;
        gc.drawPolygon(pointArray);
        gc.fillPolygon(pointArray);
      }
    }
  }

  /**
   * Draws the text string for a plot point.
   * @param gc the graphics object.
   * @param point the point for which to draw the text.
   * @param px the pixel x-coordinate.
   * @param py the pixel y-coordinate.
   */
  private static void drawPointTextString(final GC gc, final IPlotShape shape, final IPlotPoint point, final int px,
      final int py) {
    // Check that there is text to be drawn.
    String text = point.getName();
    if (text == null || text.length() < 1) {
      return;
    }

    // Determine the text width and height.
    TextAnchor anchor = shape.getTextAnchor();
    gc.setFont(shape.getTextFont());
    FontMetrics metrics = gc.getFontMetrics();
    int textWidth = metrics.getAverageCharWidth() * text.length();
    int textHeight = metrics.getAscent() + metrics.getDescent();
    //textHeight = metrics.getHeight();

    // Set the text position based on the text size and anchor point.
    int dpx = 0;
    int dpy = 0;
    if (anchor.equals(TextAnchor.NORTHWEST)) {
      dpx = -textWidth;
      dpy = -textHeight;
    } else if (anchor.equals(TextAnchor.NORTH)) {
      dpx = -textWidth / 2;
      dpy = -textHeight;
    } else if (anchor.equals(TextAnchor.NORTHEAST)) {
      dpx = 0;
      dpy = -textHeight;
    } else if (anchor.equals(TextAnchor.WEST)) {
      dpx = -textWidth;
      dpy = -textHeight / 2;
    } else if (anchor.equals(TextAnchor.CENTER)) {
      dpx = -textWidth / 2;
      dpy = -textHeight / 2;
    } else if (anchor.equals(TextAnchor.EAST)) {
      dpx = 0;
      dpy = -textHeight / 2;
    } else if (anchor.equals(TextAnchor.SOUTHWEST)) {
      dpx = -textWidth;
      dpy = 0;
    } else if (anchor.equals(TextAnchor.SOUTH)) {
      dpx = -textWidth / 2;
      dpy = 0;
    } else if (anchor.equals(TextAnchor.SOUTHEAST)) {
      dpx = 0;
      dpy = 0;
    }
    gc.setAlpha(0);
    gc.drawRectangle(px, py, textWidth, textHeight);

    // Set the text properties.
    Color textColor = new Color(gc.getDevice(), shape.getTextColor());
    gc.setForeground(textColor);

    // Draw the text string.
    gc.setAlpha(255);
    gc.drawString(text, px + dpx, py + dpy);
    textColor.dispose();
  }
}
