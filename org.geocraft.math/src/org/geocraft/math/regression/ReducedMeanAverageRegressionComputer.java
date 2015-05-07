/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.math.regression;




public class ReducedMeanAverageRegressionComputer extends AbstractRegressionComputer {

  /**
   * Computes the regression properties of a set of points (method 1). This
   * method does force the regression to pass thru (0,0).
   * @param data the regression data.
   * @return the regression statistics.
   */
  @Override
  public RegressionStatistics computeStatisticsThruOrigin(final RegressionData data) {

    double arma = 0;
    double brma = 0;
    double erma = 0;

    RegressionDataStatistics stats = data.getStatistics();
    int npts = stats.getNumPoints();
    double x2sum = stats.getX2Sum();
    double y2sum = stats.getY2Sum();
    double xysum = stats.getXYSum();

    double atrm = -xysum;
    double appd = 0;
    if (atrm == 0) {
      appd = 0;
    } else {
      double btrm = y2sum - x2sum;
      double ctrm = -atrm;
      double dscrm = Math.sqrt(btrm * btrm - 4 * atrm * ctrm);
      appd = -(btrm + dscrm) / (2 * atrm);
      arma = Math.sqrt(y2sum / x2sum);
      if (appd < 0) {
        arma = -arma;
      }

      double bld = 0;
      double gsum = 0;
      double arma2 = arma * arma + 1;
      int j = 0;
      for (int i = 0; i < npts; i++) {
        double tmp = arma * data.getX(i) + bld - data.getY(i);
        double g = tmp * tmp / arma2;
        gsum = gsum + g;
        j++;
      }
      double gold = Math.sqrt(gsum / npts);
      erma = gold;
    }

    return new RegressionStatistics(getMethod(), arma, brma, erma);
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
    double xbar = stats.getXBar();
    double ybar = stats.getYBar();
    double xsum = stats.getXSum();
    double ysum = stats.getYSum();
    double x2sum = stats.getX2Sum();
    double y2sum = stats.getY2Sum();
    double xysum = stats.getXYSum();

    // Least-squares regression.
    double det = npts * x2sum - xsum * xsum;
    double alsq = (npts * xysum - xsum * ysum) / det;

    // Reduced-mean regression.
    double arma = Math.sqrt((x2sum - xsum * xbar) * (y2sum - ysum * ybar)) / (x2sum - xsum * xbar);
    if (alsq < 0) {
      arma = -arma;
    }
    double brma = ybar - arma * xbar;
    double rsum = (y2sum - 2 * brma * ysum + npts * brma * brma) / arma - 2 * xysum + 2 * brma * xsum + arma * xysum;
    rsum = 0;
    for (int j = 0; j < npts; j++) {
      double tmp = arma * data.getX(j) + brma - data.getY(j);
      rsum += tmp * tmp;
    }

    double erma = rsum;

    return new RegressionStatistics(getMethod(), arma, brma, erma);
  }

}
