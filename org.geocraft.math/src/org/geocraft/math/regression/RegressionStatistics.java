/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.math.regression;


import java.text.NumberFormat;


public class RegressionStatistics {

  private final String _regressionName;

  private final double _slope;

  private final double _intercept;

  private final double _error;

  private final NumberFormat _formatter;

  public RegressionStatistics(final RegressionMethodDescription method, final double slope, final double intercept, final double error) {
    this(method.getName() + " (" + method.getAcronym() + ")", slope, intercept, error);
  }

  public RegressionStatistics(final String regressionName, final double slope, final double intercept, final double error) {
    _regressionName = regressionName;
    _slope = slope;
    _intercept = intercept;
    _error = error;
    _formatter = NumberFormat.getInstance();
    _formatter.setMaximumFractionDigits(8);
    _formatter.setGroupingUsed(false);
  }

  public double getSlope() {
    return _slope;
  }

  public double getIntercept() {
    return _intercept;
  }

  public double getError() {
    return _error;
  }

  public String getInfo() {
    StringBuilder builder = new StringBuilder("Regression: " + _regressionName);
    builder.append("\nslope = " + _formatter.format(_slope));
    builder.append("\n-1/slope = " + _formatter.format(-1 / _slope));
    builder.append("\nintercept = " + _formatter.format(_intercept));
    builder.append("\nerror = " + _formatter.format(_error));
    builder.append("\n");
    return builder.toString();
  }
}
