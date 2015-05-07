/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.property;


/**
 * A property for containing a boolean value.
 */
public class BooleanProperty extends Property {

  /** The boolean value. */
  private boolean _value;

  /**
   * Constructs a boolean property with an initial value.
   * 
   * @param key the property key.
   * @param value the initial property value.
   */
  public BooleanProperty(String key, boolean value) {
    super(key);
    _value = value;
  }

  /**
   * Gets the boolean value.
   * 
   * @return the boolean value.
   */
  public boolean get() {
    return _value;
  }

  /**
   * Sets the boolean value.
   * 
   * @param value the boolean value to set.
   */
  public void set(boolean value) {
    firePropertyChange(_value, _value = value);
  }

  @Override
  public Object getValueObject() {
    return Boolean.toString(_value);
  }

  @Override
  public void setValueObject(Object valueObject) {
    try {
      set(Boolean.parseBoolean(valueObject.toString()));
    } catch (Exception e) {
      throw new IllegalArgumentException("Not a boolean: " + valueObject + " " + valueObject.getClass());
    }
  }

  @Override
  public String toString() {
    return "" + get();
  }

  @Override
  public String pickle() {
    // For boolean properties, simply use the toString() representation of the value object.
    return getValueObject().toString();
  }

  @Override
  public void unpickle(String value) {
    setValueObject(value);
  }
}
