/*
 * Copyright (C) ConocoPhillips 20089 All Rights Reserved. 
 */
package org.geocraft.math.geometry;


import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.math.geometry.MarchingCubes.GridCell;
import org.geocraft.math.geometry.MarchingCubes.Triangle;


public class MarchingCubesTest extends TestCase {

  // define the cube 
  Point3d p0 = new Point3d(0, 0, 0);

  Point3d p1 = new Point3d(1, 0, 0);

  Point3d p2 = new Point3d(1, 1, 0);

  Point3d p3 = new Point3d(0, 1, 0);

  Point3d p4 = new Point3d(0, 0, 1);

  Point3d p5 = new Point3d(1, 0, 1);

  Point3d p6 = new Point3d(1, 1, 1);

  Point3d p7 = new Point3d(0, 1, 1);

  Point3d[] points = new Point3d[] { p0, p1, p2, p3, p4, p5, p6, p7 };

  public void testFlat() {

    double[] vals = new double[] { 1, 1, 1, 1, 2, 2, 2, 2 };

    GridCell cell = new MarchingCubes.GridCell(points, vals);

    List<Triangle> result = new MarchingCubes().polygonise(cell, 1.5);

    assertEquals(2, result.size());
  }

  public void testAllUnder() {
    double[] vals = new double[] { 1, 1, 1, 1, 2, 2, 2, 2 };

    GridCell cell = new MarchingCubes.GridCell(points, vals);

    List<Triangle> result = new MarchingCubes().polygonise(cell, 150000);

    assertEquals(0, result.size());
  }

  public void testAllOver() {
    double[] vals = new double[] { 1, 1, 1, 1, 2, 2, 2, 2 };

    GridCell cell = new MarchingCubes.GridCell(points, vals);

    List<Triangle> result = new MarchingCubes().polygonise(cell, -150000);

    assertEquals(0, result.size());
  }

  public void testVerticalX() {
    double[] vals = new double[] { 1, 1, 2, 2, 1, 1, 2, 2 };

    GridCell cell = new MarchingCubes.GridCell(points, vals);

    List<Triangle> result = new MarchingCubes().polygonise(cell, 1.5);

    assertEquals(2, result.size());

    System.out.println(Arrays.toString(result.toArray(new Triangle[0])));
  }

  public void testVerticalY() {
    double[] vals = new double[] { 1, 2, 2, 1, 1, 2, 2, 1 };

    GridCell cell = new MarchingCubes.GridCell(points, vals);

    List<Triangle> result = new MarchingCubes().polygonise(cell, 1.5);

    assertEquals(2, result.size());

    System.out.println(Arrays.toString(result.toArray(new Triangle[0])));
  }

  public void testExample() {
    double[] vals = new double[] { 2, 2, 2, -2, 2, 2, 2, 2 };

    GridCell cell = new MarchingCubes.GridCell(points, vals);

    List<Triangle> result = new MarchingCubes().polygonise(cell, 0);

    assertEquals(1, result.size());

    System.out.println(Arrays.toString(result.toArray(new Triangle[0])));
  }
}
