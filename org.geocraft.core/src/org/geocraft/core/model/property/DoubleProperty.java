/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.property;


/**
 * A property for containing a double value.
 */
public class DoubleProperty extends NumericProperty {

  /** The double value. */
  private double _value;

  /**
   * Constructs a double property with an initial value.
   * 
   * @param key the property key.
   * @param value the initial property value.
   */
  public DoubleProperty(String key, double value) {
    super(key);
    _value = value;
  }

  /**
   * Gets the double value.
   * 
   * @return the double value.
   */
  public double get() {
    return _value;
  }

  /**
   * Sets the double value.
   * 
   * @param value the double value to set.
   */
  public void set(double value) {
    firePropertyChange(_value, _value = value);
  }

  @Override
  public Object getValueObject() {
    return Double.toString(_value);
  }

  @Override
  public void setValueObject(Object valueObject) {
    try {
      set(Double.parseDouble(valueObject.toString()));
    } catch (Exception e) {
      throw new IllegalArgumentException("Not a double: " + valueObject);
    }
  }

  @Override
  public String toString() {
    return "" + get();
  }
}
