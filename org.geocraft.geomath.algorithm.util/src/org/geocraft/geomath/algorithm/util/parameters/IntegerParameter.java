/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.geomath.algorithm.util.parameters;


public class IntegerParameter implements IParameter {

  /** The unique ID of the parameter. */
  private final String _uniqueID;

  /** The name of the parameter. */
  private final String _name;

  int _value;

  /** The value object. */
  protected Object _valueObject;

  public IntegerParameter(final String uniqueId, final String name, final int initValue) {
    _uniqueID = uniqueId;
    _name = name;
    setValue(initValue);
  }

  public String getValueAsString() {
    return _valueObject.toString();
  }

  public int getValue() {
    return _value;
  }

  public void setValue(final int value) {
    setValueObject(new Integer(value));
  }

  public void setValueObject(final Object valueObject) {
    _valueObject = valueObject;
    try {
      _value = Integer.parseInt(_valueObject.toString());
    } catch (NumberFormatException e) {
      _value = 0;
    }
  }

  public Object getName() {
    return _name;
  }

  public Object getValueObject() {
    return _valueObject;
  }

  public String getUniqueID() {
    return _uniqueID;
  }

}
