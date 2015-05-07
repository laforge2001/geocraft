/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.geomath.algorithm.horizon.impl;


import junit.framework.TestCase;


public class InterpolateTXXX extends TestCase {

  //  static final double[] X = new double[] { 149.999, 250.001, 250.001, 149.999, 149.999};
  //  static final double[] Y = new double[] { 149.999, 149.999, 250.001, 250.001, 149.999};
  //
  //  static final float[][] DATA_A = new float[][] {
  //    {0,  0.616f,   6.575f,  11.532f,  14.734f,  15.693f,  14.262f,  10.660f,  5.435f,  0},
  //    {0,  6.649f,  70.907f, 124.371f, 158.900f, 169.238f, 153.811f, 114.967f, 58.621f,  0},
  //    {0, 12.025f, 128.238f, 224.927f, 287.374f, 306.070f, 278.170f, 207.921f, 106.017f, 2.217E-15f},
  //    {0, 16.235f, 173.132f, 303.671f, 387.979f, 413.221f, 375.553f, 280.711f, 143.133f, 0},
  //    {0, 18.850f, 201.018f, 352.582f, 450.469f, 479.776f, 436.042f, 325.924f, 166.186f, 0},
  //    {0, 19.610f, 209.126f, 366.804f, 468.639f, 499.128f, 453.629f, 339.070f, 172.890f, 3.615E-15f},
  //    {0, 18.469f, 196.954f, 345.454f, 441.363f, 470.077f, 427.227f, 319.335f, 162.827f, 0},
  //    {0, 15.510f, 165.403f, 290.114f, 370.658f, 394.772f, 358.786f, 268.178f, 136.743f, 0},
  //    {0, 11.033f, 117.660f, 206.375f, 263.670f, 280.825f, 255.226f, 190.771f,  97.273f, 0},
  //    {0,  5.476f,  58.401f, 102.434f, 130.873f, 139.387f, 126.681f,  94.689f,  48.281f, 0},
  //  };
  //
  //  private CoordinateSystem _coordSys;
  //  private float _zNull;
  //  private Grid _propertyA;
  //  private Grid _gridWithHole;
  //  private Unit _dataUnits;
  //  private MapPolygonAOI _aoi;
  //
  //  private void fillExpectedValues(float[][] expected) {
  //    // Fill in everything else but the hole with input values
  //    for (int i1 = 0; i1 < expected.length; i1++) {
  //      for (int i2 = 0; i2 < expected[i1].length; i2++) {
  //         if (expected[i1][i2] == _zNull) {
  //           expected[i1][i2] = DATA_A[i1][i2];
  //         }
  //      }
  //    }
  //  }
  //
  //  private void testExpectedValues(Grid newProp, float[][] expected) {
  //    // Fill in everything else but the hole with input values
  //    fillExpectedValues(expected);
  //
  //    assertEquals(true, TestHelperUtil.testResult(newProp, expected, 10, 10, 0.001));
  //    assertEquals(newProp.getNullValue(), -999.25);
  //  }
  //
  //  public void setUp(){
  //    _coordSys = new CoordinateSystem("UTM15", "UTM15", Types.Domain.LENGTH);
  //
  //    // Initialize the application units
  //    ApplicationPreferences.getApplicationPreferences().setTimeUnit(Unit.MILLISECONDS);
  //    ApplicationPreferences.getApplicationPreferences().setVerticalDistanceUnit(Unit.MILLISECONDS);
  //    ApplicationPreferences.getApplicationPreferences().setHorizontalDistanceUnit(Unit.METER);
  //    ApplicationPreferences.getApplicationPreferences().setSeismicDatumElevation(0.0f);
  //    ApplicationPreferences.getApplicationPreferences().setDepthCoordinateSystem(_coordSys);
  //
  //    _zNull = (float) -999.25;
  //    _propertyA = TestHelperUtil.createGrid("A", 0, 0, 50, 50, 10, 10, 0.0, _zNull, DATA_A, Domain.LENGTH,
  //        Unit.MILLISECONDS, SampleDataType.FLOAT);
  //
  //    _dataUnits = ApplicationPreferences.getApplicationPreferences().getHorizontalDistanceUnit();
  //
  //    _aoi = new MapPolygonAOI("test", _dataUnits);
  //    _aoi.addArea(X, Y);
  //
  //    _gridWithHole = EraseAreaImpl.eraseArea(_propertyA, _aoi, "erase area");
  //  }
  //
  //
  //  public void testInterpolateTessellate() {
  //
  //    // run the algorithm
  //    InterpolateTessellateImpl impl = new InterpolateTessellateImpl();
  //    Grid newProp = impl.interpolateTessellate(_gridWithHole, null, "smooth_horizon");
  //
  //     // Initially contains only values in the hole
  //    float[][] expected = new float[][] {
  //      {-999.25f, -999.25f, -999.25f, -999.25f, -999.25f, -999.25f,  -999.25f, -999.25f, -999.25f, -999.25f},
  //      {-999.25f, -999.25f, -999.25f, -999.25f, -999.25f, -999.25f,  -999.25f, -999.25f, -999.25f, -999.25f},
  //      {-999.25f, -999.25f, -999.25f, -999.25f, -999.25f, -999.25f,  -999.25f, -999.25f, -999.25f, -999.25f},
  //      {-999.25f, -999.25f, -999.25f,  244.195f, 325.871f, 361.708f, -999.25f, -999.25f, -999.25f, -999.25f},
  //      {-999.25f, -999.25f, -999.25f,  282.693f, 364.368f, 400.205f, -999.25f, -999.25f, -999.25f, -999.25f},
  //      {-999.25f, -999.25f, -999.25f,  321.190f, 402.865f, 438.702f, -999.25f, -999.25f, -999.25f, -999.25f},
  //      {-999.25f, -999.25f, -999.25f, -999.25f, -999.25f, -999.25f,  -999.25f, -999.25f, -999.25f, -999.25f},
  //      {-999.25f, -999.25f, -999.25f, -999.25f, -999.25f, -999.25f,  -999.25f, -999.25f, -999.25f, -999.25f},
  //      {-999.25f, -999.25f, -999.25f, -999.25f, -999.25f, -999.25f,  -999.25f, -999.25f, -999.25f, -999.25f},
  //      {-999.25f, -999.25f, -999.25f, -999.25f, -999.25f, -999.25f,  -999.25f, -999.25f, -999.25f, -999.25f},
  //    };
  //
  //    testExpectedValues(newProp, expected);
  //  }
  //
  //  public void testInterpolateLinear(){
  //
  //    // run the algorithm
  //    Grid newProp = new InterpolateLinearImpl().runInterpolateLinear(_gridWithHole, null, 0, 0, "smooth horizon");
  //
  //    // Initially contains only values in the hole
  //    float[][] expected = new float[][] {
  //      {-999.25f, -999.25f, -999.25f, -999.25f, -999.25f, -999.25f,  -999.25f, -999.25f, -999.25f, -999.25f},
  //      {-999.25f, -999.25f, -999.25f, -999.25f, -999.25f, -999.25f,  -999.25f, -999.25f, -999.25f, -999.25f},
  //      {-999.25f, -999.25f, -999.25f, -999.25f, -999.25f, -999.25f,  -999.25f, -999.25f, -999.25f, -999.25f},
  //      {-999.25f, -999.25f, -999.25f, 239.398f,  300.106f, 336.009f, -999.25f, -999.25f, -999.25f, -999.25f},
  //      {-999.25f, -999.25f, -999.25f, 272.482f,  341.449f, 382.679f, -999.25f, -999.25f, -999.25f, -999.25f},
  //      {-999.25f, -999.25f, -999.25f, 292.787f,  367.122f, 410.789f, -999.25f, -999.25f, -999.25f, -999.25f},
  //      {-999.25f, -999.25f, -999.25f, -999.25f, -999.25f, -999.25f,  -999.25f, -999.25f, -999.25f, -999.25f},
  //      {-999.25f, -999.25f, -999.25f, -999.25f, -999.25f, -999.25f,  -999.25f, -999.25f, -999.25f, -999.25f},
  //      {-999.25f, -999.25f, -999.25f, -999.25f, -999.25f, -999.25f,  -999.25f, -999.25f, -999.25f, -999.25f},
  //      {-999.25f, -999.25f, -999.25f, -999.25f, -999.25f, -999.25f,  -999.25f, -999.25f, -999.25f, -999.25f},
  //    };
  //
  //    testExpectedValues(newProp, expected);
  //  }
}
