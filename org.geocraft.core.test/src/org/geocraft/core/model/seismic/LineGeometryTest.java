/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.seismic;


import junit.framework.TestCase;

import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.geometry.LineGeometry;


/**
 * Unit tests for the LineGeometry class.
 */
public class LineGeometryTest extends TestCase {

  /**
   * Test the line geometry.
   */
  public void testLineGeometry() {
    // Create a line geometry for testing.
    String lineName = "foo";
    int lineNumber = 2;
    Point3d[] points = new Point3d[3];
    points[0] = new Point3d(1, 2, 0);
    points[1] = new Point3d(7.5, 4.5, 0);
    points[2] = new Point3d(8, 9, 0);
    CoordinateSeries xyCoords = CoordinateSeries.create(points, null);
    LineGeometry geometry = new LineGeometry(lineName, lineNumber, xyCoords);

    // Test the name.
    assertEquals(lineName, geometry.getDisplayName());

    // Test the number of points.
    assertEquals(3, geometry.getNumBins());

    // Test the x,y values of each point.
    assertEquals(1.0, geometry.getPoints().getX(0));
    assertEquals(2.0, geometry.getPoints().getY(0));
    assertEquals(7.5, geometry.getPoints().getX(1));
    assertEquals(4.5, geometry.getPoints().getY(1));
    assertEquals(8.0, geometry.getPoints().getX(2));
    assertEquals(9.0, geometry.getPoints().getY(2));

    // Test the transform of bin to x,y.
    assertEquals(1.0, geometry.transformBinToXY(0)[0]);
    assertEquals(2.0, geometry.transformBinToXY(0)[1]);
    assertEquals(7.75, geometry.transformBinToXY(1.5)[0]);
    assertEquals(6.75, geometry.transformBinToXY(1.5)[1]);

    // Test that asking for a bin beyond the start throws exception.
    try {
      geometry.transformBinToXY(-1);
      // The transform above should have thrown exceptions.
      assertFalse("Exception should have been thrown.", true);
    } catch (IndexOutOfBoundsException e) {
      // Exceptions was successfully thrown.
    }

    // Test that asking for a bin beyond the end throws exception.
    try {
      geometry.transformBinToXY(4);
      // The transform above should have thrown exceptions.
      assertFalse("Exception should have been thrown.", true);
    } catch (IndexOutOfBoundsException e) {
      // Exceptions was successfully thrown.
    }

    // Create another line geometry.
    LineGeometry geometry2 = new LineGeometry("bar", lineNumber, xyCoords);

    // Test the geometry equality.
    assertFalse(geometry.equals(geometry2));
    assertTrue(geometry.matchesGeometry(geometry2));

    // Create another line geometry with a different point.
    points[2] = new Point3d(8, 9.1, 0);
    xyCoords = CoordinateSeries.create(points, null);
    LineGeometry geometry3 = new LineGeometry("bar", lineNumber, xyCoords);

    // Test the geometry equality.
    assertFalse(geometry.equals(geometry3));
  }
}
