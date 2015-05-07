/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.core.model.datatypes;


import org.geocraft.core.common.util.HashCode;
import org.geocraft.core.model.base.ValueObject;


/**
 * Immutable class encapsulating a float value and the unit of measurement of the value.
 */
public final class FloatMeasurement extends ValueObject {

  /** The unit of the measurement. */
  private final Unit _unit;

  /** The value of the measurement. */
  private final float _value;

  /**
   * Constructor.
   * 
   * @param value of the measurement.
   * @param unit of the measurement.
   */
  public FloatMeasurement(final float value, final Unit unit) {
    super("");
    if (unit == null) {
      throw new IllegalArgumentException("You can't provide a null unit object to the FloatMeasurement constructor");
    }
    _unit = unit;
    _value = value;
  }

  /**
   * @return the unit acronym
   */
  public Unit getUnit() {
    return _unit;
  }

  /**
   * @return the value
   */
  public float getValue() {
    return _value;
  }

  @Override
  public String toString() {
    return _value + " " + _unit;
  }

  /**
   * Test if two FloatMeasurement objects are precisely identical.
   */
  @Override
  public boolean equals(final Object obj) {
    if (obj != null && obj instanceof FloatMeasurement) {
      FloatMeasurement tmp = (FloatMeasurement) obj;
      if (tmp._unit.equals(_unit) && tmp._value == _value) {
        return true;
      }
    }
    return false;
  }

  /**
   * Overrode the equals() method so must also change hashCode().
   */
  @Override
  public int hashCode() {
    HashCode hash = new HashCode();
    hash.add(_value);
    hash.add(_unit);
    return hash.getHashCode();
  }
}
