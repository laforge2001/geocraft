/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.math.regression;


import java.text.NumberFormat;


public class RegressionDataStatistics {

  private final int _numPoints;

  private final double _xsum;

  private final double _ysum;

  private final double _x2sum;

  private final double _y2sum;

  private final double _xysum;

  private final double _xbar;

  private final double _ybar;

  private final double _xvar;

  private final double _yvar;

  private final double _ysig;

  private final double _xsig;

  private final double _ccor;

  private final NumberFormat _formatter;

  public RegressionDataStatistics(final int numPoints, final double xsum, final double ysum, final double x2sum, final double y2sum, final double xysum, final double xbar, final double ybar, final double xvar, final double yvar, final double xsig, final double ysig, final double ccor) {
    _numPoints = numPoints;
    _xsum = xsum;
    _ysum = ysum;
    _x2sum = x2sum;
    _y2sum = y2sum;
    _xysum = xysum;
    _xbar = xbar;
    _ybar = ybar;
    _xvar = xvar;
    _yvar = yvar;
    _xsig = xsig;
    _ysig = ysig;
    _ccor = ccor;
    _formatter = NumberFormat.getInstance();
    _formatter.setMaximumFractionDigits(8);
    _formatter.setGroupingUsed(false);
  }

  public int getNumPoints() {
    return _numPoints;
  }

  public double getXSum() {
    return _xsum;
  }

  public double getYSum() {
    return _ysum;
  }

  public double getX2Sum() {
    return _x2sum;
  }

  public double getY2Sum() {
    return _y2sum;
  }

  public double getXYSum() {
    return _xysum;
  }

  public double getXBar() {
    return _xbar;
  }

  public double getYBar() {
    return _ybar;
  }

  public double getXVar() {
    return _xvar;
  }

  public double getYVar() {
    return _yvar;
  }

  public double getXSigma() {
    return _xsig;
  }

  public double getYSigma() {
    return _ysig;
  }

  public double getCorrelationCoefficient() {
    return _ccor;
  }

  public String getInfo() {
    String sigmaUpper = "\u03A3";
    String sigmaLower = "\u03C3";
    String muLower = "\u03BC";
    StringBuilder builder = new StringBuilder();
    builder.append("Number of points = " + _numPoints);
    builder.append("\n" + sigmaUpper + "A     = " + _formatter.format(_xsum));
    builder.append("\n" + sigmaUpper + "B     = " + _formatter.format(_ysum));
    builder.append("\n" + sigmaUpper + "(A*A) = " + _formatter.format(_x2sum));
    builder.append("\n" + sigmaUpper + "(B*B) = " + _formatter.format(_y2sum));
    builder.append("\n" + sigmaUpper + "(A*B) = " + _formatter.format(_xysum));
    builder.append("\nA Mean (" + muLower + ")       = " + _formatter.format(_xbar));
    builder.append("\nB Mean (" + muLower + ")       = " + _formatter.format(_ybar));
    builder.append("\nA Variance (" + sigmaLower + "^2) = " + _formatter.format(_xvar));
    builder.append("\nB Variance (" + sigmaLower + "^2) = " + _formatter.format(_yvar));
    builder.append("\nA Std. Dev. (" + sigmaLower + ")  = " + _formatter.format(_xsig));
    builder.append("\nB Std. Dev. (" + sigmaLower + ")  = " + _formatter.format(_ysig));
    builder.append("\nCorrelation Coeff. = " + _formatter.format(_ccor));
    return builder.toString();
  }
}
