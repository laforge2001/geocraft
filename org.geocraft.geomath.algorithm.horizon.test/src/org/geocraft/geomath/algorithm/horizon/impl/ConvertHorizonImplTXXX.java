/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.geomath.algorithm.horizon.impl;


import junit.framework.TestCase;


public class ConvertHorizonImplTXXX extends TestCase {

  //  static final float[][] DATAASUPER2 = new float[][] {
  //
  //    {
  //       0,  1,  2,  3,  4,  5,  6,  7,  8
  //    }, {
  //      10, 11, 12, 13, 14, 15, 16, 17, 18
  //    }, {
  //      20, 21, 22, 23, 24, 25, 26, 27, 28
  //    }, {
  //      30, 31, 32, 33, 34, 35, 36, 37, 38
  //    }, {
  //      40, 41, 42, 43, 44, 45, 46, 47, 48
  //    },
  //  };
  //
  //  static final float[][] DATAA = new float[][] {
  //    { 0, 2, 4, 6, 8 }, { 20, 22, 24, 26, 28 }, { 40, 42, 44, 46, 48 },
  //  };
  //
  //  static final float[][] DATAASUB2 = new float[][] {
  //    { 0, 4, 8 }, { 40, 44, 48 },
  //  };
  //
  //  static final float[][] DATAASUBHOR = new float[][] {
  //    { 0, 2, 4, 6 }, { 20, 22, 24, 26 },
  //  };
  //
  //  static final float[][] DATAASUPER2SUBHOR = new float[][] {
  //    {
  //      12, 13, 14, 15, 16, 17
  //    }, {
  //      22, 23, 24, 25, 26, 27
  //    }, {
  //      32, 33, 34, 35, 36, 37
  //    },
  //  };
  //
  //  static final float[][] DATABSUPER3 = new float[][] {
  //
  //    {
  //      -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999
  //    }, {
  //      -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999
  //    }, {
  //      -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999
  //    }, {
  //      -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999
  //    }, {
  //      -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999
  //    }, {
  //      -999, -999, -999, -999, -999,    1,    1,    1, -999, -999, -999, -999, -999
  //    }, {
  //      -999, -999, -999, -999, -999,    1,    1,    1, -999, -999, -999, -999, -999
  //    }, {
  //      -999, -999, -999, -999, -999,    1,    1,    1, -999, -999, -999, -999, -999
  //    }, {
  //      -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999
  //    }, {
  //      -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999
  //    }, {
  //      -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999
  //    }, {
  //      -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999
  //    }, {
  //      -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999, -999
  //    },
  //  };
  //
  //  static final float[][] DATAB = new float[][] {
  //    { -999, -999, -999, -999, -999 }, { -999, -999, -999, -999, -999 }, { -999, -999, 1, -999, -999 },
  //    { -999, -999, -999, -999, -999 }, { -999, -999, -999, -999, -999 },
  //  };
  //
  //  static final float[][] DATABSUB2 = new float[][] {
  //    { -999, -999, -999 }, { -999, 1, -999 }, { -999, -999, -999 },
  //  };
  //
  //  static final float[][] DATAC = new float[][] {
  //
  //    {
  //      9000, 1000, 9000, 1000, 9000, 1000, 9000, 1000, 9000
  //    }, {
  //      8000, 2000, 8000, 2000, 8000, 2000, 8000, 2000, 8000
  //    }, {
  //      7000, 3000, 7000, 3000, 7000, 3000, 7000, 3000, 7000
  //    }, {
  //      6000, 4000, 6000, 4000, 6000, 4000, 6000, 4000, 6000
  //    }, {
  //      5000, 5000, 5000, 5000, 5000, 5000, 5000, 5000, 5000
  //    }, {
  //      4000, 6000, 4000, 6000, 4000, 6000, 4000, 6000, 4000
  //    }, {
  //      3000, 7000, 3000, 7000, 3000, 7000, 3000, 7000, 3000
  //    }, {
  //      2000, 8000, 2000, 8000, 2000, 8000, 2000, 8000, 2000
  //    }, {
  //      1000, 9000, 1000, 9000, 1000, 9000, 1000, 9000, 1000
  //    },
  //  };
  //
  //  static final float[][] EXPECTED = new float[][] {
  //
  //    {
  //      9000, 1000, 9000, 1000, 9000, 1000, 9000, 1000
  //    }, {
  //      8000, 2000, 8000, 2000, 8000, 2000, 8000, 2000
  //    }, {
  //      7000, 3000, 7000, 3000, 7000, 3000, 7000, 3000
  //    }, {
  //      6000, 4000, 6000, 4000, 6000, 4000, 6000, 4000
  //    }, {
  //      5000, 5000, 5000, 5000, 5000, 5000, 5000, 5000
  //    }, {
  //      4000, 6000, 4000, 6000, 4000, 6000, 4000, 6000
  //    }, {
  //      3000, 7000, 3000, 7000, 3000, 7000, 3000, 7000
  //    }, {
  //      2000, 8000, 2000, 8000, 2000, 8000, 2000, 8000
  //    },
  //  };
  //
  //  private Grid _property1;
  //  private Grid _property2;
  //  private Point2D.Double _nOrigin;
  //  private float _zNull;
  //
  //  protected void setUp() {
  //
  //    _nOrigin = new Point2D.Double();
  //
  //    int rows = 8;
  //    int cols = 8;
  //    float[][] data = new float[rows][cols];
  //    Unit dataUnits1 = Unit.MILLISECONDS;
  //    Unit dataUnits2 = Unit.MILLISECONDS;
  //    Domain domainType1 = Domain.TIME;
  //    Domain domainType2 = Domain.TIME;
  //
  //    TestHelperUtil.initData(data, -999.25);
  //
  //    // create a new grid2d object
  //    _zNull = (float) -999.25;
  //    _property1 = TestHelperUtil.createGrid("C", 0, 0, 25, 25, 9, 9, 0, _zNull, DATAC, domainType1, dataUnits1,
  //      SampleDataType.FLOAT);
  //    _property2 = TestHelperUtil.createGrid("Feet", _nOrigin.x, _nOrigin.y, 25, 25, 8, 8, 0, _zNull, data, domainType2,
  //      dataUnits2, SampleDataType.FLOAT);
  //  }
  //
  //  public void testCnvrtHorizon() {
  //
  //    Grid result = ConvertHorizonImpl.convertHorizon(_property1, _property2, true, "convert Horizon");
  //
  //    assert result != null : "result should not be null";
  //
  //    assertEquals(true, TestHelperUtil.testResult(result, EXPECTED, EXPECTED.length, EXPECTED[0].length));
  //
  //    Grid result2 = ConvertHorizonImpl.convertHorizon(_property1, "test", _nOrigin.x, _nOrigin.y, 25, 25, 8, 8, 0,
  //                               _zNull, Domain.TIME, Unit.MILLISECONDS, true);
  //
  //    assert result2 != null : "result2 should not be null";
  //
  //    assertEquals(true, TestHelperUtil.testResult(result2, EXPECTED, EXPECTED.length, EXPECTED[0].length));
  //
  //    result2 = ConvertHorizonImpl.convertHorizon(_property1, "test", _nOrigin.x, _nOrigin.y, 25, 25, 8, 8, 0, _zNull,
  //      Domain.TIME, Unit.MILLISECONDS, false);
  //
  //    assert result2 != null : "result2 should not be null";
  //
  //    assertEquals(true, TestHelperUtil.testResult(result2, EXPECTED, EXPECTED.length, EXPECTED[0].length));
  //  }
  //
  //  public void testInCompatibleUnits() {
  //
  //    int rows = 8;
  //    int cols = 8;
  //    float[][] data = new float[rows][cols];
  //
  //    TestHelperUtil.initData(data, _zNull);
  //
  //    Grid prop3 = TestHelperUtil.createGrid("Units_InCompatibility_Test", _nOrigin.x, _nOrigin.y, 25, 25, rows,
  //                             cols, 0, _zNull, data, Domain.LENGTH, Unit.FOOT, SampleDataType.FLOAT);
  //
  //    Grid result = ConvertHorizonImpl.convertHorizon(_property1, prop3, true, "convert_horizon");
  //
  //    assertEquals(null, result);
  //  }
  //
  //  public void testSubHorizon() {
  //
  //    // create a new grid2d object
  //    Grid horA = (Grid) GridFactory.getInstance().create("A", 0, 0, 2, 2, 3, 5, 0.0, _zNull, DATAA, Domain.LENGTH,
  //                    Unit.METER);
  //
  //    // create store properties for the grid2d object
  //    Properties storeProperties = new Properties();
  //
  //    storeProperties.setProperty("StoreType", "InMemoryTest");
  //
  //    // Create a new grid2d property
  //    Grid propA = new Grid("AProperty", new InMemoryMapper(), horA, "A", SampleDataType.FLOAT);
  //
  //    propA.setFloatValues(TestHelperUtil.createFloatSeries(DATAA, Unit.METER), _zNull);
  //
  //    // Make sure the horizon is not null
  //    assertNotNull("Result from new Grid was null", horA);
  //    assertNotNull("Result from new Grid was null", propA);
  //
  //    // create a new grid2d object
  //    Grid horASubHor = (Grid) GridFactory.getInstance().create("ASUBHOR", 0, 0, 2, 2, 2, 4, 0.0, _zNull, DATAASUBHOR,
  //                          Domain.LENGTH, Unit.METER);
  //
  //    // Create a new grid2d property
  //    Grid propASubHor = new Grid("AProperty", new InMemoryMapper(), horASubHor, "A", SampleDataType.FLOAT);
  //
  //    propASubHor.setFloatValues(TestHelperUtil.createFloatSeries(DATAASUBHOR, Unit.METER), _zNull);
  //
  //    // Make sure the horizon is not null
  //    assertNotNull("Result from new Grid was null", horASubHor);
  //    assertNotNull("Result from new Grid was null", propASubHor);
  //
  //    // create a new grid2d object
  //    Grid horASub2 = (Grid) GridFactory.getInstance().create("ASUB2", 0, 0, 4, 4, 2, 3, 0.0, _zNull, DATAASUB2,
  //                        Domain.LENGTH, Unit.METER);
  //
  //    // Create a new grid2d property
  //    Grid propASub2 = new Grid("AProperty", new InMemoryMapper(), horASub2, "A", SampleDataType.FLOAT);
  //
  //    propASub2.setFloatValues(TestHelperUtil.createFloatSeries(DATAASUB2, Unit.METER), _zNull);
  //
  //    // Make sure the horizon is not null
  //    assertNotNull("Result from new Grid was null", horASub2);
  //    assertNotNull("Result from new Grid was null", propASub2);
  //
  //    // create a new grid2d object
  //    Grid horASuper2 = (Grid) GridFactory.getInstance().create("ASUPER2", 0, 0, 1, 1, 5, 9, 0.0, _zNull, DATAASUPER2,
  //                          Domain.LENGTH, Unit.METER);
  //
  //    // Create a new grid2d property
  //    Grid propASuper2 = new Grid("AProperty", new InMemoryMapper(), horASuper2, "A", SampleDataType.FLOAT);
  //
  //    propASuper2.setFloatValues(TestHelperUtil.createFloatSeries(DATAASUPER2, Unit.METER), _zNull);
  //
  //    // Make sure the horizon is not null
  //    assertNotNull("Result from new Grid was null", horASuper2);
  //    assertNotNull("Result from new Grid was null", propASuper2);
  //
  //    // create a new grid2d object
  //    Grid horASuper2SubHor = (Grid) GridFactory.getInstance().create("ASUPER2SUBHOR", 2, 1, 1, 1, 3, 6, 0.0, _zNull,
  //                                DATAASUPER2SUBHOR, Domain.LENGTH, Unit.METER);
  //
  //    // Create a new grid2d property
  //    Grid propASuper2SubHor = new Grid("AProperty", new InMemoryMapper(), horASuper2SubHor, "A",
  //                                         SampleDataType.FLOAT);
  //
  //    propASuper2SubHor.setFloatValues(TestHelperUtil.createFloatSeries(DATAASUPER2SUBHOR, Unit.METER), _zNull);
  //
  //    // Make sure the horizon is not null
  //    assertNotNull("Result from new Grid was null", horASuper2SubHor);
  //    assertNotNull("Result from new Grid was null", propASuper2SubHor);
  //
  //    runTest(propA, horA, propA, false);
  //    runTest(propA, horASubHor, propASubHor, false);
  //    runTest(propASuper2, horASuper2SubHor, propASuper2SubHor, false);
  //    runTest(propA, horASub2, propASub2, false);
  //    runTest(propA, horASuper2, propASuper2, false);
  //  }
  //
  //  public void testNullValueHorizons() {
  //
  //    // Test horizon with null values
  //    double xorg = 0.0;
  //    double yorg = 0.0;
  //    double dcol = 2.0;
  //    double drow = 2.0;
  //
  //    // create a new grid2d object
  //    _zNull = (float) -999.0;
  //
  //    Grid horB = (Grid) GridFactory.getInstance().create("B", xorg, yorg, dcol, drow, 5, 5, 0.0, _zNull, DATAB,
  //                    Domain.LENGTH, Unit.METER);
  //
  //    // create store properties for the grid2d object
  //    Properties storeProperties = new Properties();
  //
  //    storeProperties.setProperty("StoreType", "InMemoryTest");
  //
  //    // Create a new grid2d property
  //    Grid propB = new Grid("BProperty", new InMemoryMapper(), horB, "B", SampleDataType.FLOAT);
  //
  //    propB.setFloatValues(TestHelperUtil.createFloatSeries(DATAB, Unit.METER), _zNull);
  //
  //    // Make sure the horizon is not null
  //    assertNotNull("Result from new Grid was null", horB);
  //    assertNotNull("Result from new Grid was null", propB);
  //
  //    // create a new grid2d object
  //    Grid horBSub2 = (Grid) GridFactory.getInstance().create("BSUB2", xorg, yorg, dcol * 2, drow * 2, 3, 3, 0.0, _zNull,
  //                        DATABSUB2, Domain.LENGTH, Unit.METER);
  //
  //    // Create a new grid2d property
  //    Grid propBSub2 = new Grid("BProperty", new InMemoryMapper(), horBSub2, "B", SampleDataType.FLOAT);
  //
  //    propBSub2.setFloatValues(TestHelperUtil.createFloatSeries(DATABSUB2, Unit.METER), _zNull);
  //
  //    // Make sure the horizon is not null
  //    assertNotNull("Result from new Grid was null", horBSub2);
  //    assertNotNull("Result from new Grid was null", propBSub2);
  //
  //    // create a new grid2d object
  //    Grid horBSuper3 = (Grid) GridFactory.getInstance().create("BSUPER3", xorg, yorg, dcol / 3, drow / 3, 13, 13, 0.0, _zNull,
  //                          DATABSUPER3, Domain.LENGTH, Unit.METER);
  //
  //    // Create a new grid2d property
  //    Grid propBSuper3 = new Grid("BProperty", new InMemoryMapper(), horBSuper3, "B", SampleDataType.FLOAT);
  //
  //    propBSuper3.setFloatValues(TestHelperUtil.createFloatSeries(DATABSUPER3, Unit.METER), _zNull);
  //
  //    // Make sure the horizon is not null
  //    assertNotNull("Result from new Grid was null", horBSuper3);
  //    assertNotNull("Result from new Grid was null", propBSuper3);
  //
  //    runTest(propB, horB, propB, false);
  //    runTest(propB, horBSub2, propBSub2, false);
  //    runTest(propB, horBSuper3, propBSuper3, false);
  //  }
  //
  //  public void testNullWithDiffGeometry() {
  //
  //    // Test horizon with null values with different horizon geometry
  //    double xorg = 1618543.0;
  //    double yorg = 9761237.5;
  //    double dcol = 1968.48;
  //    double drow = 2099.68;
  //
  //    // create a new grid2d object
  //    _zNull = (float) -999.0;
  //
  //    Grid horC = (Grid) GridFactory.getInstance().create("C", xorg, yorg, dcol, drow, 5, 5, 0.0, _zNull, DATAB,
  //                    Domain.LENGTH, Unit.METER);
  //
  //    // create store properties for the grid2d object
  //    Properties storeProperties = new Properties();
  //
  //    storeProperties.setProperty("StoreType", "InMemoryTest");
  //
  //    // Create a new grid2d property
  //    Grid propC = new Grid("CProperty", new InMemoryMapper(), horC, "C", SampleDataType.FLOAT);
  //
  //    propC.setFloatValues(TestHelperUtil.createFloatSeries(DATAB, Unit.METER), _zNull);
  //
  //    // Make sure the horizon is not null
  //    assertNotNull("Result from new Grid was null", horC);
  //    assertNotNull("Result from new Grid was null", propC);
  //
  //    // create a new grid2d object
  //    Grid horCSub2 = (Grid) GridFactory.getInstance().create("CSUB2", xorg, yorg, dcol * 2, drow * 2, 3, 3, 0.0, _zNull,
  //                        DATABSUB2, Domain.LENGTH, Unit.METER);
  //
  //    // Create a new grid2d property
  //    Grid propCSub2 = new Grid("CProperty", new InMemoryMapper(), horCSub2, "C", SampleDataType.FLOAT);
  //
  //    propCSub2.setFloatValues(TestHelperUtil.createFloatSeries(DATABSUB2, Unit.METER), _zNull);
  //
  //    // Make sure the horizon is not null
  //    assertNotNull("Result from new Grid was null", horCSub2);
  //    assertNotNull("Result from new Grid was null", propCSub2);
  //
  //    // create a new grid2d object
  //    Grid horCSuper3 = (Grid) GridFactory.getInstance().create("CSUPER3", xorg, yorg, dcol / 3, drow / 3, 13, 13, 0.0, _zNull,
  //                          DATABSUPER3, Domain.LENGTH, Unit.METER);
  //
  //    // Create a new grid2d property
  //    Grid propCSuper3 = new Grid("CProperty", new InMemoryMapper(), horCSuper3, "C", SampleDataType.FLOAT);
  //
  //    propCSuper3.setFloatValues(TestHelperUtil.createFloatSeries(DATABSUPER3, Unit.METER), _zNull);
  //
  //    // Make sure the horizon is not null
  //    assertNotNull("Result from new Grid was null", horCSuper3);
  //    assertNotNull("Result from new Grid was null", propCSuper3);
  //
  //    runTest(propC, horC, propC, false);
  //    runTest(propC, horCSub2, propCSub2, false);
  //    runTest(propC, horCSuper3, propCSuper3, false);
  //  }
  //
  //  private void runTest(Grid fromProp, Grid toHor, Grid toProp, boolean noExtrap) {
  //
  //    double nullValue = toProp.getNullValue();
  //    int numCols = toHor.getNumColumns();
  //    int numRows = toHor.getNumRows();
  //    float[][] newData = new float[numRows][numCols];
  //
  //    TestHelperUtil.initData(newData, nullValue);
  //
  //    // create store properties for the grid2d object
  //    Properties newProperties = new Properties();
  //
  //    newProperties.setProperty("StoreType", "InMemoryTest");
  //
  //    // get the corner points
  //    CoordinateSeries cornerPoints = toHor.getCornerPoints();
  //
  //    // Initialize the horizon
  //    Grid newHor = new Grid("newHor", new InMemoryMapper(), toHor.getFeature(), cornerPoints, numCols, numRows,
  //                               toHor.getPrimaryDomain(), toHor.getDatumElevation(), null);
  //
  //    // Create a new grid2d property
  //    Grid newProp = new Grid("newProp", new InMemoryMapper(), newHor, "new", SampleDataType.FLOAT);
  //
  //    // add data to the property
  //    Unit newdepthUnit = toProp.getFloatValues().getUnit();
  //
  //    newProp.setFloatValues(TestHelperUtil.createFloatSeries(newData, newdepthUnit), nullValue);
  //
  //    Grid result = null;
  //
  //    try {
  //
  //      // run the algorithm
  //      result = ConvertHorizonImpl.convertHorizon(fromProp, newProp, noExtrap, "convert_horizon");
  //    } catch (Exception e) {
  //      e.printStackTrace();
  //      fail("ConvertHorizonImplTest: Failed to convert the horizon.");
  //    }
  //
  //    assert result != null : "result should not be null";
  //
  //    assertEquals(true, TestHelperUtil.testResult(result, toProp, numRows, numCols));
  //
  //  }
}
