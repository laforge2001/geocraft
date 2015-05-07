/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.math.regression;




public class PerpendicularRegressionComputer extends AbstractRegressionComputer {

  /**
   * Computes the regression properties of a set of points (method 1). This
   * method does force the regression to pass thru (0,0).
   * @param data the regression data.
   * @return the regression statistics.
   */
  @Override
  public RegressionStatistics computeStatisticsThruOrigin(final RegressionData data) {

    double appd = 0;
    double bppd = 0;
    double eppd = 0;

    RegressionDataStatistics stats = data.getStatistics();
    int npts = stats.getNumPoints();
    double x2sum = stats.getX2Sum();
    double y2sum = stats.getY2Sum();
    double xysum = stats.getXYSum();

    double emax = -1;
    double atrm = -xysum;
    if (atrm == 0) {
      appd = 0;
    } else {
      double btrm = y2sum - x2sum;
      double ctrm = -atrm;
      double dscrm = Math.sqrt(btrm * btrm - 4 * atrm * ctrm);
      appd = -(btrm + dscrm) / (2 * atrm);

      double a2 = appd * appd + 1;
      int j = 0;
      double esum = 0;
      for (int i = 0; i < npts; i++) {
        double tmp = appd * data.getX(i) - data.getY(i);
        double e = tmp * tmp / a2;
        if (emax < e) {
          emax = e;
        }
        esum = esum + e;
        j++;
      }
      esum = 0.5 * esum * a2 / Math.abs(appd);
      eppd = Math.sqrt(esum / npts);
    }
    return new RegressionStatistics(getMethod(), appd, bppd, eppd);
  }

  /**
   * Computes the regression properties of a set of points (method 2). This
   * method does NOT force the regression to pass thru (0,0).
   * @param data the regression data.
   * @return the regression statistics.
   */
  @Override
  public RegressionStatistics computeStatistics(final RegressionData data) {
    int i;
    int j;
    double[] qroots;
    double[] psba = new double[2];
    double[] psbb = new double[3];
    double[] sds = new double[3];

    RegressionDataStatistics stats = data.getStatistics();
    int npts = stats.getNumPoints();
    double xbar = stats.getXBar();
    double ybar = stats.getYBar();
    double xsum = stats.getXSum();
    double ysum = stats.getYSum();
    double x2sum = stats.getX2Sum();
    double y2sum = stats.getY2Sum();
    double xysum = stats.getXYSum();

    // Min-distance (perpendicular) regression.
    double sqcoef = xysum - xsum * ybar;
    double lncoef = x2sum - xsum * xbar - y2sum + ysum * ybar;
    double cscoef = -xysum + ysum * xbar;
    qroots = quadraticRoots(sqcoef, lncoef, cscoef);
    for (i = 0; i < 2 && !Double.isNaN(qroots[i]); i++) {
      psba[i] = qroots[i];
      double tmpa = psba[i];
      double tmpb = ybar - psba[i] * xbar;
      double tmpd = 1 / (psba[i] * psba[i] + 1) * (y2sum - tmpa * xysum - tmpb * ysum + tmpa * tmpa * x2sum + tmpa * tmpb * xsum + tmpb * tmpb * npts);
      tmpd = 0;
      for (j = 0; j < npts; j++) {
        double tmp = tmpa * data.getX(j) + tmpb - data.getY(j);
        tmpd += tmp * tmp;
      }
      for (j = i; j > 0; j--) {
        if (sds[j - 1] > tmpd) {
          sds[j] = sds[j - 1];
          psba[j] = psba[j - 1];
        } else {
          break;
        }
      }
      sds[j] = tmpd;
      psba[j] = tmpa;
      psbb[j] = tmpb;
    }
    double appd = psba[0];
    double bppd = psbb[0];
    double eppd = sds[0];

    return new RegressionStatistics(getMethod(), appd, bppd, eppd);
  }

  /**
   * Generates the quadratic roots.
   * @return the quadratic roots.
   */
  protected static double[] quadraticRoots(final double p, final double q, final double r) {
    double[] y = new double[2];
    double discriminant;
    double b = 0.5 * q / p;
    double c = r / p;
    discriminant = b * b - c;
    if (discriminant >= 0) {
      y[0] = new Double(-b + Math.sqrt(discriminant));
      y[1] = new Double(-b - Math.sqrt(discriminant));
    } else {
      y[0] = new Double(Double.NaN);
      y[0] = new Double(Double.NaN);
    }
    return y;
  }
}
