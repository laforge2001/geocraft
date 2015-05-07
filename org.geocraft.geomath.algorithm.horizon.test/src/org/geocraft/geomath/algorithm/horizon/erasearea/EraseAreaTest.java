/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.horizon.erasearea;


import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.geocraft.core.model.aoi.MapPolygonAOI;
import org.geocraft.core.model.datatypes.DataType;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.geomath.algorithm.util.TestHelperUtil;


public class EraseAreaTest extends TestCase {

  static final float NULL_VALUE = -999.25f;

  static final float[][] INPUT_DATA = new float[][] { { 0, 2, 4, 6, 8 }, { 10, 12, 14, 16, 18 },
      { 20, 22, 24, 26, 28 }, { 30, 32, 34, 36, 38 } };

  static final double[] AOI_XS = new double[] { 0.009, 2.001, 2.001, 0.009, 0.009 };

  static final double[] AOI_YS = new double[] { 0.009, 0.009, 2.001, 2.001, 0.009 };

  private Grid3d _grid;

  private MapPolygonAOI _areaOfInterest;

  @Override
  public void setUp() {
    // Initialize the application units
    UnitPreferences.getInstance().setHorizontalDistanceUnit(Unit.FOOT);

    _grid = TestHelperUtil.createGrid("A", 0, 0, 1, 1, 4, 5, 0.0, NULL_VALUE, INPUT_DATA, Domain.DISTANCE, Unit.FOOT,
        DataType.FLOAT);
    _areaOfInterest = new MapPolygonAOI("aoi1");
    _areaOfInterest.addInclusionPolygon(AOI_XS, AOI_YS);
  }

  /**
   * Tests erasing an area.
   */
  public void testEraseArea() {

    // Run the erase area.
    EraseArea eraseArea = new EraseArea();
    float[][] results = eraseArea.eraseGridData(_grid, _areaOfInterest, new NullProgressMonitor());

    // Check the results.
    float[][] expected = new float[][] { { 0, 2, 4, 6, 8 }, { 10, NULL_VALUE, NULL_VALUE, 16, 18 },
        { 20, NULL_VALUE, NULL_VALUE, 26, 28 }, { 30, 32, 34, 36, 38 } };
    assertEquals(true, TestHelperUtil.arrayIsEqual(results, expected));
  }
}
