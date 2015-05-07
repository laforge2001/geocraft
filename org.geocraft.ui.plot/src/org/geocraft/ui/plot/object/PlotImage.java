/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.ui.plot.object;


import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.geocraft.ui.plot.defs.ImageAnchor;
import org.geocraft.ui.plot.defs.ShapeType;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;


/**
 * The basic implementation of a plot image.
 * TODO: Finish this class!
 */
public class PlotImage extends PlotShape implements IPlotImage {

  /** The actual image. */
  protected Image _image;

  /** The image anchor type. */
  protected ImageAnchor _anchor;

  /** The fixed-size status of the image. */
  protected boolean _isFixedSize;

  /**
   * Constructs a plot image.
   * @param image the actual image.
   * @param name the image name.
   * @param anchorType the anchor type.
   * @param anchorPoint the anchor point.
   */
  public PlotImage(final Image image, final String name, final ImageAnchor anchorType, final Point2D.Double anchorPoint) {
    super(ShapeType.IMAGE, name);
    setImage(image);
    setAnchorType(anchorType);
    setFixedSize(true);
    _isMovable = false;
    _points.add(new PlotPoint(anchorPoint.getX(), anchorPoint.getY(), 0));
    _xmin = Double.NaN;
    _xmax = Double.NaN;
    _ymin = Double.NaN;
    _ymax = Double.NaN;
  }

  /**
   * Constructs a plot image.
   * @param image the actual image.
   * @param text the image name.
   * @param corner1 the 1st corner point.
   * @param corner2 the 2nd corner point.
   */
  public PlotImage(final Image image, final String text, final Point2D.Double corner1, final Point2D.Double corner2) {
    super(ShapeType.IMAGE, text);
    setImage(image);
    setAnchorType(ImageAnchor.Center);
    setFixedSize(false);
    _points.add(new PlotPoint(corner1.getX(), corner1.getY(), 0));
    _points.add(new PlotPoint(corner2.getX(), corner2.getY(), 0));
    _xmin = Math.min(corner1.x, corner2.x);
    _xmax = Math.max(corner1.x, corner2.x);
    _ymin = Math.min(corner1.y, corner2.y);
    _ymax = Math.max(corner1.y, corner2.y);
  }

  /**
   * Constructs a plot image.
   * @param image the actual image.
   * @param text the image name.
   * @param corner1 the 1st corner point.
   * @param corner2 the 2nd corner point.
   */
  public PlotImage(final Image image, final String text, final Point2D.Double corner1, final Point2D.Double corner2, final Point2D.Double corner3, final Point2D.Double corner4) {
    super(ShapeType.IMAGE, text);
    setImage(image);
    setAnchorType(ImageAnchor.Center);
    setFixedSize(false);
    _points.add(new PlotPoint(corner1.getX(), corner1.getY(), 0));
    _points.add(new PlotPoint(corner2.getX(), corner2.getY(), 0));
    _points.add(new PlotPoint(corner3.getX(), corner3.getY(), 0));
    _points.add(new PlotPoint(corner4.getX(), corner4.getY(), 0));
    _xmin = Math.min(corner1.x, corner2.x);
    _xmin = Math.min(corner3.x, _xmin);
    _xmin = Math.min(corner4.x, _xmin);
    _xmax = Math.max(corner1.x, corner2.x);
    _xmax = Math.max(corner3.x, _xmax);
    _xmax = Math.max(corner4.x, _xmax);
    _ymin = Math.min(corner1.y, corner2.y);
    _ymin = Math.min(corner3.y, _ymin);
    _ymin = Math.min(corner4.y, _ymin);
    _ymax = Math.max(corner1.y, corner2.y);
    _ymax = Math.max(corner3.y, _ymax);
    _ymax = Math.max(corner4.y, _ymax);
  }

  public ImageAnchor getAnchorType() {
    return _anchor;
  }

  public void setAnchorType(final ImageAnchor anchor) {
    _anchor = anchor;
  }

  public boolean isFixedSize() {
    return _isFixedSize;
  }

  public void setFixedSize(final boolean fixedSize) {
    _isFixedSize = fixedSize;
  }

  public Image getImage() {
    return _image;
  }

  public void setImage(final Image image) {
    _image = image;
    updated();
  }

  public Rectangle getRectangle(final IModelSpaceCanvas canvas) {
    Rectangle rect = _image.getBounds();
    int w = rect.x;
    int h = rect.y;
    int numPoints = getPointCount();
    if (numPoints == 1) {
      ImageAnchor anchor = getAnchorType();
      Point2D.Double m = new Point2D.Double(getPoint(0).getX(), getPoint(0).getY());
      Point2D.Double p = new Point2D.Double(0, 0);
      canvas.transformModelToPixel(getModelSpace(), m.getX(), m.getY(), p);
      if (anchor.equals(ImageAnchor.Center)) {
        rect = new Rectangle((int) (p.x - w / 2), (int) (p.y - h / 2), w, h);
      } else if (anchor.equals(ImageAnchor.UpperLeft)) {
        rect = new Rectangle((int) p.x, (int) p.y, w, h);
      } else if (anchor.equals(ImageAnchor.UpperRight)) {
        rect = new Rectangle((int) (p.x - w), (int) p.y, w, h);
      } else if (anchor.equals(ImageAnchor.LowerLeft)) {
        rect = new Rectangle((int) p.x, (int) (p.y - h), w, h);
      } else if (anchor.equals(ImageAnchor.LowerRight)) {
        rect = new Rectangle((int) (p.x - w), (int) (p.y - h), w, h);
      } else {
        throw new RuntimeException("Invalid image anchor: " + anchor);
      }
    } else if (numPoints >= 2) {
      int xmin = Integer.MAX_VALUE;
      int xmax = -Integer.MAX_VALUE;
      int ymin = Integer.MAX_VALUE;
      int ymax = -Integer.MAX_VALUE;
      for (int i = 0; i < numPoints; i++) {
        Point2D.Double m = new Point2D.Double(getPoint(i).getX(), getPoint(i).getY());
        Point2D.Double p = new Point2D.Double(0, 0);
        canvas.transformModelToPixel(getModelSpace(), m.getX(), m.getY(), p);
        xmin = Math.min(xmin, (int) p.getX());
        xmax = Math.max(xmax, (int) p.getX());
        ymin = Math.min(ymin, (int) p.getY());
        ymax = Math.max(ymax, (int) p.getY());
      }
      w = xmax - xmin + 1;
      h = ymax - ymin + 1;
      rect = new Rectangle(xmin, ymin, w, h);
    } else {
      throw new RuntimeException("Invalid image definition.");
    }
    return rect;
  }

  //  @Override
  //  protected IPlotEditor createEditor() {
  //    return null;
  //  }

  public void propertyChange(final PropertyChangeEvent evt) {
    updated();
  }
}
