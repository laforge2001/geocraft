/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.polygon;


import junit.framework.TestCase;

import org.geocraft.ui.plot.object.IPlotPolygon;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.object.PlotPolygon;
import org.geocraft.ui.plot.util.PolygonRegionsUtil;


/**
 * The unit tests for the PolygonRegionsUtil class.
 */
public class PolygonRegionsUtilTest extends TestCase {

  public void testNoOp() {
    assertTrue(true);
  }

  /**
   * Test if a point is inside a polygon.
   */
  public void xxxtestIsPointInside1() {
    IPlotPolygon polygon = new PlotPolygon();
    polygon.addPoint(new PlotPoint(0, 0, 0));
    polygon.addPoint(new PlotPoint(10, 0, 0));
    polygon.addPoint(new PlotPoint(5, 10, 0));

    // Test a point outside.
    assertFalse(PolygonRegionsUtil.isPointInside(polygon, -5, 5));

    // Test a point inside.
    assertTrue(PolygonRegionsUtil.isPointInside(polygon, 3, 1));

    // Test a point on the edge.
    assertFalse(PolygonRegionsUtil.isPointInside(polygon, 5, 0));
  }

  /**
   * Test checking a point inside an array of x,y coordinates pairs.
   */
  public void xxxtestIsPointInside2() {
    double[] xs = { 0, 10, 5 };
    double[] ys = { 0, 0, 10 };

    // Test a point outside.
    assertFalse(PolygonRegionsUtil.isPointInside(3, xs, ys, -5, 5));

    // Test a point inside.
    assertTrue(PolygonRegionsUtil.isPointInside(3, xs, ys, 3, 1));

    // Test a point on the edge.
    assertFalse(PolygonRegionsUtil.isPointInside(3, xs, ys, 5, 0));
  }

  /**
   * Test computing angle between to vectors.
   */
  public void xxxtestComputePointAngle() {
    double x0 = 0;
    double y0 = 0;
    double x1 = 10;
    double y1 = 5.7735027;
    double x2 = 5.7735027;
    double y2 = 10;
    double angleRads = PolygonRegionsUtil.computePointAngle(x1, y1, x2, y2, x0, y0);
    double angleDegs = Math.toDegrees(angleRads);
    assertTrue(Math.abs(30 - angleDegs) < 0.0001);
  }

}
