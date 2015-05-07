package org.geocraft.core.model.aoi;


import junit.framework.TestCase;

/**
 * Test case for the <code>MapPolygonAOI</code> class.
 */
public class MapPolygonAOITest extends TestCase {

  /**
   * Unit test for AOI with only inclusive polygons.
   */
  public void testInside() {

    // Create an empty AOI.
    MapPolygonAOI aoi = new MapPolygonAOI("test", new AOITestMapper());

    // Add an inclusive polygonal area.
    double[] x = new double[] { 0, 10, 10, 0, 0 };
    double[] y = new double[] { 0, 0, 10, 10, 0 };
    aoi.addInclusionPolygon(x, y);

    // Validate certain x,y coordinates are inside or outside the AOI.
    assertTrue(aoi.contains(5, 5));
    assertFalse(aoi.contains(50, 50));
    assertTrue(aoi.contains(0, 0));
    assertTrue(aoi.contains(0, 5));
    assertTrue(aoi.contains(5, 0));
    assertFalse(aoi.contains(10, 5));
    assertFalse(aoi.contains(5, 10));
  }

  /**
   * Unit test for AOI with only exclusive polygons.
   */
  public void testOutside() {

    // Create an empty AOI.
    MapPolygonAOI aoi = new MapPolygonAOI("test", new AOITestMapper());

    // Add an exclusive polygonal area.
    double[] x = new double[] { 0, 0, 10, 10, 0 };
    double[] y = new double[] { 0, 10, 10, 0, 0 };
    aoi.addExclusionPolygon(x, y);

    // Validate certain x,y coordinates are inside or outside the AOI.
    assertFalse(aoi.contains(5, 5));
    assertFalse(aoi.contains(50, 50));
    assertFalse(aoi.contains(-500, 50));
  }

  /**
   * Unit test for AOI with both inclusive and exclusive polygons.
   */
  public void testBoth() {

    MapPolygonAOI aoi = new MapPolygonAOI("test", new AOITestMapper());

    double[] x = new double[] { 0, 0, 10, 10, 0 };
    double[] y = new double[] { 0, 10, 10, 0, 0 };
    aoi.addInclusionPolygon(x, y);

    x = new double[] { 20, 20, 30, 30, 20 };
    y = new double[] { 20, 30, 30, 20, 20 };
    aoi.addInclusionPolygon(x, y);

    x = new double[] { 40, 40, 50, 50, 40 };
    y = new double[] { 40, 50, 50, 40, 40 };
    aoi.addExclusionPolygon(x, y);

    assertTrue(aoi.contains(5, 5));
    assertTrue(aoi.contains(25, 25));
    assertFalse(aoi.contains(45, 45));
  }

  /**
   * Unit test for AOI with complex overlapping polygons.
   */
  public void testComplex() {

    MapPolygonAOI aoi = new MapPolygonAOI("test", new AOITestMapper());

    double[] x = new double[] { 0, 0, 10, 10, 0 };
    double[] y = new double[] { 0, 10, 10, 0, 0 };
    aoi.addExclusionPolygon(x, y);

    x = new double[] { 0, 0, 30, 30, 0 };
    y = new double[] { 0, 30, 30, 0, 0 };
    aoi.addInclusionPolygon(x, y);

    assertFalse(aoi.contains(5, 5));
    assertTrue(aoi.contains(25, 25));
    assertFalse(aoi.contains(45, 45));
  }

}
