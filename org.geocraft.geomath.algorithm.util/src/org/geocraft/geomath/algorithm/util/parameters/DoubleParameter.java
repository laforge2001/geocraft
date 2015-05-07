/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.geomath.algorithm.util.parameters;


public class DoubleParameter implements IParameter {

  /** The unique ID of the parameter. */
  private final String _uniqueID;

  /** The name of the parameter. */
  private final String _name;

  double _value;

  /** The value object. */
  protected Object _valueObject;

  public DoubleParameter(final String uniqueId, final String name, final double initValue) {
    _uniqueID = uniqueId;
    _name = name;
    setValue(initValue);
  }

  public String getValueAsString() {
    return _valueObject.toString();
  }

  public double getValue() {
    return _value;
  }

  public void setValue(final double value) {
    setValueObject(new Double(value));
  }

  public void setValueObject(final Object valueObject) {
    _valueObject = valueObject;
    try {
      _value = Double.parseDouble(_valueObject.toString());
    } catch (NumberFormatException e) {
      _value = Double.NaN;
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
