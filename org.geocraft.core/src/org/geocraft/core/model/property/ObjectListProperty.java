/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.property;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public abstract class ObjectListProperty<T> extends Property {

  private List<T> _values;

  /** The class of the objects. */
  private Class<T> _klass;

  /**
   * @param key
   */
  public ObjectListProperty(String key, Class<T> klazz) {
    super(key);
    _values = new ArrayList<T>();
    _klass = klazz;
  }

  /**
   * Constructs a object property.
   * 
   * @param key the property key.
   */
  public ObjectListProperty(String key, Class<T> klass, List<T> values) {
    super(key);
    _values = values;
    _klass = klass;
  }

  /**
   * Constructs a object property.
   * 
   * @param key the property key.
   */
  public ObjectListProperty(String key, Class<T> klass, T[] values) {
    super(key);
    _values = Arrays.asList(values);
    _klass = klass;
  }

  /**
   * Gets the list of object values.
   * 
   * @return the array of object values.
   */
  public List<T> get() {
    return _values;
  }

  /**
   * Sets the array of object values.
   * 
   * @param values the array of object values to set.
   */
  public void set(List<T> values) {
    firePropertyChange(_values, _values = values);
  }

  /**
   * Checks if the array of object values is empty.
   * 
   * @return <i>true</i> if the array is empty or <i>null</i>; otherwise <i>false</i>.
   */
  public boolean isEmpty() {
    return _values == null || _values.size() == 0;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.property.Property#getValueObject()
   */
  @Override
  public Object getValueObject() {
    return _values;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.property.Property#setValueObject(java.lang.Object)
   */
  @Override
  public void setValueObject(Object valueObject) {
    if (valueObject == null) {
      List<T> list = new ArrayList<T>();
      set(list);
      return;
    }
    if (valueObject instanceof List) {
      List objects = (List) valueObject;
      List<T> array = new ArrayList<T>();
      for (Object object : objects) {
        array.add(_klass.cast(object));
      }
      set(array);
      return;
    }
  }

  public Class<T> getKlazz() {
    return _klass;
  }

  private List<T> copyValues() {
    List<T> vals = new ArrayList<T>();
    vals.addAll(_values);
    return vals;
  }

  public void add(T value) {
    List<T> vals = copyValues();
    vals.add(value);
    set(vals);
  }

  public void remove(T value) {
    List<T> vals = copyValues();
    vals.remove(value);
    set(vals);
  }

}
