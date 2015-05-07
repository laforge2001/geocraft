/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.property;


import java.lang.reflect.Array;


/**
 * A property for containing an array of object values.
 */
public abstract class ObjectArrayProperty<T> extends Property {

  /** The array of object values. */
  private T[] _values;

  /** The class of the objects. */
  private Class<T> _klass;

  /**
   * Constructs a object property.
   * 
   * @param key the property key.
   */
  public ObjectArrayProperty(String key, Class<T> klass) {
    super(key);
    _values = (T[]) Array.newInstance(klass, 0);
    _klass = klass;
  }

  /**
   * Constructs a object property.
   * 
   * @param key the property key.
   */
  public ObjectArrayProperty(String key, Class<T> klass, T[] values) {
    super(key);
    _values = values;
    _klass = klass;
  }

  /**
   * Gets the array of object values.
   * 
   * @return the array of object values.
   */
  public T[] get() {
    return _values;
  }

  /**
   * Sets the array of object values.
   * 
   * @param values the array of object values to set.
   */
  public void set(T[] values) {
    firePropertyChange(_values, _values = values);
  }

  /**
   * Checks if the array of object values is empty.
   * 
   * @return <i>true</i> if the array is empty or <i>null</i>; otherwise <i>false</i>.
   */
  public boolean isEmpty() {
    return _values == null || _values.length == 0;
  }

  @Override
  public Object getValueObject() {
    return _values;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setValueObject(Object valueObject) {
    if (valueObject == null) {
      T[] array = (T[]) Array.newInstance(_klass, 0);
      set(array);
      return;
    }
    if (valueObject.getClass().isArray()) {
      Object[] objects = (Object[]) valueObject;
      T[] array = (T[]) Array.newInstance(_klass, objects.length);
      for (int i = 0; i < objects.length; i++) {
        array[i] = _klass.cast(objects[i]);
      }
      set(array);
      return;
    }
  }

  public Class<T> getKlazz() {
    return _klass;
  }
}
