/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */

package org.geocraft.geomath.algorithm.util.parameters;


import org.geocraft.core.model.datatypes.Domain;


/**
 * A simple Domain parameter.
 */
public class DomainParameter implements IParameter {

  /** The unique ID of the parameter. */
  private final String _uniqueID;

  /** The name of the parameter. */
  private final String _name;

  /** The current value. */
  private Domain _value;

  /** The value object. */
  protected Object _valueObject;

  public DomainParameter(final String uniqueId, final String name, final Domain initValue) {
    _uniqueID = uniqueId;
    _name = name;
    setValue(initValue);
  }

  public String getValueAsString() {
    return _valueObject.toString();
  }

  public void setValueObject(final Object valueObject) {
    _valueObject = valueObject;
    _value = null;
    if (_valueObject.toString().equals(Domain.TIME.getTitle())) {
      _value = Domain.TIME;
    } else if (_valueObject.toString().equals(Domain.DISTANCE.getTitle())) {
      _value = Domain.DISTANCE;
    }
  }

  public Domain getValue() {
    return _value;
  }

  public void setValue(final Domain value) {
    setValueObject(value);
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
