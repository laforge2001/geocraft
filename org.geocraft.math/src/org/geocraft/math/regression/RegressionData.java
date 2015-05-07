/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.math.regression;


public class RegressionData {

  private final double[] _x;

  private final double[] _y;

  private final RegressionDataStatistics _stats;

  public RegressionData(final float[] x, final float[] y) {
    this(convertFloatToDouble(x), convertFloatToDouble(y));
  }

  public RegressionData(final double[] x, final double[] y) {
    if (x.length != y.length) {
      throw new IllegalArgumentException("Number of points in arrays do not match!");
    }
    int numPoints = x.length;
    _x = new double[numPoints];
    _y = new double[numPoints];
    System.arraycopy(x, 0, _x, 0, numPoints);
    System.arraycopy(y, 0, _y, 0, numPoints);
    double xsum = 0;
    double ysum = 0;
    double x2sum = 0;
    double y2sum = 0;
    double xysum = 0;
    for (int i = 0; i < numPoints; i++) {
      xsum += x[i];
      ysum += y[i];
      x2sum += x[i] * x[i];
      y2sum += y[i] * y[i];
      xysum += x[i] * y[i];
    }

    double xbar = xsum / numPoints;
    double ybar = ysum / numPoints;
    double xvar = x2sum / numPoints - xbar * xbar;
    double yvar = y2sum / numPoints - ybar * ybar;
    double xsig = Math.sqrt(xvar);
    double ysig = Math.sqrt(yvar);
    double ccor = 1;
    if (xsig * ysig > 0.00000000001) {
      ccor = (xysum / numPoints - xbar * ybar) / (xsig * ysig);
    }
    _stats = new RegressionDataStatistics(numPoints, xsum, ysum, x2sum, y2sum, xysum, xbar, ybar, xvar, yvar, xsig, ysig, ccor);
  }

  public double getX(final int index) {
    return _x[index];
  }

  public double getY(final int index) {
    return _y[index];
  }

  public RegressionDataStatistics getStatistics() {
    return _stats;
  }

  /**
   * Returns the number of x,y values.
   * @return the number of x,y values.
   */
  public int getNumPoints() {
    return _x.length;
  }

  /**
   * Converts an array of floats to an array of doubles.
   * @param f an array of floats.
   * @return an array of doubles.
   */
  private static double[] convertFloatToDouble(final float[] f) {
    double[] d = new double[f.length];
    for (int i = 0; i < f.length; i++) {
      d[i] = f[i];
    }
    return d;
  }
}
