/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.seismic;


import junit.framework.TestCase;

import org.geocraft.core.model.datatypes.CornerPointsSeries;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.geometry.GridGeometry3d;


/**
 * Unit tests for the GridGeometry class.
 * @author Bill Lucas
 *
 */
public class GridGeometryTest extends TestCase {

  /**
   * Test the grid geometry.
   */
  public void testGridGeometry() {
    // Create a grid geometry for testing.
    String gridName = "foo";
    int numRows = 7;
    int numCols = 7;
    Point3d[] points = new Point3d[4];
    points[0] = new Point3d(0, 0, 0);
    points[1] = new Point3d(6, 3, 0);
    points[2] = new Point3d(9, 9, 0);
    points[3] = new Point3d(3, 6, 0);
    CornerPointsSeries xyCoords = CornerPointsSeries.create(points, null);
    GridGeometry3d geometry = new GridGeometry3d(gridName, numRows, numCols, xyCoords);

    // Test the name.
    assertEquals(gridName, geometry.getDisplayName());

    // Test the number of rows.
    assertEquals(numRows, geometry.getNumRows());

    // Test the number of columns.
    assertEquals(numCols, geometry.getNumColumns());

    // Test the x,y values of each point.
    assertEquals(0.0, geometry.getCornerPoints().getX(0));
    assertEquals(0.0, geometry.getCornerPoints().getY(0));
    assertEquals(6.0, geometry.getCornerPoints().getX(1));
    assertEquals(3.0, geometry.getCornerPoints().getY(1));
    assertEquals(9.0, geometry.getCornerPoints().getX(2));
    assertEquals(9.0, geometry.getCornerPoints().getY(2));
    assertEquals(3.0, geometry.getCornerPoints().getX(3));
    assertEquals(6.0, geometry.getCornerPoints().getY(3));

    // Test the transform of row,column to x,y.
    assertEquals(0.0, geometry.transformRowColToXY(0, 0)[0]);
    assertEquals(0.0, geometry.transformRowColToXY(0, 0)[1]);
    assertEquals(2.5, geometry.transformRowColToXY(1, 2)[0]);
    assertEquals(2.0, geometry.transformRowColToXY(1, 2)[1]);
    assertEquals(6.5, geometry.transformRowColToXY(5, 4)[0]);
    assertEquals(7.0, geometry.transformRowColToXY(5, 4)[1]);

    // Test that asking for a row outside the bounds throws exception.
    try {
      geometry.transformRowColToXY(-1, 5);
      // The transform above should have thrown exceptions.
      assertFalse("Exception should have been thrown.", true);
    } catch (IndexOutOfBoundsException e) {
      // Exceptions was successfully thrown.
    }

    // Test that asking for a column outside the bounds throws exception.
    try {
      geometry.transformRowColToXY(5, -1);
      // The transform above should have thrown exceptions.
      assertFalse("Exception should have been thrown.", true);
    } catch (IndexOutOfBoundsException e) {
      // Exceptions was successfully thrown.
    }

    // Create another grid geometry (with the same name).
    GridGeometry3d geometry2a = new GridGeometry3d("foo", numRows, numCols, xyCoords);

    // Test the geometry equality.
    assertTrue(geometry.equals(geometry2a));

    // Create another grid geometry (with a different name).
    GridGeometry3d geometry2b = new GridGeometry3d("bar", numRows, numCols, xyCoords);

    // Test the geometry equality.
    assertFalse(geometry.equals(geometry2b));

    // Create another grid geometry with a number of rows.
    GridGeometry3d geometry3 = new GridGeometry3d("bar", numRows + 1, numCols, xyCoords);

    // Test the geometry equality.
    assertFalse(geometry.equals(geometry3));

    // Create another grid geometry with a number of columns.
    GridGeometry3d geometry4 = new GridGeometry3d("bar", numRows, numCols + 1, xyCoords);

    // Test the geometry equality.
    assertFalse(geometry.equals(geometry4));

    // Create another grid geometry with a different corner point.
    points[2] = new Point3d(10, 10.1, 0);
    xyCoords = CornerPointsSeries.create(points, null);
    GridGeometry3d geometry5 = new GridGeometry3d("bar", numRows, numCols, xyCoords);

    // Test the geometry equality.
    assertFalse(geometry.equals(geometry5));

  }
}
