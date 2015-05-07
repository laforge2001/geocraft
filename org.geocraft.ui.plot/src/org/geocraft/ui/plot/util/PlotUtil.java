/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.util;


import java.awt.geom.Point2D;
import java.io.IOException;
import java.net.URL;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotShape;


/**
 * A collection of utilities related to the plot code.
 */
public class PlotUtil {

  /** The RGB for the color light-gray. */
  public static final RGB RGB_LIGHT_GRAY = new RGB(229, 229, 229);

  /** The RGB for the color tan. */
  public static final RGB RGB_TAN = new RGB(236, 233, 216);

  /** The RGB for the color black */
  public static final RGB RGB_BLACK = new RGB(0, 0, 0);

  /**
   * Creates a buffered image from the file at the specified path.
   * The returned image will need to be disposed of by the consumer.
   * @param path the path to the image file.
   * @return the image the buffered image.
   * @throws IOException if an I/O error occurs
   */
  public static Image createImage(final String path) throws IOException {
    URL url = createURL(path);
    if (url == null) {
      return null;
    }
    return new Image(Display.getCurrent(), path);
  }

  /**
   * Create a URL using a path that is related to the location
   * of a specified class.
   * @param path the full path.
   * @return the URL.
   */
  public static URL createURL(final String path) {
    return PlotUtil.class.getClassLoader().getResource(path);
  }

  /**
   * Creates a color resource from the specified RGB.
   * The returned color will need to be disposed of
   * by the consumer.
   * @param display the display device.
   * @param rgb the RGB of the color to create.
   * @return the color.
   */
  public static Color createColor(final Display display, final RGB rgb) {
    return new Color(display, rgb);
  }

  /**
   * Gets the rectangular bounds of the specified object.
   * @param renderer the model renderer.
   * @return the rectangular bounds of the object.
   */
  public static Rectangle getRectangle(final IModelSpaceCanvas renderer, final IPlotPoint point) {
    Rectangle rect = null;
    // Gets the shape associated with the specified point.
    IPlotShape shape = point.getShape();
    if (shape == null) {
      return rect;
    }
    IPlotPoint pointPrev = shape.getPrevPoint(point);
    IPlotPoint pointNext = shape.getNextPoint(point);
    int pointSize;
    int pointSizePrev;
    int pointSizeNext;
    int pxmin;
    int pxminPrev;
    int pxminNext;
    int pxmax;
    int pxmaxPrev;
    int pxmaxNext;
    int pymin;
    int pyminPrev;
    int pyminNext;
    int pymax;
    int pymaxPrev;
    int pymaxNext;

    // Get the model space of the shape.
    IModelSpace modelSpace = shape.getLayer().getModelSpace();

    // Get the pixel coordinates of the point.
    Point2D.Double p = new Point2D.Double(0, 0);
    renderer.transformModelToPixel(modelSpace, point.getX(), point.getY(), p);
    pointSize = point.getPointSize();
    if (pointSize < 0) {
      pointSize = shape.getPointProperties().getSize();
    }
    if (pointSize < 5) {
      pointSize = 5;
    }
    pointSize *= 2;
    pxmin = (int) (p.x - pointSize);
    pxmax = (int) (p.x + pointSize);
    pymin = (int) (p.y - pointSize);
    pymax = (int) (p.y + pointSize);
    if (pointPrev != null) {
      Point2D.Double pPrev = new Point2D.Double(0, 0);
      renderer.transformModelToPixel(modelSpace, pointPrev.getX(), pointPrev.getY(), pPrev);
      pointSizePrev = pointPrev.getPointProperties().getSize();
      if (pointSizePrev < 0) {
        pointSizePrev = shape.getPointProperties().getSize();
      }
      if (pointSizePrev < 5) {
        pointSizePrev = 5;
      }
      pointSizePrev *= 2;
      pxminPrev = (int) (pPrev.x - pointSizePrev);
      pxmaxPrev = (int) (pPrev.x + pointSizePrev);
      pyminPrev = (int) (pPrev.y - pointSizePrev);
      pymaxPrev = (int) (pPrev.y + pointSizePrev);
      pxmin = Math.min(pxmin, pxminPrev);
      pxmax = Math.max(pxmax, pxmaxPrev);
      pymin = Math.min(pymin, pyminPrev);
      pymax = Math.max(pymax, pymaxPrev);
    }
    if (pointNext != null) {
      Point2D.Double pNext = new Point2D.Double(0, 0);
      renderer.transformModelToPixel(modelSpace, pointNext.getX(), pointNext.getY(), pNext);
      pointSizeNext = pointNext.getPointProperties().getSize();
      if (pointSizeNext < 0) {
        pointSizeNext = shape.getPointProperties().getSize();
      }
      if (pointSizeNext < 5) {
        pointSizeNext = 5;
      }
      pointSizeNext *= 2;
      pxminNext = (int) (pNext.x - pointSizeNext);
      pxmaxNext = (int) (pNext.x + pointSizeNext);
      pyminNext = (int) (pNext.y - pointSizeNext);
      pymaxNext = (int) (pNext.y + pointSizeNext);
      pxmin = Math.min(pxmin, pxminNext);
      pxmax = Math.max(pxmax, pxmaxNext);
      pymin = Math.min(pymin, pyminNext);
      pymax = Math.max(pymax, pymaxNext);
    }
    int width = pxmax - pxmin + 1;
    int height = pymax - pymin + 1;

    // Create and return the rectangular bounds.
    return new Rectangle(pxmin, pymin, width, height);
  }
}
