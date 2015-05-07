/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.property;


public class ObjectProperty<T> extends Property {

  private T _value;

  private final Class<T> _klass;

  public ObjectProperty(final String key, final Class<T> klass) {
    super(key);
    _value = null;
    _klass = klass;
  }

  public T get() {
    return _value;
  }

  public void set(final T value) {
    firePropertyChange(_value, _value = value);
  }

  public boolean isNull() {
    return _value == null;
  }

  @Override
  public Object getValueObject() {
    return _value;
  }

  @Override
  public void setValueObject(final Object value) {
    if (value == null) {
      set((T) null);
      return;
    }
    if (_klass.isAssignableFrom(value.getClass())) {
      set(_klass.cast(value));
      return;
    }
  }

  @Override
  public String toString() {
    return "" + get();
  }

  protected Class<T> getKlazz() {
    return _klass;
  }

  @Override
  public String pickle() {
    return getValueObject().toString();
  }

  @Override
  public void unpickle(final String value) {
    // TODO: How to implement this?
  }
}
