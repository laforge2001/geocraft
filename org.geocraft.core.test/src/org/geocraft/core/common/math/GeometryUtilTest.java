/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.math;


import junit.framework.TestCase;


public class GeometryUtilTest extends TestCase {

  public void testNormal() {
    double[] normal = GeometryUtil.computeNormal(0, 0, 0, 1, 0, 0, 1, 1, 0);
    assertEquals(0.0, normal[0]);
    assertEquals(0.0, normal[1]);
    assertEquals(1.0, normal[2]);
  }

  public void testDistance() {
    double[] normal = GeometryUtil.computeNormal(0, 0, 0, 1, 0, 0, 1, 1, 0);
    double distance = GeometryUtil.distancePointToPlane(0, 0, 0, normal, 10, 10, 10);
    assertEquals(10.0, distance);
  }

  public void testDistance2() {
    double[] normal = GeometryUtil.computeNormal(0, 0, 0, 1, 1, 0, 1, 0, 0);
    double distance = GeometryUtil.distancePointToPlane(0, 0, 0, normal, 10, 10, 10);
    assertEquals(-10.0, distance);
  }

  public void testPointToLine1() {
    double distance = GeometryUtil.distancePointToLine(1, 1, 10, 1, 5, 5);
    assertEquals(4.0, distance);
  }

  public void testPointToLine2() {
    double distance = GeometryUtil.distancePointToLine(1, 1, 10, 1, 5, 1);
    assertEquals(0.0, distance);
  }

  public void testPointToLine3() {
    double distance = GeometryUtil.distancePointToLine(1, 1, 10, 1, 1, 1);
    assertEquals(0.0, distance);
  }

  public void testClipHorizontalLineToRectangle() {
    double x0 = -5;
    double y0 = 5;
    double x1 = 15;
    double y1 = 5;
    double minx = 0;
    double miny = 0;
    double maxx = 10;
    double maxy = 10;
    CohenSutherlandClip csc = new CohenSutherlandClip(minx, miny, maxx, maxy);
    double[] results = csc.clip(x0, y0, x1, y1);
    assertEquals(results[2], 0.);
    assertEquals(results[3], 5.);
    assertEquals(results[0], 10.);
    assertEquals(results[1], 5.);
  }

  public void testClipVerticalLineToRectangle() {
    double x0 = 5;
    double y0 = -5;
    double x1 = 5;
    double y1 = 15;
    double minx = 0;
    double miny = 0;
    double maxx = 10;
    double maxy = 10;
    CohenSutherlandClip csc = new CohenSutherlandClip(minx, miny, maxx, maxy);
    double[] results = csc.clip(x0, y0, x1, y1);
    assertEquals(5., results[2]);
    assertEquals(0., results[3]);
    assertEquals(5., results[0]);
    assertEquals(10., results[1]);
  }

  public void testClipDiagonalLineToRectangle() {
    double x0 = -5;
    double y0 = -4;
    double x1 = 15;
    double y1 = 16;
    double minx = 0;
    double miny = 0;
    double maxx = 10;
    double maxy = 10;
    CohenSutherlandClip csc = new CohenSutherlandClip(minx, miny, maxx, maxy);
    double[] results = csc.clip(x0, y0, x1, y1);
    assertEquals(0., results[2]);
    assertEquals(1., results[3]);
    assertEquals(9., results[0]);
    assertEquals(10., results[1]);
  }
}
