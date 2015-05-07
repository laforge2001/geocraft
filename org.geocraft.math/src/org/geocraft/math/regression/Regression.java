/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.math.regression;


/**
 * @deprecated this class is deprecated in favor of regression statistics
 * and the new regression method service.
 */
@Deprecated
public class Regression {

  /** Enumeration for regression methods. */
  public static enum RegressionMethod {
    LeastSquared, Perpendicular, ReducedMeanAverage
  }

  protected RegressionType _type;

  protected RegressionMethod _method;

  protected double _slope;

  protected double _intercept;

  public static RegressionMethod[] getMethods() {
    return new RegressionMethod[] { RegressionMethod.Perpendicular, RegressionMethod.LeastSquared, RegressionMethod.ReducedMeanAverage };
  }

  public Regression(final RegressionType type, final RegressionMethod method, final double slope, final double intercept) {
    _type = type;
    _method = method;
    _slope = slope;
    _intercept = intercept;
  }

  public RegressionType getType() {
    return _type;
  }

  public RegressionMethod getMethod() {
    return _method;
  }

  public double getSlope() {
    return _slope;
  }

  public double getIntercept() {
    return _intercept;
  }

  public String getName() {
    return _method.toString();
  }

  public String getNameAbbr() {
    if (_method.equals(RegressionMethod.LeastSquared)) {
      return "LSQ";
    } else if (_method.equals(RegressionMethod.Perpendicular)) {
      return "PPD";
    } else if (_method.equals(RegressionMethod.ReducedMeanAverage)) {
      return "RMA";
    } else {
      assert false : "Invalid regression method.";
    }
    return "";
  }
}
