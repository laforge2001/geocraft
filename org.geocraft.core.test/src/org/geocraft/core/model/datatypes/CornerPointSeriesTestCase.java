/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.datatypes;


import junit.framework.TestCase;


public class CornerPointSeriesTestCase extends TestCase {

  static Point3d[] rectangle = { new Point3d(0, 0, 0), new Point3d(100, 0, 0), new Point3d(100, 100, 0),
      new Point3d(0, 100, 0) };

  static Point3d[] parallelogram = { new Point3d(0, 0, 0), new Point3d(100, 0, 0), new Point3d(200, 100, 0),
      new Point3d(100, 100, 0) };

  static Point3d[] blob = { new Point3d(0, 0, 0), new Point3d(100, 50, 0), new Point3d(2000, 100, 0),
      new Point3d(100, 100, 0) };

  static Point3d[] pentagon = { new Point3d(0, 0, 0), new Point3d(100, 50, 0), new Point3d(2000, 100, 0),
      new Point3d(100, 100, 0), new Point3d(0, 0, 0) };

  public void testRectangle() {
    CornerPointsSeries cornerPoints = CornerPointsSeries.create(rectangle, null);
    assertTrue(cornerPoints.isRectangular());
  }

  public void testParallelogram() {
    CornerPointsSeries cornerPoints = CornerPointsSeries.create(parallelogram, null);
    assertTrue(cornerPoints.isRectilinear());
    assertFalse(cornerPoints.isRectangular());
  }

  public void testBlob() {
    CornerPointsSeries cornerPoints = CornerPointsSeries.create(blob, null);
    assertFalse(cornerPoints.isRectilinear());
    assertFalse(cornerPoints.isRectangular());
  }

  public void testPentagon() {
    try {
      CornerPointsSeries.create(pentagon, null);
      fail("Should have thrown an exception when creating corner-points series with 5 points.");
    } catch (IllegalArgumentException iex) {
      // do nothing here. 
    }
  }
}
