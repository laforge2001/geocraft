/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.property;


/**
 * A property for containing a integer value.
 */
public class IntegerProperty extends NumericProperty {

  /** The integer value. */
  private int _value;

  /**
   * Constructs a integer property with an initial value.
   * 
   * @param key the property key.
   * @param value the initial property value.
   */
  public IntegerProperty(String key, int value) {
    super(key);
    _value = value;
  }

  /**
   * Gets the integer value.
   * 
   * @return the integer value.
   */
  public int get() {
    return _value;
  }

  /**
   * Sets the integer value.
   * 
   * @param value the integer value to set.
   */
  public void set(int value) {
    firePropertyChange(_value, _value = value);
  }

  @Override
  public Object getValueObject() {
    return Integer.toString(_value);
  }

  @Override
  public void setValueObject(Object valueObject) {
    try {
      set(Integer.parseInt(valueObject.toString()));
    } catch (Exception e) {
      throw new IllegalArgumentException("Not an integer: " + valueObject);
    }
  }

  @Override
  public String toString() {
    return "" + get();
  }
}
