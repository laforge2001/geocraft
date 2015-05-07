/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.geomath.algorithm.horizon.extrapolate;


/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.geocraft.core.model.datatypes.DataType;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.geomath.algorithm.util.TestHelperUtil;


public class ExtrapolateTest extends TestCase {

  static final float[][] DATA_A = new float[][] {
      { -999.25f, -999.25f, -999.25f, -999.25f, -999.25f, -999.25f, -999.25f, -999.25f },
      { -999.25f, 53, 59, 21, 40, 39, 56, -999.25f }, { -999.25f, 46, 32, 67, 51, 35, 34, -999.25f },
      { -999.25f, 44, 38, 87, 57, 37, 45, -999.25f }, { -999.25f, 48, 34, 89, 52, 31, 47, -999.25f },
      { -999.25f, 32, 68, 69, 34, 41, 43, -999.25f }, { -999.25f, 57, 43, 44, 42, 47, 30, -999.25f },
      { -999.25f, -999.25f, -999.25f, -999.25f, -999.25f, -999.25f, -999.25f, -999.25f } };

  public void testBogus() {
    assertTrue(true);
  }

  public void testExtrapolate() {
    float zNull = (float) -999.25;
    Grid3d gridAProperty = TestHelperUtil.createGrid("A", 0, 0, 1, 1, 8, 8, 0.0, zNull, DATA_A, Domain.TIME,
        Unit.MILLISECONDS, DataType.FLOAT);
    GridGeometry3d geometryA = gridAProperty.getGeometry();

    Grid3d property = TestHelperUtil.createGrid("BProperty", 0, 0, 1, 1, 8, 8, 0.0, zNull, DATA_A, Domain.TIME,
        Unit.MILLISECONDS, DataType.FLOAT);

    // add data to the property
    property.setValues(DATA_A, zNull, Unit.FOOT);

    // gridA run the algorithm
    float[][] result = Extrapolate.extrapolateHor(geometryA, null, property, 2, 2, 2, 2, new NullProgressMonitor());

    float[][] expected1 = new float[][] { { 32, 46, 32, 67, 51, 35, 34, 35 }, { 59, 53, 59, 21, 40, 39, 56, 39 },
        { 32, 46, 32, 67, 51, 35, 34, 35 }, { 38, 44, 38, 87, 57, 37, 45, 37 }, { 34, 48, 34, 89, 52, 31, 47, 31 },
        { 68, 32, 68, 69, 34, 41, 43, 41 }, { 43, 57, 43, 44, 42, 47, 30, 47 }, { 68, 32, 68, 69, 34, 41, 43, 41 } };

    // check the results
    for (int i = 0; i < result.length; i++) {
      for (int j = 0; j < result.length; j++) {
        assertEquals(expected1[i][j], result[i][j]);
      }
    }
  }
}
