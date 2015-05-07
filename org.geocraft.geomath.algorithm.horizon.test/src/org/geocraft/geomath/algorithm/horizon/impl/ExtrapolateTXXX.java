/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.geomath.algorithm.horizon.impl;


/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

import junit.framework.TestCase;


public class ExtrapolateTXXX extends TestCase {

  //  static final float[][] DATA_A = new float[][] {
  //    {-999.25f, -999.25f, -999.25f, -999.25f, -999.25f, -999.25f, -999.25f, -999.25f},
  //    {-999.25f,   53,       59,       21,       40,       39,       56,     -999.25f},
  //    {-999.25f,   46,       32,       67,       51,       35,       34,     -999.25f},
  //    {-999.25f,   44,       38,       87,       57,       37,       45,     -999.25f},
  //    {-999.25f,   48,       34,       89,       52,       31,       47,     -999.25f},
  //    {-999.25f,   32,       68,       69,       34,       41,       43,     -999.25f},
  //    {-999.25f,   57,       43,       44,       42,       47,       30,     -999.25f},
  //    {-999.25f, -999.25f, -999.25f, -999.25f, -999.25f, -999.25f, -999.25f, -999.25f}
  //  };
  //
  //  public void testBogus() {
  //    assertTrue(true);
  //  }
  //
  //  public void testExtrapolate() {
  //    //  create a new grid2d object
  //    float zNull = (float) -999.25;
  //    Grid gridA = (Grid)GridFactory.getInstance().create("A", 0, 0, 1, 1, 8, 8, 0.0, zNull, DATA_A,
  //        Domain.TIME, Unit.MILLISECONDS);
  //
  //    // create store properties for the grid2d object
  //    Properties storeProperties = new Properties();
  //    storeProperties.setProperty("StoreType", "InMemoryTest");
  //
  //    // Create a new grid2d property
  //    Grid property = new Grid("BProperty", new InMemoryMapper(),
  //        gridA, "B", SampleDataType.FLOAT);
  //
  //    // add a color to the property
  //    property.setColor(Color.cyan);
  //
  //    // add data to the property
  //    float[] z = MathUtil.convert(DATA_A);
  //    property.setFloatValues(new FloatMeasurementSeries(z, Unit.FOOT), zNull);
  //
  //    // add property to the grid
  //    gridA.addProperty(property);
  //
  //    // gridA run the algorithm
  //    Grid result = ExtrapolateImpl.extrapolate(property, null, 2, 2, 2, 2, "extrapolate horizon");
  //
  //    float[][] expected1 = new float[][] {
  //      {32,      46,     32,    67,    51,    35,     34,    35},
  //      {59,      53,     59,    21,    40,    39,     56,    39},
  //      {32,      46,     32,    67,    51,    35,     34,    35},
  //      {38,      44,     38,    87,    57,    37,     45,    37},
  //      {34,      48,     34,    89,    52,    31,     47,    31},
  //      {68,      32,     68,    69,    34,    41,     43,    41},
  //      {43,      57,     43,    44,    42,    47,     30,    47},
  //      {68,      32,     68,    69,    34,    41,     43,    41}
  //    };
  //
  //    // determine the data series
  //    float[] series = result.getFloatValues().getValues();
  //
  //    // convert into a double multi-dimensional array
  //    float[][] data1 = MathUtil.convert(series, 8, 8);
  //
  //    // check the results
  //    for (int i = 0; i < data1.length; i++) {
  //      for (int j = 0; j < data1[i].length; j++) {
  //        assertEquals(expected1[i][j], data1[i][j]);
  //      }
  //    }
  //  }
}
