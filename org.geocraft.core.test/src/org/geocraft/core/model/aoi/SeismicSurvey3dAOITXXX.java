/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.aoi;


import junit.framework.TestCase;

import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.CoordinateSystem;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.seismic.SeismicSurvey3d;


public class SeismicSurvey3dAOITXXX extends TestCase {

  /**
   * Generates an AOI for the unit tests.
   * @param name the AOI name.
   * @param xyUnits the x,y units.
   */
  public SeismicSurvey3dAOI generateAOI(final String name, final Unit xyUnits, final float inlineDeltaAOI,
      final float xlineDeltaAOI) {
    SeismicSurvey3d geometry = generateSeismicSurvey(name, xyUnits);
    SeismicSurvey3dAOI aoi = null; //new SeismicSurvey3dAOI(name, geometry, 10, 20, inlineDeltaAOI, 30, 40, xlineDeltaAOI, new InMemoryMapper(), new InMemoryMapper());
    return aoi;
  }

  /**
   * Generates a seismic geometry for the unit tests.
   * @param name the seismic geometry name.
   * @param xyUnits the x,y units.
   * @return the generated seismic geometry.
   */
  private SeismicSurvey3d generateSeismicSurvey(final String name, final Unit xyUnits) {
    float inlineStart = 0;
    float inlineEnd = 100;
    float inlineDelta = 1;
    float xlineStart = 100;
    float xlineEnd = 0;
    float xlineDelta = -1;
    UnitPreferences.getInstance().setHorizontalDistanceUnit(xyUnits);
    CoordinateSystem cs = new CoordinateSystem("cs", Domain.TIME);
    Point3d[] points = new Point3d[4];
    points[0] = new Point3d(0, 0, 0);
    points[1] = new Point3d(100, 0, 0);
    points[2] = new Point3d(100, 100, 0);
    points[3] = new Point3d(0, 100, 0);
    CoordinateSeries cornerPoints = CoordinateSeries.createDirect(points, cs);
    SeismicSurvey3d geometry = null; //new SeismicSurvey3d(name, new InMemoryMapper(), inlineStart, inlineEnd, inlineDelta, xlineStart, xlineEnd, xlineDelta, cornerPoints);
    return geometry;
  }

  /**
   * Test creating an AOI name from the geometry and the inline and xline ranges.
   *
   */
  public void testCreateName() {
    SeismicSurvey3d geometry = generateSeismicSurvey("foo", Unit.FOOT);
    assertEquals("foo: IL(3.0/48.0/5.0),XL(97.0/87.0/-2.0)", SeismicSurvey3dAOI.generateName(geometry, 3, 48, 5, 97,
        87, -2));
  }

  /**
   * Test the AOI without decimation.
   */
  public void testSeismicSurvey3dAOI1() {
    Unit xyUnits = Unit.FOOT;
    SeismicSurvey3dAOI aoi = generateAOI("foo", xyUnits, 1, -1);
    assertFalse(aoi.isDecimated());
    assertEquals("foo", aoi.getDisplayName());
    assertTrue(aoi.contains(10, 60));
    assertTrue(aoi.contains(10, 70));
    assertTrue(aoi.contains(20, 60));
    assertTrue(aoi.contains(20, 70));
    assertFalse(aoi.contains(10, 0));
    assertFalse(aoi.contains(20, 0));
    assertFalse(aoi.contains(0, 60));
    assertFalse(aoi.contains(0, 70));
  }

  /**
   * Test the AOI with decimation.
   */
  public void testSeismicSurvey3dAOI2() {
    Unit xyUnits = Unit.FOOT;
    SeismicSurvey3dAOI aoi = generateAOI("foo", xyUnits, 2, -4);
    assertEquals("foo", aoi.getDisplayName());
    assertTrue(aoi.isDecimated());
    assertTrue(aoi.contains(10, 60));
    assertFalse(aoi.contains(10, 70));
    assertTrue(aoi.contains(20, 60));
    assertFalse(aoi.contains(20, 70));
    assertFalse(aoi.contains(10, 0));
    assertFalse(aoi.contains(20, 0));
    assertFalse(aoi.contains(0, 60));
    assertFalse(aoi.contains(0, 70));
  }

}
