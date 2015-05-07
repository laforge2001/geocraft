/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.property;


import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


/**
 * The abstract base class for all model properties.
 * All properties contain an immutable key that must be unique within a model.
 * Sub-classes must implement the <code>getValueObject</code> and <code>setValueObject</code>
 * methods, which must contain the necessary code to convert between an object and the
 * specific type of value contained in the property.
 */
public abstract class Property {

  /** The key of the property (must be unique within a model). */
  private final String _key;

  private final PropertyChangeSupport _propertyChangeSupport;

  /**
   * The default constructor.
   * @param key the property key.
   */
  public Property(final String key) {
    _propertyChangeSupport = new PropertyChangeSupport(this);
    _key = key;
  }

  /**
   * Returns the property key.
   */
  public String getKey() {
    return _key;
  }

  /**
   * Gets the property value (as an object).
   * 
   * @return the object representation of the property value.
   */
  public abstract Object getValueObject();

  /**
   * Sets the property value (using an object representation).
   * 
   * @param valueObject the object representation of the property value to set.
   */
  public abstract void setValueObject(Object valueObject);

  /**
   * Adds a listener to the property.
   * The listener will be notified when the property value has changed.
   * 
   * @param listener the listener to add.
   */
  public void addPropertyChangeListener(final PropertyChangeListener listener) {
    _propertyChangeSupport.addPropertyChangeListener(listener);
  }

  /**
   * Removes a listener to the property.
   * The listener will no longer be notified when the property value has changed.
   * 
   * @param listener the listener to remove.
   */
  public void removePropertyChangeListener(final PropertyChangeListener listener) {
    _propertyChangeSupport.removePropertyChangeListener(listener);
  }

  /**
   * Notifies listeners that the property value has changed.
   * 
   * @param oldValue the old property value.
   * @param newValue the new property value.
   */
  protected void firePropertyChange(final Object oldValue, final Object newValue) {
    firePropertyChange(_key, oldValue, newValue);
  }

  /**
   * Notifies listeners that the property value has changed.
   * 
   * @param oldValue the old property value.
   * @param newValue the new property value.
   */
  protected void firePropertyChange(final String key, final Object oldValue, final Object newValue) {
    if (oldValue == null && newValue == null) {
      return;
    }
    _propertyChangeSupport.firePropertyChange(key, oldValue, newValue);
  }

  /**
   * Packs the property values into a string representation.
   * This is used by the preferences system to store models.
   * 
   * @return the "pickled" value.
   */
  public abstract String pickle();

  /**
   * Unpacks a property value from a string representation.
   * This is used by the preferences system to restore models.
   * 
   * @param value the "pickled" value.
   */
  public abstract void unpickle(String value);

  /**
   * Disposes of any resources associated with the property.
   */
  public void dispose() {
    // Does nothing by default, but certain sub-classes will needed to implement custom logic.
  }
}
