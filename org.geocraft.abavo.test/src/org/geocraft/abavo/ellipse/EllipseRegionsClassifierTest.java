/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.ellipse;


import junit.framework.TestCase;


public class EllipseRegionsClassifierTest extends TestCase {

  private static double getValue(final double radius, final double x) {
    return Math.sqrt(radius * radius - x * x);
  }

  /**
   * The main test.
   */
  public void testMain() {
    double sqrt50 = Math.sqrt(50);
    double minEllipseSlope = -1;
    double maxEllipseSlope = -2;
    double minEllipseLength = 10;
    double minEllipseWidth = 10;
    double maxEllipseLength = 22;
    double maxEllipseWidth = 22;
    double[] outerXs = { 1, -1, -100, -100, -99, -1, 1, 100, 100, 99 };
    double[] outerYs = { -100, -100, 0, 99, 100, 100, 100, 0, -99, -100 };
    double[] innerXs = { 1, -1, -10, -sqrt50, -sqrt50, -1, 1, 10, sqrt50, sqrt50 };
    double[] innerYs = { -getValue(10, 1), -getValue(10, 1), 0, sqrt50, sqrt50, getValue(10, 1), getValue(10, 1), 0,
        -sqrt50, -sqrt50 };
    double xmin = -100;
    double xmax = 100;
    double ymin = -100;
    double ymax = 100;
    double normalization = 128;
    double xctr = 0;
    double yctr = 0;
    EllipseRegionsClassifier process = null;
    try {
      process = new EllipseRegionsClassifier(minEllipseSlope, minEllipseLength, minEllipseWidth, maxEllipseSlope,
          maxEllipseLength, maxEllipseWidth, outerXs, outerYs, innerXs, innerYs, normalization, xmin, xmax, ymin, ymax,
          xctr, yctr);
      fail("Should have failed due to differing ellipse slopes.");
    } catch (Exception e) {
      assertTrue(true);
    }

    maxEllipseSlope = -1;
    try {
      process = new EllipseRegionsClassifier(minEllipseSlope, minEllipseLength, minEllipseWidth, maxEllipseSlope,
          maxEllipseLength, maxEllipseWidth, outerXs, outerYs, innerXs, innerYs, normalization, xmin, xmax, ymin, ymax,
          xctr, yctr);
    } catch (Exception e) {
      fail();
    }

    // Test region P1
    assertTrue(Double.isNaN(process.processAB(1.1, -9)));
    assertEquals(6.0, process.processAB(1.1, -11));
    assertEquals(10.0, process.processAB(1.1, -13));
    assertEquals(14.0, process.processAB(1.1, -15));
    assertEquals(18.0, process.processAB(1.1, -17));
    assertEquals(22.0, process.processAB(1.1, -19));
    assertEquals(26.0, process.processAB(1.1, -21));
    assertEquals(30.0, process.processAB(1.1, -23));

    // Test region N1
    assertTrue(Double.isNaN(process.processAB(-1.1, 9)));
    assertEquals(-6.0, process.processAB(-1.1, 11));
    assertEquals(-10.0, process.processAB(-1.1, 13));
    assertEquals(-14.0, process.processAB(-1.1, 15));
    assertEquals(-18.0, process.processAB(-1.1, 17));
    assertEquals(-22.0, process.processAB(-1.1, 19));
    assertEquals(-26.0, process.processAB(-1.1, 21));
    assertEquals(-30.0, process.processAB(-1.1, 23));

    // Test region P2
    assertTrue(Double.isNaN(process.processAB(0, -9)));
    assertEquals(38.0, process.processAB(0, -11));
    assertEquals(42.0, process.processAB(0, -13));
    assertEquals(46.0, process.processAB(0, -15));
    assertEquals(50.0, process.processAB(0, -17));
    assertEquals(54.0, process.processAB(0, -19));
    assertEquals(58.0, process.processAB(0, -21));
    assertEquals(62.0, process.processAB(0, -23));

    // Test region N2
    assertTrue(Double.isNaN(process.processAB(0, 9)));
    assertEquals(-38.0, process.processAB(0, 11));
    assertEquals(-42.0, process.processAB(0, 13));
    assertEquals(-46.0, process.processAB(0, 15));
    assertEquals(-50.0, process.processAB(0, 17));
    assertEquals(-54.0, process.processAB(0, 19));
    assertEquals(-58.0, process.processAB(0, 21));
    assertEquals(-62.0, process.processAB(0, 23));

    // Test region P3
    assertTrue(Double.isNaN(process.processAB(-9, -0.1)));
    assertEquals(70.0, process.processAB(-11, -0.1));
    assertEquals(74.0, process.processAB(-13, -0.1));
    assertEquals(78.0, process.processAB(-15, -0.1));
    assertEquals(82.0, process.processAB(-17, -0.1));
    assertEquals(86.0, process.processAB(-19, -0.1));
    assertEquals(90.0, process.processAB(-21, -0.1));
    assertEquals(94.0, process.processAB(-23, -0.1));

    // Test region N3
    assertTrue(Double.isNaN(process.processAB(9, 0.1)));
    assertEquals(-70.0, process.processAB(11, 0.1));
    assertEquals(-74.0, process.processAB(13, 0.1));
    assertEquals(-78.0, process.processAB(15, 0.1));
    assertEquals(-82.0, process.processAB(17, 0.1));
    assertEquals(-86.0, process.processAB(19, 0.1));
    assertEquals(-90.0, process.processAB(21, 0.1));
    assertEquals(-94.0, process.processAB(23, 0.1));

    // Test region P4
    assertTrue(Double.isNaN(process.processAB(-9, 0.1)));
    assertEquals(102.0, process.processAB(-11, 0.1));
    assertEquals(106.0, process.processAB(-13, 0.1));
    assertEquals(110.0, process.processAB(-15, 0.1));
    assertEquals(114.0, process.processAB(-17, 0.1));
    assertEquals(118.0, process.processAB(-19, 0.1));
    assertEquals(122.0, process.processAB(-21, 0.1));
    assertEquals(126.0, process.processAB(-23, 0.1));

    // Test region N4
    assertTrue(Double.isNaN(process.processAB(9, -0.1)));
    assertEquals(-102.0, process.processAB(11, -0.1));
    assertEquals(-106.0, process.processAB(13, -0.1));
    assertEquals(-110.0, process.processAB(15, -0.1));
    assertEquals(-114.0, process.processAB(17, -0.1));
    assertEquals(-118.0, process.processAB(19, -0.1));
    assertEquals(-122.0, process.processAB(21, -0.1));
    assertEquals(-126.0, process.processAB(23, -0.1));

  }
}
