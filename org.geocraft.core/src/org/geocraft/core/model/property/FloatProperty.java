/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.property;


/**
 * A property for containing a float value.
 */
public class FloatProperty extends NumericProperty {

  /** The float value. */
  private float _value;

  /**
   * Constructs a float property with an initial value.
   * 
   * @param key the property key.
   * @param value the initial property value.
   */
  public FloatProperty(String key, float value) {
    super(key);
    _value = value;
  }

  /**
   * Gets the float value.
   * 
   * @return the float value.
   */
  public float get() {
    return _value;
  }

  /**
   * Sets the float value.
   * 
   * @param value the float value to set.
   */
  public void set(float value) {
    firePropertyChange(_value, _value = value);
  }

  @Override
  public Object getValueObject() {
    return Float.toString(_value);
  }

  @Override
  public void setValueObject(Object valueObject) {
    try {
      set(Float.parseFloat(valueObject.toString()));
    } catch (Exception e) {
      throw new IllegalArgumentException("Not a float: " + valueObject);
    }
  }

  @Override
  public String toString() {
    return "" + get();
  }

}
