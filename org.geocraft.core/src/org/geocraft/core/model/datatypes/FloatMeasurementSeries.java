/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.core.model.datatypes;


import org.geocraft.core.common.math.MathUtil;
import org.geocraft.core.model.base.ValueObject;


/**
 * A series of floating point values and their single unit of measurement.
 */
public final class FloatMeasurementSeries extends ValueObject {

  /** The units of the measurements. */
  private final Unit _unit;

  /** The data values. */
  private final float[] _values;

  /** The minimum non-null data value. */
  private FloatMeasurement _minDataValue;

  /** The maximum non-null data value. */
  private FloatMeasurement _maxDataValue;

  /** A value used to indicate invalid - null data. */
  private float _nullValue = Float.POSITIVE_INFINITY;

  /**
   * The constructor.
   * @param vals the array of float values.
   * @param unit for all of the float values.
   */
  public FloatMeasurementSeries(final float[] vals, final Unit unit) {
    super("float measuement series");
    _unit = unit;
    _values = vals;
  }

  /**
   * The constructor.
   * @param vals the array of float values.
   * @param unit for all of the float values.
   */
  public FloatMeasurementSeries(final float[] vals, final Unit unit, final float nullValue) {
    super("");
    _unit = unit;
    _values = vals;
    _nullValue = nullValue;
  }

  /**
   * @return the unit object
   */
  public Unit getUnit() {
    return _unit;
  }

  /**
   * @return the value
   */
  public float[] getValues() {
    float[] vals = new float[_values.length];
    System.arraycopy(_values, 0, vals, 0, _values.length);
    return vals;
  }

  /**
   * Get the value at the specified index.
   */
  public float getValue(final int index) {
    return _values[index];
  }

  /**
   * Hmmm - should we allow people to change the values?
   * 
   * @param index
   * @param value
   */
  public void setValue(final int index, final float value) {
    _values[index] = value;
  }

  /**
   * Is the n'th data point in this series null?
   *
   * @param index
   * @return true if it is null.
   */
  public boolean isNull(final int index) {
    return MathUtil.isEqual(_values[index], _nullValue);
  }

  /**
   * Get the measurement at the specified index.
   */
  public FloatMeasurement getMeasurement(final int index) {
    return new FloatMeasurement(getValue(index), _unit);
  }

  /**
   * The number of data points in the series.
   *
   * @return length of the series.
   */
  public int getNumPoints() {
    return _values.length;
  }

  public FloatMeasurement getMinValue() {
    if (_minDataValue == null) {
      updateDataRange();
    }

    return _minDataValue;
  }

  public FloatMeasurement getMaxValue() {
    if (_maxDataValue == null) {
      updateDataRange();
    }

    return _maxDataValue;
  }

  /**
   * The value that signifies a null data point.
   * @return the null value.
   */
  public float getNullValue() {
    return _nullValue;
  }

  /**
   * The value that signifies a null data point.
   */
  public void setNullValue(final float nullValue) {
    _nullValue = nullValue;
  }

  @Override
  public String toString() {
    String result = "No data";
    if (_values.length > 0) {
      result = "First: " + _values[0] + " Last: " + _values[_values.length - 1] + " Length: " + _values.length;
    }
    return result + " Units: " + _unit;
  }

  /**
   * Compute the min and max values in this series.
   *
   * If there are no points or they are all null sets min and max
   * to 0.
   */

  private void updateDataRange() {

    float[] range = MathUtil.computeRange(getValues(), _nullValue);

    _minDataValue = new FloatMeasurement(range[0], _unit);
    _maxDataValue = new FloatMeasurement(range[1], _unit);

  }

  @Override
  public Object[][] getDisplayableProperties() {
    Object[][] result = new Object[_values.length + 1][2];
    for (int i = 0; i < _values.length; i++) {
      result[i][0] = i;
      result[i][1] = _values[1];
    }

    return result;
  }

  public Object getDescription() {
    float[] fValues = getValues();
    Float[] values = new Float[fValues.length];
    for (int i = 0; i < fValues.length; i++) {
      values[i] = fValues[i];
    }
    return values;
  }

}
