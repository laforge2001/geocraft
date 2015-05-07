/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model;


import junit.framework.TestCase;

import org.geocraft.core.model.datatypes.CornerPointsSeries;
import org.geocraft.core.model.datatypes.Orientation;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.geometry.GridGeometry3d;


public class GridTest extends TestCase {

  public void testOrientationRow() {
    int numRows = 10;
    int numCols = 10;
    Point3d[] pts = new Point3d[4];
    pts[0] = new Point3d(1948043, 10217509, 0);
    pts[1] = new Point3d(1928818, 10249840, 0);
    pts[2] = new Point3d(1966838, 10272075, 0);
    pts[3] = new Point3d(1986385, 10239636, 0);
    CornerPointsSeries cornerPoints = CornerPointsSeries.createDirect(pts, null);
    GridGeometry3d gridGeometry = new GridGeometry3d("", numRows, numCols, cornerPoints);
    assertEquals(Orientation.ROW, gridGeometry.getClockwise());
  }

  public void testOrientationCol() {
    int numRows = 10;
    int numCols = 10;
    Point3d[] pts = new Point3d[4];
    pts[0] = new Point3d(1948043, 10217509, 0);
    pts[3] = new Point3d(1928818, 10249840, 0);
    pts[2] = new Point3d(1966838, 10272075, 0);
    pts[1] = new Point3d(1986385, 10239636, 0);
    CornerPointsSeries cornerPoints = CornerPointsSeries.createDirect(pts, null);
    GridGeometry3d gridGeometry = new GridGeometry3d("", numRows, numCols, cornerPoints);
    assertEquals(Orientation.COLUMN, gridGeometry.getClockwise());
  }

}
