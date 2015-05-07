/*
 * Copyright (C) ConocoPhillips 2009 All Rights Reserved. 
 */
package org.geocraft.core.model.base;


/**
 * Property descriptor for a property. Differs from an Eclipse property descriptor in that
 * it also carries the value of the property.
 *
 */
public class PropertyDescriptor extends org.eclipse.ui.views.properties.PropertyDescriptor {

  private final Object _value;

  public Object getValue() {
    return _value;
  }

  public PropertyDescriptor(final Object id, final String displayName, final Object value) {
    super(id, displayName);
    this._value = value;
  }
}
