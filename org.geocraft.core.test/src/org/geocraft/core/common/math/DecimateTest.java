/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.math;


import junit.framework.TestCase;


public class DecimateTest extends TestCase {

  public void testTwoPoints() {
    double[][] points = new double[][] { { 0, 0 }, { 1, 1 } };
    double[][] res = Decimate.decimate(points, 100);
    assertTrue(areArraysEqual(points, res));
  }

  public void testStraightLine() {
    double[][] points = new double[][] { { 0, 0 }, { 1, 1 }, { 2, 2 } };
    double[][] res = Decimate.decimate(points, 100);
    assertEquals(2, res.length);
  }

  public void testThreePoints() {
    double[][] points = new double[][] { { 0, 0 }, { 1.1, 1.2 }, { 2, 2 } };
    double[][] res = Decimate.decimate(points, 100);
    assertEquals(2, res.length);
  }

  // this one is weird because {1000, 0} forces the algorithm to also
  // select the {1.1, 1.2} point. 
  public void testFourPoints() {
    double[][] points = new double[][] { { 0, 0 }, { 1.1, 1.2 }, { 1000, 0 }, { 2, 2 } };
    double[][] res = Decimate.decimate(points, 1);
    assertEquals(4, res.length);
  }

  public void testPoints() {
    double[][] points = new double[][] { { 0, 0 }, { 1.1, 1.2 }, { 2, 2 } };
    double[][] res = Decimate.decimate(points, 100);
    assertEquals(2, res.length);
  }

  public void XXtestMany() {
    double[][] points = new double[1000][2];

    System.out.println("0 0");
    for (int i = 1; i < points.length; i++) {
      points[i][0] = i;
      points[i][1] = 10 * Math.sin(i / 100.0) + Math.random();
      System.out.println(points[i][0] + " " + points[i][1]);
    }

    System.out.println("\n\n");

    double[][] res = Decimate.decimate(points);
    for (double[] re : res) {
      System.out.println(re[0] + " " + re[1]);
    }
  }

  boolean areArraysEqual(final double[][] array1, final double[][] array2) {
    for (int i = 0; i < array1.length; i++) {
      for (int j = 0; j < array1[0].length; j++) {
        if (array1[i][j] != array2[i][j]) {
          return false;
        }
      }
    }
    return true;
  }

}
