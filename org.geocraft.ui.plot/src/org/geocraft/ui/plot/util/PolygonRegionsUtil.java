/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.util;


import org.geocraft.ui.plot.object.IPlotPolygon;


public class PolygonRegionsUtil {

  /**
   * Returns flag indicating if point (x,y) is inside the polygon.
   * @param polygon the polygon.
   * @param x the x-coordinate to check.
   * @param y the y-coordinate to check.
   * @return true if x,y is inside polygon; otherwise false.
   */
  public static boolean isPointInside(final IPlotPolygon polygon, final double x, final double y) {
    if (polygon.getPointCount() <= 2) {
      return false;
    }
    double[] xs = new double[polygon.getPointCount()];
    double[] ys = new double[polygon.getPointCount()];
    for (int i = 0; i < polygon.getPointCount(); i++) {
      xs[i] = polygon.getPoint(i).getX();
      ys[i] = polygon.getPoint(i).getY();
    }
    return isPointInside(polygon.getPointCount(), xs, ys, x, y);
  }

  /**
   * Returns flag indicating if point (x,y) is inside the polygon defined by points xs,ys.
   * @param npts the number of point in the polygon.
   * @param xs the array of polygon x-coordinates.
   * @param ys the array of polygon y-coordinates.
   * @param x the x-coordinate to check.
   * @param y the y-coordinate to check.
   * @return true if x,y is inside polygon; otherwise false.
   */
  public static boolean isPointInside(final int npts, final double[] xs, final double[] ys, final double x, final double y) {

    int i;
    double accang; /* Accumulation of angles. */

    /* Start accumulation at zero. */
    accang = 0;

    /* If the region is not closed (ie first and last point different). */
    if (Math.abs(xs[npts - 1] - xs[0]) > 1e-6 || Math.abs(ys[npts - 1] - ys[0]) > 1e-6) {

      /* Acquire closure by tieing last point back to first point. */
      accang += computePointAngle(xs[npts - 1], ys[npts - 1], xs[0], ys[0], x, y);
    }
    for (i = 1; i < npts; i++) {

      /* Accumulate angles of all adjacent vertices in order. */
      accang += computePointAngle(xs[i - 1], ys[i - 1], xs[i], ys[i], x, y);
    }

    /* Accumulated angle will be multiply of 2PI. */
    /* Zero clearly indicates point is outside region. */
    /* 2PI or -2PI clearly indicate point is inside regions. */
    /* Larger multiples indicates region makes loops around point. */
    return accang > 4 || accang < -4;
  }

  /**
   * Returns flag indicating if point (x,y) is inside the polygon defined by points xs,ys.
   * @param npts the number of point in the polygon.
   * @param xs the array of polygon x-coordinates.
   * @param ys the array of polygon y-coordinates.
   * @param x the x-coordinate to check.
   * @param y the y-coordinate to check.
   * @return true if x,y is inside polygon; otherwise false.
   */
  public static boolean isPointInside(final int npts, final float[] xs, final float[] ys, final double x, final double y) {

    int i;
    double accang; /* Accumulation of angles. */

    /* Start accumulation at zero. */
    accang = 0;

    /* If the region is not closed (ie first and last point different). */
    if (Math.abs(xs[npts - 1] - xs[0]) > 1e-6 || Math.abs(ys[npts - 1] - ys[0]) > 1e-6) {

      /* Acquire closure by tieing last point back to first point. */
      accang += computePointAngle(xs[npts - 1], ys[npts - 1], xs[0], ys[0], x, y);
    }
    for (i = 1; i < npts; i++) {

      /* Accumulate angles of all adjacent vertices in order. */
      accang += computePointAngle(xs[i - 1], ys[i - 1], xs[i], ys[i], x, y);
    }

    /* Accumulated angle will be multiply of 2PI. */
    /* Zero clearly indicates point is outside region. */
    /* 2PI or -2PI clearly indicate point is inside regions. */
    /* Larger multiples indicates region makes loops around point. */
    return accang > 4 || accang < -4;
  }

  /**
   * Returns an angle accumulation used by PointInside.
   * @return the angle used by PointInside.
   */
  public static double computePointAngle(final double x1, final double y1, final double x2, final double y2, final double x0, final double y0) {

    /* Get vectors from x0,y0 to x1,y1 AND from x0,y0 to x2,y2. */
    double xv1 = x1 - x0;
    double yv1 = y1 - y0;
    double xv2 = x2 - x0;
    double yv2 = y2 - y0;

    /* Find sin (cross product) and cos (dot product). */
    /* Use atan2 to return angle in radians. */
    return Math.atan2(xv1 * yv2 - xv2 * yv1, xv1 * xv2 + yv1 * yv2);
  }
}
