/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.model;


import java.awt.geom.Point2D;

import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.geocraft.ui.plot.axis.IAxis;
import org.geocraft.ui.plot.defs.AxisDirection;
import org.geocraft.ui.plot.defs.AxisScale;
import org.geocraft.ui.plot.defs.Orientation;


public class CoordinateTransform implements ICoordinateTransform, ControlListener {

  /** The plot canvas. */
  private final IModelSpaceCanvas _canvas;

  private int _canvasWidth = -1;

  private int _canvasHeight = -1;

  /**
   * The default constructor.
   * 
   * @param canvas the plot canvas. 
   */
  public CoordinateTransform(final IModelSpaceCanvas canvas) {
    _canvas = canvas;
    _canvas.addControlListener(this);
    updateWidthAndHeight();
  }

  public void transformModelToPixel(final IModelSpace model, final double mx, final double my, final Point2D.Double p) {
    IAxis xAxis = model.getAxisX();
    IAxis yAxis = model.getAxisY();
    p.x = Math.round(transformModelToPixel(xAxis, mx));
    p.y = Math.round(transformModelToPixel(yAxis, my));
  }

  public void transformPixelToModel(final IModelSpace model, final int px, final int py, final Point2D.Double m) {
    IAxis xAxis = model.getAxisX();
    IAxis yAxis = model.getAxisY();
    m.x = transformPixelToModel(xAxis, px);
    m.y = transformPixelToModel(yAxis, py);
  }

  /**
   * Converts a pixel coordinate to a model coordinate.
   * 
   * @param axis
   *            the model axis to use in conversion.
   * @param pixelCoord
   *            the x-or-y pixel coordinate to convert.
   * @param numPixels
   *            the pixel size to use in conversion.
   * @return the converted model coordinate.
   */
  public double transformPixelToModel(final IAxis axis, final double pixelCoord) {
    int numPixels = getNumPixelsInModelCanvas(axis.getOrientation());
    double percent = pixelCoord / (numPixels - 1);
    return pixelPercentToModel(percent, axis);
  }

  /**
   * Converts a model coordinate to a pixel coordinate.
   * 
   * @param axis
   *            the model axis to use in conversion.
   * @param coord
   *            the x-or-y model coordinate to convert.
   * @param numPixels
   *            the pixel size to use in conversion.
   * @return the converted pixel coordinate.
   */
  public double transformModelToPixel(final IAxis axis, final double coord) {
    int numPixels = getNumPixelsInModelCanvas(axis.getOrientation());
    double percent = modelToPixelPercent(coord, axis);
    return percent * (numPixels - 1);
  }

  /**
   * Converts a pixel percentage coordinate to a model coordinate.
   * 
   * @param pixelPercent
   *            the pixel percentage to use in conversion.
   * @param axis
   *            the model axis to use in conversion.
   * @return the converted model coordinate.
   */
  private double pixelPercentToModel(final double pixelPercent, final IAxis axis) {

    double start = axis.getViewableStart();
    double end = axis.getViewableEnd();
    Orientation orientation = axis.getOrientation();
    AxisDirection direction = axis.getDirection();
    AxisScale scale = axis.getScale();
    double modelCoord = 0;

    if (scale.equals(AxisScale.LOG)) {
      start = Math.log(start);
      end = Math.log(end);
    }

    boolean isStartToEnd = direction.isStartToEnd();
    boolean isHorizontal = orientation.equals(Orientation.HORIZONTAL);

    if (isHorizontal == isStartToEnd) {
      modelCoord = pixelPercent * (end - start) + start;
    } else {
      modelCoord = pixelPercent * (start - end) + end;
    }
    if (scale.equals(AxisScale.LOG)) {
      modelCoord = Math.exp(modelCoord);
    }

    return modelCoord;
  }

  /**
   * Converts a model coordinate to a pixel percentage coordinate.
   * 
   * @param modelCoordinate
   *            the model coordinate to use in conversion.
   * @param axis
   *            the model axis to use in conversion.
   * @return the converted pixel percentage.
   */
  protected double modelToPixelPercent(final double modelCoordinate, final IAxis axis) {

    double modelCoord = modelCoordinate;
    double start = axis.getViewableStart();
    double end = axis.getViewableEnd();
    AxisDirection direction = axis.getDirection();
    Orientation orientation = axis.getOrientation();
    AxisScale type = axis.getScale();
    double pixel = 0;

    if (type.equals(AxisScale.LOG)) {
      modelCoord = Math.log(modelCoord);
      start = Math.log(start);
      end = Math.log(end);
    }

    boolean isStartToEnd = direction.isStartToEnd();
    boolean isHorizontal = orientation.equals(Orientation.HORIZONTAL);

    if (isHorizontal && isStartToEnd || !isHorizontal && !isStartToEnd) {
      pixel = (modelCoord - start) / (end - start);
    } else if (isHorizontal && !isStartToEnd || !isHorizontal && isStartToEnd) {
      pixel = (modelCoord - end) / (start - end);
    } else {
      throw new IllegalArgumentException("Invalid axis direction/orientation.");
    }
    return pixel;
  }

  public int getNumPixelsInModelCanvas(final Orientation orientation) {
    if (orientation.equals(Orientation.HORIZONTAL)) {
      return _canvasWidth;
    } else if (orientation.equals(Orientation.VERTICAL)) {
      return _canvasHeight;
    }
    throw new RuntimeException("Invalid axis orientation.");
  }

  public void controlMoved(final ControlEvent e) {
    // No action required.
  }

  public void controlResized(final ControlEvent event) {
    // Store the control widget and height, so getSize() does not have to be called
    // upon each coordinate transform.
    updateWidthAndHeight();
  }

  private void updateWidthAndHeight() {
    Composite composite = (Composite) _canvas;
    Point size = composite.getSize();
    _canvasWidth = size.x;
    _canvasHeight = size.y;
  }
}
