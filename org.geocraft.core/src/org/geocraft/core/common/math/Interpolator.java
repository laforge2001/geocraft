/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.math;


import java.util.Arrays;


public class Interpolator {

  float[] _x;

  float[] _y;

  public Interpolator(float[] x, float[] y) {

    if (x.length < 3 || y.length < 3 || x.length != y.length) {
      throw new IllegalArgumentException("x and y must be same length and contain 3 or more values");
    }

    _x = x;
    _y = y;
  }

  public float interpolate(float x) {

    if (x < _x[0] || x > _x[_x.length - 1]) {
      throw new IllegalArgumentException("x out of bounds " + x);
    }

    int i = Arrays.binarySearch(_x, x);

    if (i < 0) {
      i = i * -1 - 1;
    }
    //    int i = 1;
    //    while (_x[i] < x) {
    //      i++;
    //    }

    return interpolate(_x[i - 1], _y[i - 1], _x[i], _y[i], _x[i + 1], _y[i + 1], x);

  }

  public static float interpolate(float x0, float y0, float x1, float y1, float x2, float y2, float x) {

    // avoid divide by zero 
    if (x0 == x1 || x0 == x2 || x1 == x2) {
      throw new IllegalArgumentException("x values may not be identical");
    }

    // also avoids divide by zero
    if (x0 == x) {
      return y0;
    } else if (x1 == x) {
      return y1;
    } else if (x2 == x) {
      return y2;
    }

    float y = y0 * (x - x1) * (x - x2) / ((x0 - x1) * (x0 - x2));
    y += y1 * (x - x0) * (x - x2) / ((x1 - x0) * (x1 - x2));
    y += y2 * (x - x0) * (x - x1) / ((x2 - x0) * (x2 - x1));

    return y;
  }

  public static float[] interpolate(float[] x, float[] y, float[] newx) {
    float[] result = new float[newx.length];
    Interpolator interp = new Interpolator(x, y);
    for (int i = 0; i < newx.length; i++) {
      result[i] = interp.interpolate(newx[i]);
    }
    return result;
  }

}
