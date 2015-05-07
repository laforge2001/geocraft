package org.geocraft.geomath.algorithm.horizon.decimate;


import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.geocraft.core.model.datatypes.DataType;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.geomath.algorithm.util.TestHelperUtil;


public class DecimateTest extends TestCase {

  static final float NULL_VALUE = -999.25f;

  static final float[][] INPUT_DATA = new float[][] { { 0, 2, 4, 6, 8 }, { 10, 12, NULL_VALUE, 16, 18 },
      { 20, 22, 24, 26, 28 }, { 30, 32, 34, 36, 38 }, { 40, 42, 44, 46, 48 }, { 50, 52, 54, 56, 58 }, };

  private Grid3d _inputGrid;

  @Override
  public void setUp() {
    _inputGrid = TestHelperUtil.createGrid("A", 0, 0, 1, 1, 6, 5, 0.0, NULL_VALUE, INPUT_DATA, Domain.TIME,
        Unit.MILLISECONDS, DataType.FLOAT);
  }

  /**
   * Test no decimation.
   */
  public void testNoDecimation() {

    // Run the decimation.
    Decimate decimate = new Decimate();
    float[][] results = decimate.decimateGridData(_inputGrid, 1, 1, new NullProgressMonitor());

    // Check the results.
    assertEquals(true, TestHelperUtil.arrayIsEqual(results, INPUT_DATA));
  }

  /**
   * Tests an actual (2x3) decimation.
   */
  public void testDecimate() {

    // Run the decimation.
    Decimate decimate = new Decimate();
    float[][] results = decimate.decimateGridData(_inputGrid, 2, 3, new NullProgressMonitor());

    // Check the results.
    float[][] expected = new float[][] { { 0, NULL_VALUE, NULL_VALUE, 6, NULL_VALUE },
        { NULL_VALUE, NULL_VALUE, NULL_VALUE, NULL_VALUE, NULL_VALUE }, { 20, NULL_VALUE, NULL_VALUE, 26, NULL_VALUE },
        { NULL_VALUE, NULL_VALUE, NULL_VALUE, NULL_VALUE, NULL_VALUE }, { 40, NULL_VALUE, NULL_VALUE, 46, NULL_VALUE },
        { NULL_VALUE, NULL_VALUE, NULL_VALUE, NULL_VALUE, NULL_VALUE }, };
    assertEquals(true, TestHelperUtil.arrayIsEqual(results, expected));
  }

}
