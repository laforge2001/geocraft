/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.math;


import org.geocraft.core.common.math.MathUtil;


public class Interpolation {

  /**
   * Performs quadratic interpolation to compute the maximum deflection (+ or -).
   * The point of maximum deflection must be in x[1]. This means that:
   * |x[0]| and |x[2]| should both be less than |x[1]|.
   *
   * @param x0 contains the values at the 1st point.
   * @param x1 contains the values at the 2nd point.
   * @param x2 contains the values at the 3rd point.
   * @return the extreme value
   *
   */
  public static float computeExtreme(float x0, float x1, float x2) {

    float shift = computeShift(x0, x1, x2);

    double max = 0.5 * ((x2 - 2. * x1 + x0) * shift * shift + (x2 - x0) * shift + 2. * x1);

    return (float) max;
  }

  /**
   * Compute the distance between the center value and the interpolated extreme.
   *
   * Shift will always be >= -0.5 and <= 0.5.
   *
   * A positive shift means the extreme is between index 1 and 2.
   *
   * @param x0 contains the values at the 1st point.
   * @param x1 contains the values at the 2nd point.
   * @param x2 contains the values at the 3rd point.
   * @return the distance to the extreme value
   */
  public static float computeShift(float x0, float x1, float x2) {

    double denom = x2 - 2. * x1 + x0;
    double zero = 0.0;
    double shift = 0.0;

    // Make sure that we are not dividing by zero
    if (!MathUtil.isEqual(denom, zero)) {
      shift = -0.5 * (x2 - x0) / (x2 - 2. * x1 + x0);
    }

    return (float) shift;
  }

}
