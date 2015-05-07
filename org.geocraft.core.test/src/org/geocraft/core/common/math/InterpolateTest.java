/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.math;


import junit.framework.TestCase;


public class InterpolateTest extends TestCase {

  public void testSimple() {
    double y = Interpolator.interpolate(1, 2, 2, 3, 3, 1, 2.5f);
    assertEquals(2.375, y, Constants.FLOAT_DELTA);
  }

  public void testMultiple() {
    float[] x = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    float[] y = { 0, 1, 0, 2, 0, 1, 0, 3, 0, 1 };
    float[] res = { 0.75f, 0.125f, 1.5f, 0.625f, 0.75f, 0, 2.25f, 1 };

    float[] ix = new float[x.length - 2];

    for (int i = 0; i < x.length - 2; i++) {
      ix[i] = i + 0.5f;
      float r = Interpolator.interpolate(x[i], y[i], x[i + 1], y[i + 1], x[i + 2], y[i + 2], ix[i]);
      assertEquals(res[i], r, Constants.FLOAT_DELTA);
    }

    Interpolator interp = new Interpolator(x, y);

    for (int i = 0; i < x.length - 2; i++) {
      float r2 = interp.interpolate(ix[i]);
      assertEquals(res[i], r2, Constants.FLOAT_DELTA);
    }

    float[] r3 = Interpolator.interpolate(x, y, ix);
    for (int i = 0; i < r3.length; i++) {
      assertEquals(res[i], r3[i], Constants.FLOAT_DELTA);
    }
  }

  public void testError() {
    float y = Interpolator.interpolate(1, 2, 2, 3, 3, 1, 1);
    assertEquals(2, y, Constants.FLOAT_DELTA);
    y = Interpolator.interpolate(1, 2, 2, 3, 3, 1, 2);
    assertEquals(3, y, Constants.FLOAT_DELTA);
    y = Interpolator.interpolate(1, 2, 2, 3, 3, 1, 3);
    assertEquals(1, y, Constants.FLOAT_DELTA);
  }
}
