/**
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */
package org.geocraft.core.model.grid;


import org.geocraft.core.common.math.MathUtil;
import org.geocraft.core.io.Grid3dInMemoryMapper;
import org.geocraft.core.model.GeologicInterpretation;
import org.geocraft.core.model.datatypes.CornerPointsSeries;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.OnsetType;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.mapper.IGrid3dMapper;


/**
 * A 2D array of data values mapped onto a <code>GridGeometry3d</code>.
 */
public class Grid3d extends GeologicInterpretation {

  /** The geometry (row and columns) onto which the grid bins are mapped. */
  private GridGeometry3d _geometry;

  /** The 2D array of values in [row][column] order. */
  private float[][] _dataValues;

  /** The unit of measurement for the values array. */
  private Unit _dataUnit;

  /** The value representing null in the grid. */
  private float _nullValue;

  /** The minimum (non-null) value in the grid. */
  private float _minValue;

  /** The maximum (non-null) value in the grid. */
  private float _maxValue;

  /** The onset type of the grid. */
  private OnsetType _onsetType = OnsetType.MINIMUM;

  /** The range of (index) tolerance if the row/col values are just outside the grid bounds 
   * for interpolation
   */
  private double _boundsTolerance = 0.5;

  public Grid3d(final String name, final GridGeometry3d geometry) {
    super(name, new Grid3dInMemoryMapper());
    _geometry = geometry;
  }

  /**
   * Constructs a <code>Grid3d</code> entity.
   * 
   * @param name the grid name.
   * @param mapper the grid mapper to the underlying datastore
   */
  public Grid3d(final String name, final IGrid3dMapper mapper) {
    super(name, mapper);
  }

  /**
   * Constructs a <code>Grid3d</code> entity.
   * 
   * @param name the grid name.
   * @param mapper the grid mapper to the underlying datastore.
   * @param geometry the geometry on which the grid is defined.
   */
  public Grid3d(final String name, final IGrid3dMapper mapper, final GridGeometry3d geometry) {
    super(name, mapper);
    _geometry = geometry;
  }

  /**
   * Get the geometry on which the grid is defined.
   * 
   * @return the grid geometry.
   */
  public GridGeometry3d getGeometry() {
    if (_geometry == null) {
      load();
    }
    return _geometry;
  }

  /**
   * Sets the geometry on which the grid is defined..
   * 
   * @param geometry the grid geometry.
   */
  public void setGeometry(final GridGeometry3d geometry) {
    _geometry = geometry;
    setDirty(true);
  }

  /**
   * Gets the unit of measurement for the data values.
   * 
   * @return the unit of measurement for the data values.
   */
  public Unit getDataUnit() {
    load();
    return _dataUnit;
  }

  /**
   * Sets the unit of measurement for the data values.
   * <p>
   * This will optionally set the domain as well, if the unit
   * of measurement represents time or depth (length).
   * 
   * @param dataUnit the unit of measurement for the data values.
   */
  public void setDataUnit(final Unit dataUnit) {
    _dataUnit = dataUnit;
    //setZDomain(dataUnit.getDomain());
    setDirty(true);
  }

  /**
   * Gets the onset type of the grid.
   * 
   * @return the onset type.
   */
  public OnsetType getOnsetType() {
    load();
    return _onsetType;
  }

  /**
   * Sets the onset type of the grid.
   * 
   * @param onsetType the onset type.
   */
  public void setOnsetType(final OnsetType onsetType) {
    _onsetType = onsetType;
    setDirty(true);
  }

  /**
   * Return a read-only copy of the values
   * @return
   */
  public float[][] getReadOnlyValues() {
    return _dataValues;
  }

  /**
   * Gets the 2D array of data values.
   * <p>
   * This method creates a defensive copy of the values and returns the copy.
   * This copy can be modified without affecting the grid.
   * 
   * @return the 2D array of data values.
   */
  public float[][] getValues() {
    load();

    // Return a copy of the values array, not the original array.
    int numRows = _geometry.getNumRows();
    int numCols = _geometry.getNumColumns();
    float[][] values = new float[numRows][numCols];
    if (_dataValues != null) {
      for (int row = 0; row < numRows; row++) {
        System.arraycopy(_dataValues[row], 0, values[row], 0, numCols);
      }
    }
    return values;
  }

  /**
   * Sets the 2D array of grid values.
   * <p>
   * This method creates a defensive copy of the values and stores the copy.
   * It also determines the minimum and maximum non-null values of the grid.
   * 
   * @param values the 2D array of data values.
   */
  public void setValues(final float[][] values) {
    validateArraySizes(values);

    // Store a copy of the values array, not the original array.
    int numRows = _geometry.getNumRows();
    int numCols = _geometry.getNumColumns();
    _dataValues = new float[numRows][numCols];
    for (int row = 0; row < numRows; row++) {
      System.arraycopy(values[row], 0, _dataValues[row], 0, numCols);
    }
    computeMinMax();
    setDirty(true);
  }

  /**
   * Sets the 2D array of grid values.
   * <p>
   * This method creates a defensive copy of the values and stores the copy.
   * It also determines the minimum and maximum non-null values of the grid.
   * 
   * @param values the 2D array of data values.
   */
  public void setValuesWithMinMax(final float[][] values, final float minValue, final float maxValue) {
    validateArraySizes(values);

    // Store a copy of the values array, not the original array.
    int numRows = _geometry.getNumRows();
    int numCols = _geometry.getNumColumns();
    _dataValues = new float[numRows][numCols];
    for (int row = 0; row < numRows; row++) {
      System.arraycopy(values[row], 0, _dataValues[row], 0, numCols);
    }
    _minValue = minValue;
    _maxValue = maxValue;
    setDirty(true);
  }

  /**
   * Computes the minimum and maximum non-null values contained in the grid.
   * If the grid is entirely null, then the min/max values are set to the null value.
   */
  protected void computeMinMax() {
    // Find the minimum and maximum non-null values.
    float minValue = Float.MAX_VALUE;
    float maxValue = -Float.MAX_VALUE;
    boolean allNulls = true;
    for (int row = 0; row < _geometry.getNumRows(); row++) {
      for (int col = 0; col < _geometry.getNumColumns(); col++) {
        if (!MathUtil.isEqual(_dataValues[row][col], _nullValue)) {
          float value = _dataValues[row][col];
          minValue = Math.min(minValue, value);
          maxValue = Math.max(maxValue, value);
          allNulls = false;
        }
      }
    }
    // If the grid is all nulls, then set the min/max to the null value.
    // Otherwise use the computed min/max values.
    if (!allNulls) {
      _minValue = minValue;
      _maxValue = maxValue;
    } else {
      _minValue = _nullValue;
      _maxValue = _nullValue;
    }
  }

  /**
   * Sets the data values of the grid.
   * <p>
   * This method creates a defensive copy of the values and stores the copy.
   * It also determines the minimum and maximum non-null values of the grid.
   * 
   * @param values the 2D array of data values.
   * @param nullValue the null value.
   * @param dataUnit the unit of measurement for the data valyes.
   */
  public void setValues(final float[][] values, final float nullValue, final Unit dataUnit) {
    validateArraySizes(values);
    setDataUnit(dataUnit);
    _nullValue = nullValue; // calling setNullValue() would computeMinMax twice. 
    setValues(values);
  }

  /**
   * Sets the data values of the grid.
   * <p>
   * This method creates a defensive copy of the values and stores the copy.
   * It also determines the minimum and maximum non-null values of the grid.
   * 
   * @param values the 2D array of data values.
   * @param nullValue the null value.
   * @param dataUnit the unit of measurement for the data valyes.
   */
  public void setValuesWithMinMax(final float[][] values, final float nullValue, final Unit dataUnit,
      final float minValue, final float maxValue) {
    validateArraySizes(values);
    setDataUnit(dataUnit);
    _nullValue = nullValue; // calling setNullValue() would computeMinMax twice. 
    setValuesWithMinMax(values, minValue, maxValue);
  }

  /**
   * Gets the data value at the specified row,column location.
   * 
   * @param row the row.
   * @param col the column.
   * @return the data value at the row,column location.
   */
  public float getValueAtRowCol(final int row, final int col) {
    load();
    if (!_geometry.containsRowCol(row, col)) {
      return _nullValue;
    }
    return _dataValues[row][col];
  }

  /**
   * Gets the data value at the specified x,y coordinates.
   * <p>
   * This method currently snaps to the nearest grid cell. No interpolation is done.
   * 
   * @param x the x-coordinate.
   * @param y the y-coordinate.
   * @return the data value at the x,y coordinates.
   */
  public float getValueAtXY(final double x, final double y) {
    load();
    double[] rowcol = _geometry.transformXYToRowCol(x, y, true);
    int row = (int) rowcol[0];
    int col = (int) rowcol[1];
    return getValueAtRowCol(row, col);
  }

  /**
   * Gets the data value at the specified x,y coordinates.
   * <p>
   * If the round flag is set to false, this method will do a linear 
   * interpolation taking into account the 4 adjacent cells
   * 
   * @param x the x-coordinate.
   * @param y the y-coordinate.
   * @param snap if true, will snap to the nearest grid cell; otherwise, will compute interpolated value
   * @return the data value at the x,y coordinates.
   */
  public float getValueAtXY(final double x, final double y, final boolean snap) {
    load();
    if (snap) {
      return getValueAtXY(x, y);
    }
    double[] rc = _geometry.transformXYToRowCol(x, y, false);
    int row = (int) rc[0];
    int col = (int) rc[1];

    rc[0] = adjustElementIfWithinTolerance(rc[0], _dataValues.length - 1);
    rc[1] = adjustElementIfWithinTolerance(rc[1], _dataValues[0].length - 1);

    // Smoothing, so get the row,col coordinates of the 4 surrounding grid cells.
    int row0 = (int) rc[0];
    int row1 = row0 + 1;
    int col0 = (int) rc[1];
    int col1 = col0 + 1;
    double row1wt = rc[0] - row0;
    double row0wt = 1 - row1wt;
    double col1wt = rc[1] - col0;
    double col0wt = 1 - col1wt;

    int[] rowArray = new int[4];
    int[] colArray = new int[4];
    rowArray[0] = row0;
    colArray[0] = col0;
    rowArray[1] = row1;
    colArray[1] = col0;
    rowArray[2] = row0;
    colArray[2] = col1;
    rowArray[3] = row1;
    colArray[3] = col1;

    // Compute the weighting factors for each of the 4 cells.
    double[] sclArray = new double[4];
    sclArray[0] = row0wt * col0wt;
    sclArray[1] = row1wt * col0wt;
    sclArray[2] = row0wt * col1wt;
    sclArray[3] = row1wt * col1wt;

    float gridValue = 0;
    double count = 0;

    // If all the row,col coordinates are value, get the values from the Grid.
    if (rc[1] >= 0 && rc[1] < _geometry.getNumColumns() - 1 + _boundsTolerance && rc[0] >= 0
        && rc[0] < _geometry.getNumRows() - 1 + _boundsTolerance) {
      // Add the value from each cell, excluding any null values.
      for (int m = 0; m < 4; m++) {
        row = rowArray[m];
        col = colArray[m];
        // If the value is not null, weight the contribution of each cell value by its scalar.
        if (!isNull(row, col)) {
          gridValue += getValueAtRowCol(row, col) * sclArray[m];
          count += sclArray[m];
        }
      }
      if (count >= 0.5) {
        gridValue /= count;
      } else {
        gridValue = getNullValue();
      }
    }
    return gridValue;
  }

  /**
   * If the given row/column value falls outside the dimensions of the grid, this 
   * snaps to the nearest index value (0, or dimension length - 1) if it is within 
   * the bounds tolerance 
   * 
   * @param value the exact row/col value (not rounded)
   * @param maxIndexValue the maximum row or col dimension index
   * @return the same value if it is already in range or outside of the bounds tolerance, 
   * 0 if it is negative and within tolerance range, 
   * the index value if it is greater than the dimension and within tolerance range
   */
  private double adjustElementIfWithinTolerance(final double value, final int maxIndexValue) {
    //if value is negative
    if (0 > Double.compare(value, 0)) {
      //check to see that it is within the bounds tolerance
      if (0 < Double.compare(value, 0 - _boundsTolerance)) {
        //snap to nearest value
        return 0.0;
      }
      //if value is greater than the grid row/col dimensions
    } else if (0 < Double.compare(value, maxIndexValue)) {
      //check to see that it is within the bounds tolerance
      if (0 > Double.compare(value, maxIndexValue + _boundsTolerance)) {
        //snap to nearest value
        return maxIndexValue;
      }
    }
    //else don't change the value
    return value;
  }

  /**
   * Gets a subset of the data values in a grid.
   * <p>
   * The starting row and column must be <= the ending row and column (respectively).
   * The returned array is a copy of the data subset and can be modified without affecting the grid.
   * 
   * @param minRow the starting row.
   * @param maxRow the ending row (inclusive).
   * @param minCol the starting column.
   * @param maxCol the ending column (inclusive).
   * @return the subset of the data values.
   */
  public float[][] getValues(final int minRow, final int maxRow, final int minCol, final int maxCol) {
    load();

    // Allocate the subset array.
    int numRows = maxRow - minRow + 1;
    int numCols = maxCol - minCol + 1;
    float[][] result = new float[numRows][numCols];

    // Extract the subset.
    int subsetRow = 0;
    int subsetCol = 0;
    for (int row = minRow; row <= maxRow; row++) {
      for (int col = minCol; col <= maxCol; col++) {
        result[subsetRow][subsetCol] = getValueAtRowCol(row, col);
        subsetCol++;
      }
      subsetRow++;
    }

    return result;
  }

  /**
   * Returns a flag indicating if the value at the specified row,column in the grid is null.
   * 
   * @param row the row to check.
   * @param col the column to check.
   * @return <i>true</i> if the value at the specified row,column is null; otherwise <i>false</i>.
   */
  public boolean isNull(final int row, final int col) {
    load();
    return isNull(getValueAtRowCol(row, col));
  }

  /**
   * Returns a flag indicating if a specified value matches the null value of the grid.
   * 
   * @param value the value to check.
   * @return <i>true</i> if the value matches the null value of the grid; otherwise <i>false</i>.
   */
  public boolean isNull(final float value) {
    load();
    return MathUtil.isEqual(value, _nullValue);
  }

  /**
   * Gets the null value of the grid.
   * 
   * @return the null value of the grid.
   */
  public float getNullValue() {
    load();
    return _nullValue;
  }

  /**
   * Sets the null value of the grid.
   * This recomputes the data range taking into account the new null value. 
   * 
   * @param nullValue the null value to set.
   */
  public void setNullValue(final float nullValue) {
    _nullValue = nullValue;
    if (_dataValues != null) {
      computeMinMax();
    }
    setDirty(true);
  }

  /**
   * Gets the minimum non-null data value in the grid.
   * 
   * @return the minimum non-null data value in the grid.
   */
  public float getMinValue() {
    load();
    return _minValue;
  }

  /**
   * Gets the maximum non-null data value in the grid.
   * 
   * @return the maximum non-null data value in the grid.
   */
  public float getMaxValue() {
    load();
    return _maxValue;
  }

  /**
   * Gets the number of rows in the grid.
   * This is obtained from the grid's geometry.
   * 
   * @return the number of rows in the grid.
   */
  public int getNumRows() {
    load();
    return _geometry.getNumRows();
  }

  /**
   * Gets the number of columns in the grid.
   * This is obtained from the grid's geometry.
   * 
   * @return the number of columns in the grid.
   */
  public int getNumColumns() {
    load();
    return _geometry.getNumColumns();
  }

  /**
   * Gets the corner points of the grid.
   * This is obtained from the grid's geometry.
   * 
   * @return the corner points of the grid.
   */
  public CornerPointsSeries getCornerPoints() {
    load();
    return _geometry.getCornerPoints();
  }

  /**
   * Gets the domain of the data values.
   * 
   * @return the domain of the data values.
   */
  public Domain getDataDomain() {
    load();
    return getDataUnit().getDomain();
  }

  /**
   * Returns a flag indicating if the grid is in the depth domain.
   * This is accomplished by checking the data unit of the grid.
   * 
   * @return <i>true</i> if the grid is in the depth domain; <i>false</i> if not.
   */
  public boolean isDepthGrid() {
    load();
    Unit dataUnit = getDataUnit();
    if (dataUnit != null) {
      return dataUnit.getDomain() == Domain.DISTANCE;
    }
    return false;
  }

  /**
   * Returns a flag indicating if the grid is in the time domain.
   * This is accomplished by checking the data unit of the grid.
   * 
   * @return <i>true</i> if the grid is in the time domain; <i>false</i> if not.
   */
  public boolean isTimeGrid() {
    load();
    Unit dataUnit = getDataUnit();
    if (dataUnit != null) {
      return dataUnit.getDomain() == Domain.TIME;
    }
    return false;
  }

  /**
   * Unloads the data values of the grid and marks it as being a "ghost".
   * This will trigger a reload the next time info is requested. This can
   * be used to save memory. This could be a temporary measure, depending
   * on whether or not grid data will be stored in the entity or always
   * read from the datastore.
   */
  public void unload() {
    markGhost();
    _dataValues = null;
  }

  /**
   * Set the bounds tolerance; that is, the range of (index) tolerance if the
   * row/col values are just outside the grid bounds for interpolation.
   * @param boundsTolerance bounds tolerance
   */
  public void setBoundsTolerance(final double boundsTolerance) {
    _boundsTolerance = boundsTolerance;
  }

  /**
   * Get the bounds tolerance; that is, the range of (index) tolerance if the
   * row/col values are just outside the grid bounds for interpolation.
   * @return bounds tolerance
   */
  public double getBoundsTolerance() {
    return _boundsTolerance;
  }

  /**
   * Validates a 2D array of candidate values against the row and column dimensions of the grid geometry.
   * 
   * @param values the array to check.
   */
  private void validateArraySizes(final float[][] values) {
    int numRows = _geometry.getNumRows();
    if (values.length != numRows) {
      throw new IllegalArgumentException("Invalid array size. The 1st dimension must equal # of rows: " + numRows);
    }
    for (int i = 0; i < numRows; i++) {
      validateArraySize(values[i]);
    }
  }

  /**
   * Validates a 1D array of candidate values against the column dimension of the grid geometry.
   * 
   * @param values the array to check.
   */
  private void validateArraySize(final float[] values) {
    int numCols = _geometry.getNumColumns();
    if (values.length != numCols) {
      throw new IllegalArgumentException("Invalid array size. The 2nd dimension must equal # of columns: " + numCols);
    }
  }

}
