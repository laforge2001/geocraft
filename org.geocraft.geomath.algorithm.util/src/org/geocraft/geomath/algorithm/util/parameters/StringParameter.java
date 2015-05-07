/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.geomath.algorithm.util.parameters;


public class StringParameter implements IParameter {

  /** The unique ID of the parameter. */
  private final String _uniqueID;

  /** The name of the parameter. */
  private final String _name;

  /** The current value. */
  private String _value;

  /** The multi-line string flag. */
  private boolean _isMultiLine = false;

  private final String[] _definedValues;

  /** The value object. */
  protected Object _valueObject;

  public StringParameter(final String uniqueId, final String name, final String initValue, final boolean isMultiLine) {
    _uniqueID = uniqueId;
    _name = name;
    setValue(initValue);
    _isMultiLine = isMultiLine;
    _definedValues = new String[0];
  }

  public StringParameter(final String uniqueId, final String name, final String initValue) {
    _uniqueID = uniqueId;
    _name = name;
    setValue(initValue);
    _isMultiLine = false;
    _definedValues = new String[0];
  }

  public StringParameter(final String uniqueId, final String name, final String initValue, final String[] definedValues) {
    _uniqueID = uniqueId;
    _name = name;
    setValue(initValue);
    _definedValues = new String[definedValues.length];
    System.arraycopy(definedValues, 0, _definedValues, 0, definedValues.length);
  }

  public String getValueAsString() {
    return _value;
  }

  public boolean isMultiLineString() {
    return _isMultiLine;
  }

  public String getValue() {
    return _value;
  }

  public void setValue(final String value) {
    setValueObject(value);
  }

  public void setValueObject(final Object valueObject) {
    _valueObject = valueObject;
    _value = valueObject.toString();
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
