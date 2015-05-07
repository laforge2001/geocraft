/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.geomath.algorithm.util.parameters;


public class FloatParameter implements IParameter {

  /** The unique ID of the parameter. */
  private final String _uniqueID;

  /** The name of the parameter. */
  private final String _name;

  float _value;

  /** The value object. */
  protected Object _valueObject;

  public FloatParameter(final String uniqueId, final String name, final float initValue) {
    _uniqueID = uniqueId;
    _name = name;
    setValue(initValue);
  }

  public String getValueAsString() {
    return _valueObject.toString();
  }

  public float getValue() {
    return _value;
  }

  public void setValue(final float value) {
    setValueObject(new Float(value));
  }

  public void setValueObject(final Object valueObject) {
    _valueObject = valueObject;
    try {
      _value = Float.parseFloat(_valueObject.toString());
    } catch (NumberFormatException e) {
      _value = Float.NaN;
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
