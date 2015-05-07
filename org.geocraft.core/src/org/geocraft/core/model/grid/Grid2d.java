/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.grid;


import org.geocraft.core.common.math.MathUtil;
import org.geocraft.core.model.GeologicInterpretation;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.geometry.GridGeometry2d;
import org.geocraft.core.model.geometry.LineGeometry;
import org.geocraft.core.model.mapper.IGrid2dMapper;


public class Grid2d extends GeologicInterpretation {

  /** The geometry (lines) onto which the grid bins are mapped. */
  private final GridGeometry2d _gridGeometry;

  /** The unit of measurement for the grid data values. */
  private Unit _dataUnit;

  /** The value representing null in the grid. */
  private float _nullValue;

  /** The minimum (non-null) value in the grid. */
  private float _minValue;

  /** The maximum (non-null) value in the grid. */
  private float _maxValue;

  /**
   * Constructs a <code>Grid2d</code> entity.
   * 
   * @param name the grid name.
   * @param mapper the grid mapper to the underlying datastore.
   * @param gridGeometry the geometry on which the grid is defined.
   */
  public Grid2d(final String name, final IGrid2dMapper mapper, final GridGeometry2d gridGeometry) {
    super(name, mapper);
    _gridGeometry = gridGeometry;
  }

  /**
   * Gets the geometry on which the grid is defined.
   * 
   * @return the grid geometry.
   */
  public GridGeometry2d getGridGeometry() {
    return _gridGeometry;
  }

  /**
   * Gets the # of lines in the grid.
   * This is the same as the # of lines in the grid geometry.
   * 
   * @return the number of lines.
   */
  public int getNumLines() {
    return _gridGeometry.getNumLines();
  }

  /**
   * Gets the unit of measurement for the grid data values.
   * 
   * @return the data unit of measurement.
   */
  public Unit getDataUnit() {
    load();
    return _dataUnit;
  }

  /**
   * Sets the unit of measurement for the grid data values.
   * This will optionally set the domain as well, if the unit
   * of measurement represents time or depth (length).
   * 
   * @param dataUnit the data unit of measurement.
   */
  public void setDataUnit(final Unit dataUnit) {
    _dataUnit = dataUnit;
    //setZDomain(dataUnit.getDomain());
    setDirty(true);
  }

  /**
   * Gets the data values from the grid for the given line.
   * 
   * @param lineNumber the line number.
   * @return the grid values.
   */
  public float[] getValues(final int lineNumber) {
    load();
    IGrid2dMapper mapper = (IGrid2dMapper) getMapper();
    return mapper.getValues(this, lineNumber);
  }

  /**
   * Puts the given data values into the grid for the given line.
   * 
   * @param lineNumber the line number.
   * @param values the grid values.
   */
  public void putValues(final int lineNumber, final float[] values) {
    validateArraySize(lineNumber, values);
    IGrid2dMapper mapper = (IGrid2dMapper) getMapper();
    mapper.putValues(this, lineNumber, values);
    setDirty(true);
  }

  /**
   * Gets the minimum (non-null) value of the grid.
   * 
   * @return the minimum value.
   */
  public float getMinValue() {
    load();
    return _minValue;
  }

  /**
   * Gets the maximum (non-null) value of the grid.
   * 
   * @return the maximum value.
   */
  public float getMaxValue() {
    load();
    return _maxValue;
  }

  /**
   * Sets the minimum and maximum (non-null) values for the grid.
   * 
   * @param minValue the minimum value to set.
   * @param maxValue the maximum value to set.
   */
  public void setMinMaxValues(final float minValue, final float maxValue) {
    _minValue = minValue;
    _maxValue = maxValue;
    setDirty(true);
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
   * Gets the value representing a null in the grid.
   * 
   * @return the null value.
   */
  public float getNullValue() {
    load();
    return _nullValue;
  }

  /**
   * Sets the null value of the grid and recomputes the data
   * range taking into account the new null value. 
   * 
   * @param nullValue the null value to set.
   */
  public void setNullValue(final float nullValue) {
    _nullValue = nullValue;
    setDirty(true);
  }

  /**
   * Returns the domain of the grid data values.
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
   * Validates the size of the data array for the given line.
   * If the length of the array must match the number of bins in the line,
   *
   * @param lineNumber the line number.
   * @param values the grid values.
   */
  private void validateArraySize(final int lineNumber, final float[] values) {
    LineGeometry lineGeometry = _gridGeometry.getLineByNumber(lineNumber);
    int numBins = lineGeometry.getNumBins();
    if (values == null || values.length != numBins) {
      throw new IllegalArgumentException("Invalid # of values. The array must match the number of bins in the line.");
    }
  }
}
