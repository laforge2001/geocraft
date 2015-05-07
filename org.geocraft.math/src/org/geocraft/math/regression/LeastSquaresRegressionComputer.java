/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.math.regression;




public class LeastSquaresRegressionComputer extends AbstractRegressionComputer {

  /**
   * Computes the regression properties of a set of points (method 1). This
   * method does force the regression to pass thru (0,0).
   * @param data the regression data.
   * @return the regression statistics.
   */
  @Override
  public RegressionStatistics computeStatisticsThruOrigin(final RegressionData data) {

    double alsq = 0;
    double blsq = 0;
    double elsq = 0;

    RegressionDataStatistics stats = data.getStatistics();
    int npts = stats.getNumPoints();
    double x2sum = stats.getX2Sum();
    double xysum = stats.getXYSum();

    double atrm = -xysum;
    if (atrm == 0) {
      // No action.
    } else {
      alsq = xysum / x2sum;

      double fsum = 0;
      double c2 = alsq * alsq + 1;
      int j = 0;
      for (int i = 0; i < npts; i++) {
        double tmp = alsq * data.getX(i) - data.getY(i);
        double f = tmp * tmp / c2;
        fsum = fsum + f;
        j++;
      }
      elsq = Math.sqrt(fsum / npts);
    }
    RegressionMethodDescription desc = getMethod();
    if (desc == null) {
      return new RegressionStatistics("Least-squares", alsq, blsq, elsq);
    }
    return new RegressionStatistics(getMethod(), alsq, blsq, elsq);
  }

  /**
   * Computes the regression properties of a set of points (method 2). This
   * method does NOT force the regression to pass thru (0,0).
   * @param data the regression data.
   * @return the regression statistics.
   */
  @Override
  public RegressionStatistics computeStatistics(final RegressionData data) {

    RegressionDataStatistics stats = data.getStatistics();
    int npts = stats.getNumPoints();
    double ybar = stats.getYBar();
    double xsum = stats.getXSum();
    double ysum = stats.getYSum();
    double x2sum = stats.getX2Sum();
    double y2sum = stats.getY2Sum();
    double xysum = stats.getXYSum();

    // Least-squares regression.
    double det = npts * x2sum - xsum * xsum;
    double alsq = (npts * xysum - xsum * ysum) / det;
    double blsq = (ysum * x2sum - xsum * xysum) / det;
    double elsq = y2sum - ysum * ybar - alsq * (xysum - xsum * ybar);

    RegressionMethodDescription desc = getMethod();
    if (desc == null) {
      return new RegressionStatistics("Least-squares", alsq, blsq, elsq);
    }
    return new RegressionStatistics(getMethod(), alsq, blsq, elsq);
  }

}
