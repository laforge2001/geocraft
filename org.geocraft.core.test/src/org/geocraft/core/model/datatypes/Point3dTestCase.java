/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.datatypes;


import junit.framework.TestCase;


/**
 * Unit tests for the <code>Point3d</code> class.
 */
public class Point3dTestCase extends TestCase {

  /**
   * Test the basic and copy constructors.
   */
  public void testConstructors() {
    double x = 4.579E9;
    double y = -2.792E4;
    double z = -2002.3743;

    // Test the basic constructor.
    Point3d point1 = new Point3d(x, y, z);
    assertEquals(x, point1.getX());
    assertEquals(y, point1.getY());
    assertEquals(z, point1.getZ());

    // Test the copy constructor.
    Point3d point2 = new Point3d(point1);
    assertEquals(x, point2.getX());
    assertEquals(y, point2.getY());
    assertEquals(z, point2.getZ());

    // Test the 2 points are considered equals.
    assertTrue(point1.equals(point2));

    // Test the copy constructor.
    Point3d point3 = new Point3d(x + 0.01, y, z);
    assertFalse(point1.equals(point3));
  }

  /**
   * Test the calculation of distance between 2 points.
   */
  public void testDistance() {
    double x = 100;
    double y = -200;
    double z = 5000;
    double xdelta = 10;
    double ydelta = 20;
    double zdelta = 40;

    // Construct 2 points.
    Point3d point1 = new Point3d(x, y, z);
    Point3d point2 = new Point3d(x + xdelta, y + ydelta, z + zdelta);

    // Test the distance calculation.
    double expectedValue = Math.sqrt(xdelta * xdelta + ydelta * ydelta + zdelta * zdelta);
    assertEquals(expectedValue, point1.distanceTo(point2));
  }

  /**
   * Test the interpolation between 2 points.
   */
  public void testInterpolation() {
    double x = 100;
    double y = -200;
    double z = 5000;
    double xdelta = 10;
    double ydelta = 20;
    double zdelta = 40;
    double alpha = 0.75;

    // Construct 2 points.
    Point3d point1 = new Point3d(x, y, z);
    Point3d point2 = new Point3d(x + xdelta, y + ydelta, z + zdelta);

    // Test the interpolation calculation.
    Point3d point3 = Point3d.interpolate(point1, point2, alpha);
    assertEquals(x + xdelta * alpha, point3.getX());
    assertEquals(y + ydelta * alpha, point3.getY());
    assertEquals(z + zdelta * alpha, point3.getZ());
  }
}
