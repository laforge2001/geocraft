/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.math;


/**
 * Fits a quadratic surface to a 3 by 3 array of z values. 
 * 
 * z = f(x,y) = a*x*x + b*y*y + c*x*y + d*x + e*y + f
 * 
 * The order of the zValues' rows is opposite to how GeoCraft 
 * stores it's data in a horizon. 
 * 
 *  z1 z2 z3
 *  z4 z5 z6
 *  z7 z8 z9
 * 
 * See paper by Andy Roberts in FirstBreak vol 19.2 February 2001
 * 
 * This is actually a special case of a least squares solution 
 * to surface fitting that does not require a matrix inversion. 
 * 
 * It seems to work when dx and dy are not equal but the original 
 * paper (Evans 1979) does not mention this so use with care. 
 */
public class QuadraticSurface {

  public double a, b, c, d, e, f;

  private final double[] zs = new double[9];

  public QuadraticSurface(final double[][] zValues, final double dx, final double dy) {

    double dx2 = dx * dx;
    double dy2 = dy * dy;

    // z1 - z2 - z3
    //  |    |   |
    // z4 - z5 - z6
    //  |    |   |
    // z7 - z8 - z9

    double z1 = zValues[0][0];
    double z2 = zValues[0][1];
    double z3 = zValues[0][2];

    double z4 = zValues[1][0];
    double z5 = zValues[1][1];
    double z6 = zValues[1][2];

    double z7 = zValues[2][0];
    double z8 = zValues[2][1];
    double z9 = zValues[2][2];

    zs[0] = z1;
    zs[1] = z2;
    zs[2] = z3;
    zs[3] = z4;
    zs[4] = z5;
    zs[5] = z6;
    zs[6] = z7;
    zs[7] = z8;
    zs[8] = z9;

    a = (z1 + z3 + z4 + z6 + z7 + z9) / (6 * dx2) - (z2 + z5 + z8) / (3 * dx2);
    b = (z1 + z2 + z3 + z7 + z8 + z9) / (6 * dy2) - (z4 + z5 + z6) / (3 * dy2);
    c = (z3 + z7 - z1 - z9) / (4 * dx * dy);
    d = (z3 + z6 + z9 - z1 - z4 - z7) / (6 * dx);
    e = (z1 + z2 + z3 - z7 - z8 - z9) / (6 * dy);
    f = (2 * (z2 + z4 + z6 + z8) - (z1 + z3 + z7 + z9) + 5 * z5) / 9;
  }

  public String getEquation() {
    return String.format("%9.6f * x * x + %9.6f * y * y + %9.6f * x * y + %9.6f * x + %9.6f * y + %9.6f", a, b, c, d,
        e, f);
  }

  public double getPoint(final double x, final double y) {
    if (x < -1 || x > 1 || y < -1 || y > 1) {
      throw new IllegalArgumentException("-1 < x,y < 1 " + x + " " + y);
    }

    return a * x * x + b * y * y + c * x * y + d * x + e * y + f;
  }

  /**
   * Marfurt Geophys Developments No. 11 P75
   * 
   * Note that maximum curvature is not the same as the direction of dip. 
   * 
   * @return
   */
  public double getMaximumCurvatureAzimuth() {
    if (a == b) {
      return Double.NaN;
    }
    return Math.atan(c / (a - b));
  }

  /**
   * Taken from J Wood's thesis. 
   * 
   * @return
   */
  public double getDipAzimuth() {
    if (a == b) {
      return Double.NaN;
    }
    return Math.atan(e / d);
  }

  public double getDipAngle() {
    return Math.atan(d * d / e * e);
  }

  public double getDipCurvature() {

    double denom = d * d + e * e;
    denom = denom * Math.pow(1 + denom, 1.5);

    return 2 * (a * d * d + b * e * e + c * d * e) / denom;
  }

  /** 
   * Brute force search for the dip of a quadratic surface. 
   * 
   * @return strike angle in radians 0 < strike < Pi
   */
  public double getStrikeAzimuth() {
    return getDipAzimuth() + Math.PI / 2;
  }

  public double getStrikeCurvature() {

    double denom = d * d + e * e;
    denom = denom * Math.sqrt(1 + denom);
    if (denom == 0) {
      return Double.NaN;
    }

    return 2 * (a * e * e + b * d * d - c * d * e) / denom;
  }

  public double getSlope() {
    return Math.atan(Math.sqrt(d * d + e * e));
  }

  public double getCrossCurvature() {
    return (b * d * d + a * e * e) / (d * d + e * e);
  }

  /**
   * This is different to the Marfurt paper where he has a + b
   * or is it -a - b?
   * @return
   */
  public double getNegativeCurvature() {
    return a + b - Math.sqrt((a - b) * (a - b) + c * c);
  }

  /**
   * This is different to the Marfurt paper where he has a + b 
   * or is it -a - b?
   * @return
   */
  public double getPositiveCurvature() {
    return a + b + Math.sqrt((a - b) * (a - b) + c * c);
  }

  public double getMeanCurvature() {
    return (a * (1 + e * e) - c * d * e + b * (1 + d * d)) / Math.pow((1 + d * d + e * e), 1.5);
  }

  public double getMaximumCurvature() {
    double km = getMeanCurvature();
    return km + Math.sqrt(km * km - getGaussianCurvature());
  }

  public double getMinimumCurvature() {
    double km = getMeanCurvature();
    return km - Math.sqrt(km * km - getGaussianCurvature());
  }

  public double getGaussianCurvature() {
    double denom = 1 + d * d + e * e;
    return (4 * a * b - c * c) / (denom * denom);
  }

  public double getContourCurvature() {
    // hmm I have seen 1 + e*e + d * d also...... 
    double denom = Math.pow(d * d + e * e, 1.5);
    return 2 * (a * e * e + b * d * d - c * d * e) / denom;
  }

  public double getCurvedness() {
    double kMax = getMaximumCurvature();
    double kMin = getMinimumCurvature();
    return Math.sqrt((kMax * kMax + kMin * kMin) / 2.0);
  }

  public double getShapeIndex() {
    double kMax = getMaximumCurvature();
    double kMin = getMinimumCurvature();
    double denom = kMax - kMin;
    if (denom == 0) {
      return Double.NaN;
    }
    return 2 * Math.PI * Math.atan((kMin + kMax) / denom);
  }

  public void print() {
    for (int i = 0; i < zs.length; i++) {
      System.out.println("Z[" + (i + 1) + "] = " + zs[i]);
    }
  }

}
