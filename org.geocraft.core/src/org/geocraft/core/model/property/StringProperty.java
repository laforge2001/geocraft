/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.property;


/**
 * A property for containing a string value.
 */
public class StringProperty extends Property {

  /** The string value. */
  private String _value;

  /**
   * Constructs a string property.
   * 
   * @param key the property key.
   */
  public StringProperty(final String key) {
    this(key, "");
  }

  /**
   * Constructs a string property with an initial value.
   * 
   * @param key the property key.
   * @param value the initial property value.
   */
  public StringProperty(final String key, final String value) {
    super(key);
    _value = value;
    if (_value == null) {
      _value = "";
    }
  }

  /**
   * Gets the string value.
   * 
   * @return the string value.
   */
  public String get() {
    return _value;
  }

  /**
   * Sets the string value.
   * 
   * @param value the string value to set.
   */
  public void set(final String value) {
    String newValue = value;
    // If the new string value is null, change it to an empty string.
    if (value == null) {
      newValue = "";
    }
    firePropertyChange(_value, _value = newValue);
  }

  /**
   * Concatenate the current value with a string
   * <p>
   * Note: This is needed when adding text to a TextBox whose associated
   *       property is a String.
   * @param value The string to append.
   */
  public void cat(final String value) {
    if (value == null) {
      return;
    }
    String newValue = value;
    firePropertyChange(_value, _value += newValue);
  }

  @Override
  public Object getValueObject() {
    return _value;
  }

  @Override
  public void setValueObject(final Object valueObject) {
    try {
      set(valueObject.toString());
    } catch (Exception e) {
      throw new IllegalArgumentException("Not a string: " + valueObject);
    }
  }

  /**
   * Checks if the string value is empty.
   * @return <i>true</i> if the string value is empty; <i>false</i> if not.
   */
  public boolean isEmpty() {
    return _value.length() == 0;
  }

  @Override
  public String toString() {
    return get();
  }

  @Override
  public String pickle() {
    return getValueObject().toString();
  }

  @Override
  public void unpickle(final String value) {
    setValueObject(value);
  }
}
