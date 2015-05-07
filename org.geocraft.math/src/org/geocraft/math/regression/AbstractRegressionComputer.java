/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.math.regression;


public abstract class AbstractRegressionComputer {

  private RegressionMethodDescription _method;

  public RegressionStatistics compute(final RegressionType type, final RegressionData data) {
    int npts = data.getNumPoints();
    if (npts > 0 && (type.equals(RegressionType.Origin) || type.equals(RegressionType.Offset))) {
      if (type.equals(RegressionType.Origin)) {
        return computeStatisticsThruOrigin(data);
      } else if (type.equals(RegressionType.Offset)) {
        return computeStatistics(data);
      } else {
        throw new IllegalArgumentException("Invalid regression type.");
      }
    }
    if (npts == 0) {
      throw new IllegalArgumentException("Regression Error: No points in the series.");
    }
    throw new IllegalArgumentException("Regression Error: Invalid regression type.");
  }

  public RegressionMethodDescription getMethod() {
    return _method;
  }

  public void setMethod(final RegressionMethodDescription method) {
    _method = method;
  }

  /**
   * Computes the regression for arrays of double-precision x,y values.
   * @param data the regression data.
   * @return the computed regression.
   */
  public abstract RegressionStatistics computeStatistics(RegressionData data);

  /**
   * Computes the origin-constrained regression for arrays of double-precision x,y values.
   * @param data the regression data.
   * @return the computed regression.
   */
  public abstract RegressionStatistics computeStatisticsThruOrigin(RegressionData data);

}
