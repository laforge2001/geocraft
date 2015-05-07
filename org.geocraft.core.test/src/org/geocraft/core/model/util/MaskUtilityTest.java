/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.util;


import junit.framework.TestCase;

import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.CoordinateSystem;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.SpatialExtent;
import org.geocraft.core.model.geometry.GridGeometry2d;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.geometry.LineGeometry;
import org.geocraft.core.model.mapper.InMemoryMapper;
import org.geocraft.core.model.seismic.SeismicSurvey2d;


public class MaskUtilityTest extends TestCase {

  /**
   * Unit test for creating a 3D geometry mask.
   */
  public void testCreateMask3d() {
    // Setup.
    GridGeometry3d geometry = new GridGeometry3d("3D Geometry", 0, 0, 1, 1, 4, 3, 0);
    AreaOfInterest aoi = createAOI();

    // Execution.
    boolean[][] mask = MaskUtility.createMask(geometry, aoi);

    // Validation.
    assertFalse(mask[0][0]);
    assertTrue(mask[1][0]);
    assertFalse(mask[2][0]);
    assertFalse(mask[3][0]);
    assertFalse(mask[0][1]);
    assertTrue(mask[1][1]);
    assertTrue(mask[2][1]);
    assertFalse(mask[3][1]);
    assertFalse(mask[0][2]);
    assertFalse(mask[1][2]);
    assertTrue(mask[2][2]);
    assertFalse(mask[3][2]);
  }

  /**
   * Unit test for creating a 2D geometry mask.
   */
  public void testCreateMask2d() {
    // Setup.
    LineGeometry[] lines = new LineGeometry[2];
    CoordinateSystem coordSys = new CoordinateSystem("Projection", "Datum", Domain.TIME);
    Point3d[] xyPoints1 = new Point3d[3];
    xyPoints1[0] = new Point3d(0, 0, 0);
    xyPoints1[1] = new Point3d(1, 1.5, 0);
    xyPoints1[2] = new Point3d(2, 3, 0);
    CoordinateSeries xyCoordinates1 = CoordinateSeries.create(xyPoints1, coordSys);
    lines[0] = new LineGeometry("Line 1", 1, xyCoordinates1);
    Point3d[] xyPoints2 = new Point3d[4];
    xyPoints2[0] = new Point3d(1, 0, 0);
    xyPoints2[1] = new Point3d(1, 1, 0);
    xyPoints2[2] = new Point3d(1, 2, 0);
    xyPoints2[3] = new Point3d(1, 3, 0);
    CoordinateSeries xyCoordinates2 = CoordinateSeries.create(xyPoints2, coordSys);
    lines[1] = new LineGeometry("Line 2", 2, xyCoordinates2);
    GridGeometry2d geometry = new GridGeometry2d("2D Geometry", lines);
    AreaOfInterest aoi = createAOI();

    // Execution.
    boolean mask[][] = MaskUtility.createMask(geometry, aoi);

    // Validation.
    assertFalse(mask[0][0]);
    assertTrue(mask[0][1]);
    assertFalse(mask[0][2]);
    assertFalse(mask[1][0]);
    assertTrue(mask[1][1]);
    assertTrue(mask[1][2]);
    assertFalse(mask[1][3]);
  }

  /**
   * Creates a test AOI.
   * 
   * @return the test AOI.
   */
  private AreaOfInterest createAOI() {
    AreaOfInterest aoi = new AreaOfInterest("FOO", new InMemoryMapper(AreaOfInterest.class)) {

      @Override
      public SpatialExtent getExtent() {
        return null;
      }

      @Override
      public boolean contains(final double x, final double y) {
        if (x >= 0.5 && x <= 1.5 && y >= 0.5 && y <= 2.5) {
          return true;
        } else if (x >= -0.5 && x <= 0.5 && y >= 0.5 && y <= 1.5) {
          return true;
        } else if (x >= 1.5 && x <= 2.5 && y >= 1.5 && y <= 2.5) {
          return true;
        }
        return false;
      }

      @Override
      public boolean contains(final double x, final double y, final SeismicSurvey2d survey) {
        // TODO Auto-generated method stub
        return false;
      }
    };
    return aoi;
  }

}
