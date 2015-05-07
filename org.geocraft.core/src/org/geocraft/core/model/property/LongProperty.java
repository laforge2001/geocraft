/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.property;


/**
 * A property for containing a long value.
 */
public class LongProperty extends NumericProperty {

  /** The long value. */
  private long _value;

  /**
   * Constructs a long property with an initial value.
   * 
   * @param key the property key.
   * @param value the initial property value.
   */
  public LongProperty(String key, long value) {
    super(key);
    _value = value;
  }

  /**
   * Gets the long value.
   * 
   * @return the long value.
   */
  public long get() {
    return _value;
  }

  /**
   * Sets the long value.
   * 
   * @param value the long value to set.
   */
  public void set(long value) {
    firePropertyChange(_value, _value = value);
  }

  @Override
  public Object getValueObject() {
    return Long.toString(_value);
  }

  @Override
  public void setValueObject(Object valueObject) {
    try {
      set(Long.parseLong(valueObject.toString()));
    } catch (Exception e) {
      throw new IllegalArgumentException("Not an long: " + valueObject);
    }
  }

  @Override
  public String toString() {
    return "" + get();
  }
}
