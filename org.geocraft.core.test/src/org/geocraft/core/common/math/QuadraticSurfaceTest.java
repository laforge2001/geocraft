/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.math;


import junit.framework.TestCase;


public class QuadraticSurfaceTest extends TestCase {

  public void testFit() {
    double[][] input = new double[3][3];

    double a = 1;
    double b = 2;
    double c = 3;
    double d = 4;
    double e = 5;
    double f = 6;

    for (int row = 0; row < 3; row++) {
      double y = 1 - row;
      for (int col = 0; col < 3; col++) {
        double x = col - 1;
        input[row][col] = a * x * x + b * y * y + c * x * y + d * x + e * y + f;
      }
    }

    QuadraticSurface qs = new QuadraticSurface(input, 1, 1);

    assertEquals(a, qs.a, Constants.FLOAT_DELTA);
    assertEquals(b, qs.b, Constants.FLOAT_DELTA);
    assertEquals(c, qs.c, Constants.FLOAT_DELTA);
    assertEquals(d, qs.d, Constants.FLOAT_DELTA);
    assertEquals(e, qs.e, Constants.FLOAT_DELTA);
    assertEquals(f, qs.f, Constants.FLOAT_DELTA);
  }

  public void testDifferentDxDy() {
    double[][] input = new double[3][3];

    double a = 1;
    double b = 2;
    double c = 3;
    double d = 4;
    double e = 5;
    double f = 6;

    double dx = 1;
    double dy = 2;

    for (int row = 0; row < 3; row++) {
      double y = 2 * (1 - row);
      for (int col = 0; col < 3; col++) {
        double x = col - 1;
        input[row][col] = a * x * x + b * y * y + c * x * y + d * x + e * y + f;
      }
    }

    QuadraticSurface qs = new QuadraticSurface(input, dx, dy);

    assertEquals(a, qs.a, Constants.FLOAT_DELTA);
    assertEquals(b, qs.b, Constants.FLOAT_DELTA);
    assertEquals(c, qs.c, Constants.FLOAT_DELTA);
    assertEquals(d, qs.d, Constants.FLOAT_DELTA);
    assertEquals(e, qs.e, Constants.FLOAT_DELTA);
    assertEquals(f, qs.f, Constants.FLOAT_DELTA);
  }

  public void testAzimuth() {
    double[][] input = new double[3][3];

    double a = 100 * (Math.random() - 0.5);
    double b = 100 * (Math.random() - 0.5);
    double c = 100 * (Math.random() - 0.5);
    double d = 100 * (Math.random() - 0.5);
    double e = 100 * (Math.random() - 0.5);
    double f = 100 * (Math.random() - 0.5);

    for (int row = 0; row < 3; row++) {
      double y = 1 - row;
      for (int col = 0; col < 3; col++) {
        double x = col - 1;
        input[row][col] = a * x * x + b * y * y + c * x * y + d * x + e * y + f;
        System.out.println(x + " " + y + " " + input[row][col]);
      }
    }

    QuadraticSurface qs = new QuadraticSurface(input, 1, 1);

    assertEquals(a, qs.a, Constants.FLOAT_DELTA);
    assertEquals(b, qs.b, Constants.FLOAT_DELTA);
    assertEquals(c, qs.c, Constants.FLOAT_DELTA);
    assertEquals(d, qs.d, Constants.FLOAT_DELTA);
    assertEquals(e, qs.e, Constants.FLOAT_DELTA);
    assertEquals(f, qs.f, Constants.FLOAT_DELTA);

    System.out.println(qs.getEquation());

    double angle = qs.getDipAzimuth() + Math.PI / 2;
    double dx = Math.cos(angle);
    double dy = Math.sin(angle);

    // plot the strike line
    dx = Math.cos(angle);
    dy = Math.sin(angle);

    System.out.println(.2 * dx + " " + .2 * dy + " " + input[1][1]);
    System.out.println(-.2 * dx + " " + -.2 * dy + " " + input[1][1]);

    System.out.println();
    System.out.println(Math.toDegrees(qs.getDipAzimuth()) + " " + Math.toDegrees(qs.getStrikeAzimuth()) + " "
        + Math.toDegrees(qs.getMaximumCurvatureAzimuth()));
  }
}
