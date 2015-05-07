/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.geomath.algorithm.util;


import java.util.Properties;

import org.geocraft.core.common.math.MathUtil;
import org.geocraft.core.model.datatypes.DataType;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;


public class TestHelperUtil {

  /*
   * Tests for equality between doubles within some epsilon value
   */
  @Deprecated
  private static boolean isEqual(final double d1, final double d2, final double epsilon) {
    return MathUtil.isEqual(d1, d2, epsilon);
  }

  /*
   * Initializes a double array of double values with an initial value
   */
  public static void initData(final float[][] data, final double value) {

    for (int i = 0; i < data.length; i++) {
      for (int j = 0; j < data[0].length; j++) {
        data[i][j] = (float) value;
      }
    }
  }

  /*
   * Tests if an array is populated with a single value
   */
  public static boolean arrayIsEqual(final float[] data, final float val) {
    for (int i = 0; i < data.length; ++i) {
      if (data[i] != val) {
        return false;
      }
    }
    return true;
  }

  /*
   * Tests if an array is populated with a single float value within an epsilon
   */
  public static boolean arrayIsEqual(final float[] data, final float val, final double epsilon) {
    for (int i = 0; i < data.length; ++i) {
      if (!isEqual(data[i], val, epsilon)) {
        return false;
      }
    }
    return true;
  }

  /*
   * Tests if an array is populated with a single double value within an epsilon
   */
  public static boolean arrayIsEqual(final double[] data, final double val, final double epsilon) {
    for (int i = 0; i < data.length; ++i) {
      if (!isEqual(data[i], val, epsilon)) {
        return false;
      }
    }
    return true;
  }

  /*
   * Tests if two single arrays of floats are equal
   */
  public static boolean arrayIsEqual(final float[] data, final float[] data2) {

    if (data.length != data2.length) {
      return false;
    }
    for (int i = 0; i < data.length; ++i) {
      if (data[i] != data2[i]) {
        return false;
      }
    }

    return true;
  }

  /*
   * Tests if two single arrays of floats are equal
   */
  public static boolean arrayIsEqual(final float[] data, final float[] data2, final double epsilon) {

    if (data.length != data2.length) {
      return false;
    }
    for (int i = 0; i < data.length; ++i) {
      if (!isEqual(data[i], data2[i], epsilon)) {
        return false;
      }
    }

    return true;
  }

  /*
   * Tests if two double arrays consisting of double values are equal
   */
  public static boolean arrayIsEqual(final float[][] data, final float[][] data2) {

    for (int i = 0; i < data.length; ++i) {
      for (int j = 0; j < data[i].length; ++j) {
        if (data[i][j] != data2[i][j]) {
          return false;
        }
      }
    }
    return true;
  }

  /*
   * Tests if two double arrays consisting of double values are equal within some epsilon value
   */
  public static boolean arrayIsEqual(final float[][] data, final float[][] data2, final double epsilon) {

    for (int i = 0; i < data.length; ++i) {
      for (int j = 0; j < data[i].length; ++j) {
        if (!isEqual(data[i][j], data2[i][j], epsilon)) {
          return false;
        }
      }
    }
    return true;
  }

  /*
   * Tests equality between a Grid's data results and the expected values
   */
  public static boolean testResult(final Grid3d result, final float[][] expected, final int rows, final int cols) {

    float[][] results = result.getValues();

    return arrayIsEqual(expected, results);
  }

  /*
   * Tests equality between a FloatMeasurementSeries and a float array of expected values
   */
  public static boolean testResult(final float[][] values, final float[][] expected) {

    for (int i = 0; i < values.length; i++) {
      for (int j = 0; j < values[i].length; j++) {
        if (expected[i][j] != values[i][j]) {
          return false;
        }
      }
    }

    return true;
  }

  /*
   * Tests equality between a Grid's data results and the expected values
   */
  public static boolean testResult(final Grid3d result, final float[][] expected) {

    return testResult(result.getValues(), expected);
  }

  /*
   * Tests equality between a Trace's data results and the expected values
   */
  public static boolean testResult(final Trace testTrace, final float[] expected) {

    float[] output = testTrace.getData();

    // verify
    for (int i = 0; i < output.length; i++) {
      if (expected[i] != output[i]) {
        return false;
      }
    }
    return true;
  }

  /*
   * Tests equality between a Grid's data results and the expected values within an epsilon value
   */
  public static boolean testResult(final Grid3d result, final float[][] expected, final int rows, final int cols,
      final double epsilon) {

    float[][] horOutData = result.getValues();

    return arrayIsEqual(expected, horOutData, epsilon);
  }

  /*
   * Tests equality between two GridProperties (their values)
   */
  public static boolean testResult(final Grid3d result, final Grid3d expected, final int rows, final int cols) {

    float[][] expectedVals = expected.getValues();

    return testResult(result, expectedVals, rows, cols);
  }

  /*
   * Tests equality between two GridProperties (their values) w/in an epsilon
   */
  public static boolean testResult(final Grid3d result, final Grid3d expected, final int rows, final int cols,
      final double epsilon) {

    float[][] expectedVals = expected.getValues();

    return testResult(result, expectedVals, rows, cols, epsilon);
  }

  /*
   * creates a grid2d
   */
  public static GridGeometry3d createGrid(final String gridName, final double x, final double y,
      final double colSpacing, final double rowSpacing, final int nRows, final int nCols, final double rotation,
      final double nullValue, final float[][] data, final Domain domainType, final Unit dataUnit) {

    return new GridGeometry3d(gridName, x, y, colSpacing, rowSpacing, nRows, nCols, rotation);
  }

  /**
   * creates a grid2d Property from an existing grid2d
   */
  public static Grid3d createGrid(final GridGeometry3d grid, final float[][] data, final Unit dataUnit,
      final DataType sampleDataType, final float nullValue) {

    // create store properties for the grid2d object
    Properties storePropertiesA = new Properties();

    storePropertiesA.setProperty("StoreType", "InMemoryTest");

    // Create a new grid2d property
    Grid3d propertyA = new Grid3d("AProperty", grid);

    propertyA.setValues(data, nullValue, dataUnit);

    return propertyA;
  }

  /**
   * Creates a test grid2d property
   */
  public static Grid3d createGrid(final String gridName, final double x, final double y, final double colSpacing,
      final double rowSpacing, final int nRows, final int nCols, final double rotation, final float nullValue,
      final float[][] data, final Domain domainType, final Unit dataUnit, final DataType sampleDataType) {

    GridGeometry3d gridA = createGrid(gridName, x, y, colSpacing, rowSpacing, nRows, nCols, rotation, nullValue, data,
        domainType, dataUnit);

    return createGrid(gridA, data, dataUnit, sampleDataType, nullValue);
  }
}
