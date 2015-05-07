/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.renderer;


import java.awt.geom.Point2D;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Display;
import org.geocraft.ui.plot.defs.ImageAnchor;
import org.geocraft.ui.plot.model.ICoordinateTransform;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.object.IPlotImage;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotShape;


/**
 * This class renders image shapes.
 */
public class ImageRenderer extends PointRenderer implements IShapeRenderer {

  /**
   * Constructs a renderer for drawing images in a rectangle, based on
   * a given coordinate transform.
   * @param coordTransform the coordinate transform.
   */
  public ImageRenderer(final ICoordinateTransform transform) {
    super(transform);
  }

  public void render(final GC gc, final Rectangle rectangle, final Rectangle mask, final IPlotShape shape) {
    int pointCount = shape.getPointCount();
    IPlotImage image = (IPlotImage) shape;
    gc.setAdvanced(false);

    // If shape is not visible, has zero points, or the image is null,
    // then simple return.
    if (!shape.isVisible() || image.getImage() == null || pointCount < 1) {
      return;
    }

    IModelSpace modelSpace = image.getModelSpace();
    ImageAnchor anchor = image.getAnchorType();
    int x = 0;
    int y = 0;
    Rectangle bounds = image.getImage().getBounds();
    int srcWidth = bounds.width;
    int srcHeight = bounds.height;
    if (pointCount == 1) {
      // If point count is 1, then the image is a fixed size and anchored
      // by one of its corners.
      Point2D.Double m = new Point2D.Double(image.getPoint(0).getX(), image.getPoint(0).getY());
      Point2D.Double p = new Point2D.Double(0, 0);
      _coordTransform.transformModelToPixel(modelSpace, m.getX(), m.getY(), p);
      // Compute the x,y anchor point.
      if (anchor.equals(ImageAnchor.Center)) {
        x = Math.round((float) (p.x - srcWidth / 2));
        y = Math.round((float) (p.y - srcWidth / 2));
      } else if (anchor.equals(ImageAnchor.UpperLeft)) {
        x = Math.round((float) p.x);
        y = Math.round((float) p.y);
      } else if (anchor.equals(ImageAnchor.UpperRight)) {
        x = Math.round((float) (p.x - srcWidth / 2));
        y = Math.round((float) p.y);
      } else if (anchor.equals(ImageAnchor.LowerLeft)) {
        x = Math.round((float) p.x);
        y = Math.round((float) (p.y - srcHeight / 2));
      } else if (anchor.equals(ImageAnchor.LowerRight)) {
        x = Math.round((float) (p.x - srcWidth));
        y = Math.round((float) (p.y - srcHeight));
      } else {
        throw new IllegalArgumentException("Invalid image anchor: " + anchor);
      }
      gc.drawImage(image.getImage(), x, y);
    } else if (pointCount == 2) {
      // If point count is 2, then the image is a variable size, where the
      // points represent opposite corners (e.g. northwest and southeast).
      IPlotPoint point1 = image.getPoint(0);
      Point2D.Double m1 = new Point2D.Double(point1.getX(), point1.getY());
      Point2D.Double p1 = new Point2D.Double(0, 0);
      _coordTransform.transformModelToPixel(modelSpace, m1.getX(), m1.getY(), p1);
      int x1 = (int) Math.round(p1.x);
      int y1 = (int) Math.round(p1.y);
      IPlotPoint point2 = image.getPoint(1);
      Point2D.Double m2 = new Point2D.Double(point2.getX(), point2.getY());
      Point2D.Double p2 = new Point2D.Double(0, 0);
      _coordTransform.transformModelToPixel(modelSpace, m2.getX(), m2.getY(), p2);
      int x2 = (int) Math.round(p2.x);
      int y2 = (int) Math.round(p2.y);
      if (y2 < y1) {
        int temp = y1;
        y1 = y2;
        y2 = temp;
      }
      if (x2 < x1) {
        int temp = x1;
        x1 = x2;
        x2 = temp;
      }
      int w = x2 - x1 + 1;
      int h = y2 - y1 + 1;
      gc.drawImage(image.getImage(), 0, 0, srcWidth, srcHeight, x1, y1, w, h);
    } else if (pointCount == 4) {
      // If point count is 4, then the image is a variable size, where the
      // points represent the four corners. 
      IPlotPoint point1 = image.getPoint(0);
      Point2D.Double p1 = new Point2D.Double(0, 0);
      _coordTransform.transformModelToPixel(modelSpace, point1.getX(), point1.getY(), p1);

      IPlotPoint point2 = image.getPoint(1);
      Point2D.Double p2 = new Point2D.Double(0, 0);
      _coordTransform.transformModelToPixel(modelSpace, point2.getX(), point2.getY(), p2);

      IPlotPoint point3 = image.getPoint(2);
      Point2D.Double p3 = new Point2D.Double(0, 0);
      _coordTransform.transformModelToPixel(modelSpace, point3.getX(), point3.getY(), p3);

      IPlotPoint point4 = image.getPoint(3);
      Point2D.Double p4 = new Point2D.Double(0, 0);
      _coordTransform.transformModelToPixel(modelSpace, point4.getX(), point4.getY(), p4);
      int x4 = (int) Math.round(p4.x);
      int y4 = (int) Math.round(p4.y);

      double dx = point2.getX() - point1.getX();
      double dy = point2.getY() - point1.getY();
      double angle = Math.toDegrees(Math.atan2(dy, dx));

      double imageDiag = Math.hypot(srcWidth, srcHeight);
      double screenDiag = Math.hypot(p3.getX() - p1.getX(), p3.getY() - p1.getY());

      float mag = (float) (screenDiag / imageDiag);
      int w = Math.round(mag * srcWidth);
      int h = Math.round(mag * srcHeight);

      // check to see if the image is ridiculously tiny .... 
      if (w < 2 || h < 2) {
        // nothing much to draw here. 
        return;
      }

      // gc.setAdvanced(false);

      Transform xform = new Transform(Display.getCurrent());

      float centerX = (float) p4.getX();
      float centerY = (float) p4.getY();
      xform.translate(centerX, centerY);
      xform.rotate((float) angle * -1); // zero is at 3pm in the transform, clockwise is +ve
      xform.translate(-centerX, -centerY);
      gc.setTransform(xform);

      // this approach seems to anti alias the resampled image which looks blurry. 
      gc.setAntialias(SWT.OFF);
      gc.setInterpolation(SWT.NONE);
      gc.drawImage(image.getImage(), 0, 0, srcWidth, srcHeight, x4, y4, w, h);

      // this approach looks better.....but may be slower
      //System.out.println("Creating yet another scaled image");
      //ImageData imageData = image.getImage().getImageData();
      //ImageData scaledImageData = imageData.scaledTo(w, h);
      //Image scaledImage = new Image(Display.getCurrent(), scaledImageData);
      //gc.drawImage(scaledImage, x4, y4);
      //scaledImage.dispose();

      gc.setTransform(null);
      xform.dispose();

    } else {
      throw new IllegalArgumentException("Invalid point count for image shape: " + pointCount);
    }
  }
}
