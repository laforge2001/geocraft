/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.horizon.mediansmooth;


import junit.framework.TestCase;

import org.geocraft.core.model.datatypes.DataType;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.geomath.algorithm.util.TestHelperUtil;


public class MedianSmoothTaskTXXX extends TestCase {

  static final float[][] DATA_A = new float[][] { { 75, 90, 5, 23, 34, 34, 30, 29 }, { 28, 15, 98, 85, 76, 63, 81, 2 },
      { 81, 2, 23, 41, 53, -999.25f, 49, 45 }, { 39, 24, 5, 88, 72, 59, 6, 30 } };

  static final float[][] DATA_B = new float[][] { { -999.25f, 1, 1, 1, 1, 1, -999.25f, -999.25f },
      { -999.25f, 1, 1, 1, 1, 1, -999.25f, -999.25f }, { -999.25f, 1, 1, 1, 1, 1, -999.25f, -999.25f },
      { -999.25f, 1, 1, 1, 1, 1, -999.25f, -999.25f } };

  static final float[][] DATA_C = new float[][] { { 75, 90, 5, 23, 34, 34, 30, 29 },
      { 28, 15, 98, -999.25f, -999.25f, -999.25f, 81, 2 }, { 81, 2, 23, -999.25f, -999.25f, -999.25f, 49, 45 },
      { 39, 24, 5, 88, 72, 59, 6, 30 } };

  static final float[][] DATA_D = new float[][] { { 75, 90, 5, 23, 34, 34, 30, 29 },
      { 28, -999.25f, -999.25f, 85, 76, 63, 81, 2 }, { 81, -999.25f, -999.25f, 41, 53, -999.25f, 49, 45 },
      { 39, 24, 5, 88, 72, 59, 6, 30 } };

  private Grid3d _propertyA;

  private Grid3d _propertyB;

  private Grid3d _propertyC;

  private Grid3d _propertyD;

  private final float _zNull = -999.25f;

  @Override
  public void setUp() {

    _propertyA = TestHelperUtil.createGrid("A", 0, 0, 1, 1, 4, 8, 0.0, _zNull, DATA_A, Domain.TIME, Unit.MILLISECONDS,
        DataType.FLOAT);
    _propertyB = TestHelperUtil.createGrid("B", 0, 0, 1, 1, 4, 8, 0.0, _zNull, DATA_B, Domain.TIME, Unit.MILLISECONDS,
        DataType.FLOAT);
    _propertyC = TestHelperUtil.createGrid("C", 0, 0, 1, 1, 4, 8, 0.0, _zNull, DATA_C, Domain.TIME, Unit.MILLISECONDS,
        DataType.FLOAT);
    _propertyD = TestHelperUtil.createGrid("D", 0, 0, 1, 1, 4, 8, 0.0, _zNull, DATA_D, Domain.TIME, Unit.MILLISECONDS,
        DataType.FLOAT);

    // add data to the properties
    _propertyA.setValues(DATA_A, _zNull, Unit.FOOT);
    _propertyB.setValues(DATA_B, _zNull, Unit.FOOT);
    _propertyC.setValues(DATA_C, _zNull, Unit.FOOT);
    _propertyD.setValues(DATA_D, _zNull, Unit.FOOT);
  }

  public void testMedSmooth() {
    //    // run the algorithm
    //    Grid3d result1 = new MedianSmoothTask().medianSmooth(_propertyA, null, 0, 5, 5, InterpolateOption.NOT_NULL, false,
    //        false, "smooth grid", logger);
    //    Grid3d result2 = new MedianSmoothTask().medianSmooth(_propertyA, _propertyB, 0, 5, 5, InterpolateOption.NOT_NULL,
    //        true, false, "smooth grid", logger);
    //    Grid3d result3 = new MedianSmoothTask().medianSmooth(_propertyC, _propertyB, 4, 0, 0, InterpolateOption.NULL_ONLY,
    //        true, true, "smooth grid", logger);
    //
    //    float[][] expected1 = new float[][] { { 75, 41, 53, 41, 49, 45, 49, 45 }, { 28, 39, 41, 53, 53, 49, 49, 45 },
    //        { 28, 39, 41, 53, 53, -999.25f, 49, 45 }, { 28, 39, 53, 59, 63, 59, 59, 49 } };
    //    float[][] expected2 = new float[][] { { 75, 41, 41, 41, 53, 53, 30, 29 }, { 28, 24, 41, 53, 59, 63, 81, 2 },
    //        { 81, 24, 41, 53, 59, -999.25f, 49, 45 }, { 39, 41, 53, 59, 72, 72, 6, 30 } };
    //    float[][] expected3 = new float[][] { { 75, 90, 5, 23, 34, 34, 30, 29 }, { 28, 15, 98, 23, 34, 34, 81, 2 },
    //        { 81, 2, 23, 88, 72, 72, 49, 45 }, { 39, 24, 5, 88, 72, 59, 6, 30 } };
    //
    //    // Check the results
    //    assertEquals(true, TestHelperUtil.testResult(result1, expected1, 4, 8, 0.001));
    //    assertEquals(true, TestHelperUtil.testResult(result2, expected2, 4, 8, 0.001));
    //    assertEquals(true, TestHelperUtil.testResult(result3, expected3, 4, 8, 0.001));
  }

  //  public void testWavgSmooth() {
  //    //    run the algorithm
  //    Grid result1 = WeightedSmoothImpl.weightedSmooth(_propertyA, null, 1.0, 2, 2, false, false, false, "smooth grid");
  //    Grid result2 = WeightedSmoothImpl.weightedSmooth(_propertyA, _propertyB, 1.0, 2, 2, true, false, false, "smooth grid");
  //    Grid result3 = WeightedSmoothImpl.weightedSmooth(_propertyC, _propertyB, 1.0, 2, 2, true, true, false, "smooth grid");
  //
  //    float[][] expected1 = new float[][] { { 52.0f, 51.833f, 52.667f, 53.5f, 52.5f, 53.0f, 39.833f, 35.5f }, { 48.5f, 46.333f, 42.444f, 48.667f, 51.125f, 52.5f, 41.625f, 39.333f },
  //        { 31.5f, 35.0f, 42.333f, 60.111f, 67.125f, -999.25f, 41.875f, 35.5f }, { 36.5f, 29.0f, 30.5f, 47.0f, 62.6f, 47.8f, 37.8f, 32.5f } };
  //    float[][] expected2 = new float[][] { { 75.0f, 52.0f, 52.667f, 53.5f, 52.5f, 51.75f, 30.0f, 29.0f }, { 28.0f, 38.833f, 42.444f, 48.667f, 51.125f, 52.0f, 81.0f, 2.0f },
  //        { 81.0f, 27.833f, 42.333f, 60.111f, 67.125f, -999.25f, 49.0f, 45.0f }, { 39.0f, 13.5f, 30.5f, 47.0f, 62.6f, 61.333f, 6.0f, 30.0f } };
  //    float[][] expected3 = new float[][] { { 75.0f, 51.833f, 46.2f, 40.0f, 30.333f, 44.75f, 30.0f, 29.0f }, { 28.0f, 46.333f, 36.571f, -999.25f, -999.25f, -999.25f, 81.0f, 2.0f },
  //        { 81.0f, 35.0f, 36.429f, -999.25f, -999.25f, -999.25f, 49.0f, 45.0f }, { 39.0f, 29.0f, 28.4f, 47.0f, 73.0f, 46.5f, 6.0f, 30.0f } };
  //
  //    //    Check the results
  //    assertEquals(true, TestHelperUtil.testResult(result1, expected1, 4, 8, 0.001));
  //    assertEquals(true, TestHelperUtil.testResult(result2, expected2, 4, 8, 0.001));
  //    assertEquals(true, TestHelperUtil.testResult(result3, expected3, 4, 8, 0.001));
  //  }

  //  public void testBlending() {
  //    //    run the algorithm
  //    Grid result1 = WeightedSmoothImpl.weightedSmooth(_propertyA, _propertyD, 1.0, 2, 2, true, false, true, "smooth grid");
  //
  //    float[][] expected1 = new float[][] { { 69.0f, 72.0f, 25.333f, 38.0f, 52.5f, 53.0f, 39.833f, 35.5f }, { 46.0f, 15.0f, 98.0f, 60.975f, 56.345f, 54.703f, 49.888f, 39.333f },
  //        { 64.111f, 2.0f, 23.0f, 52.493f, 64.16f, -999.25f, 43.370f, 35.5f }, { 44.062f, 29.888f, 20.333f, 62.861f, 65.472f, 51.222f, 28.083f, 32.5f } };
  //
  //    //    Check the results
  //    assertEquals(true, TestHelperUtil.testResult(result1, expected1, 4, 8, 0.001));
  //  }

}
