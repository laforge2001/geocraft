/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.ui.plot.object;


import java.awt.image.BufferedImage;

import org.eclipse.swt.graphics.RGB;
import org.geocraft.ui.plot.attribute.FillProperties;
import org.geocraft.ui.plot.attribute.LineProperties;
import org.geocraft.ui.plot.attribute.PointProperties;
import org.geocraft.ui.plot.attribute.TextProperties;
import org.geocraft.ui.plot.defs.FillStyle;
import org.geocraft.ui.plot.defs.ShapeType;


/**
 * The basic implementation of a plot polygon.
 */
public class PlotPolygon extends PlotPolyline implements IPlotPolygon {

  /** The plot fill properties. */
  protected FillProperties _fillProperties;

  /**
   * Constructs a plot polygon.
   */
  public PlotPolygon() {
    this("", new TextProperties(), new PointProperties(), new LineProperties(), new FillProperties());
  }

  /**
   * Constructs a plot polygon, copy from another plot polygon.
   * @param polygon the plot polygon to copy.
   */
  public PlotPolygon(final IPlotPolygon polygon) {
    this(polygon.getName(), polygon.getTextProperties(), polygon.getPointProperties(), polygon.getLineProperties(),
        polygon.getFillProperties());
  }

  /**
   * Constructs a plot polygon.
   * @param name the polygon name.
   * @param textProps the text properties.
   * @param pointProps the point properties.
   * @param lineProps the line properties.
   * @param fillProps the fill properties.
   */
  public PlotPolygon(final String name, final TextProperties textProps, final PointProperties pointProps, final LineProperties lineProps, final FillProperties fillProps) {
    super(name, textProps, pointProps, lineProps);
    _fillProperties = new FillProperties(fillProps);
    setShapeType(ShapeType.POLYGON);
  }

  public FillProperties getFillProperties() {
    return _fillProperties;
  }

  public FillStyle getFillStyle() {
    return _fillProperties.getStyle();
  }

  public RGB getFillColor() {
    return _fillProperties.getRGB();
  }

  public BufferedImage getFillImage() {
    return _fillProperties.getImage();
  }

  public void setFillStyle(final FillStyle fillStyle) {
    _fillProperties.setStyle(fillStyle);
    updated();
  }

  public void setFillColor(final RGB color) {
    _fillProperties.setRGB(color);
    updated();
  }

  public void setFillImage(final BufferedImage fillImage) {
    _fillProperties.setImage(fillImage);
  }

  public void setFillProperties(final FillProperties fillProperties) {
    _fillProperties = fillProperties;
    updated();
  }

  /**
   * Overridden method since points wrap for a polygon.
   */
  @Override
  public IPlotPoint getPrevPoint(final IPlotPoint point) {
    IPlotPoint pointPrev = null;
    int size = _points.size();
    int ndx = _points.indexOf(point);
    if (size < 1) {
      return pointPrev;
    }
    if (ndx > 0 && ndx < size) {
      pointPrev = _points.get(ndx - 1);
    } else if (ndx == 0) {
      pointPrev = _points.get(size - 1);
    }
    return pointPrev;
  }

  /**
   * Overridden method since points wrap for a polygon.
   */
  @Override
  public IPlotPoint getNextPoint(final IPlotPoint point) {
    IPlotPoint pointNext = null;
    int size = _points.size();
    int ndx = _points.indexOf(point);
    if (size < 1) {
      return pointNext;
    }
    if (ndx >= 0 && ndx < size - 1) {
      pointNext = _points.get(ndx + 1);
    } else if (ndx == size - 1) {
      pointNext = _points.get(0);
    }
    return pointNext;
  }

  public boolean isPointInside(final double x, final double y) {
    boolean inside = false;
    int npts = getPointCount();
    double[] px = new double[npts];
    double[] py = new double[npts];
    for (int i = 0; i < npts; i++) {
      px[i] = getPoint(i).getX();
      py[i] = getPoint(i).getY();
    }
    inside = isPointInside(npts, px, py, x, y);
    return inside;
  }

  //  @Override
  //  protected IPlotEditor createEditor() {
  //    return new PlotPolygonEditor(this);
  //  }

  protected boolean isPointInside(final int npts, final double[] px, final double[] py, final double x, final double y) {
    int i;
    double accang; /* Accumulation of angles. */

    /* Start accumulation at zero. */
    accang = 0;

    /* If the region is not closed (ie first and last point different). */
    if (Math.abs(px[npts - 1] - px[0]) > 1e-6 || Math.abs(py[npts - 1] - py[0]) > 1e-6) {
      /* Acquire closure by tieing last point back to first point. */
      accang += computePointAngle(px[npts - 1], py[npts - 1], px[0], py[0], x, y);
    }

    for (i = 1; i < npts; i++) {
      /* Accumulate angles of all adjacent vertices in order. */
      accang += computePointAngle(px[i - 1], py[i - 1], px[i], py[i], x, y);
    }

    /* Accumulated angle will be multiply of 2pi. */
    /* Zero clearly indicates point is outside region. */
    /* 2pi or -2pi clearly indicate point is inside regions. */
    /* Larger multiples indicates region makes loops around point. */
    return accang > 4 || accang < -4;
  }

  protected double computePointAngle(final double x1, final double y1, final double x2, final double y2,
      final double x0, final double y0) {
    double xv1;
    double xv2;
    double yv1;
    double yv2; /* vector components */

    /* Get vectors from x0y0 to x1y1 AND from x0y0 to x2y2. */
    xv1 = x1 - x0;
    yv1 = y1 - y0;
    xv2 = x2 - x0;
    yv2 = y2 - y0;

    /* Find sin (cross product) and cos (dot product). */
    /* Use atan2 to return angle in radians. */
    return Math.atan2(xv1 * yv2 - xv2 * yv1, xv1 * xv2 + yv1 * yv2);
  }

  @Override
  public void dispose() {
    super.dispose();
    _fillProperties.dispose();
  }
}
