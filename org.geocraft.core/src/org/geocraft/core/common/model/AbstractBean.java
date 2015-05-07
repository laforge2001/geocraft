/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.model;


import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


public abstract class AbstractBean {

  protected final PropertyChangeSupport _propertyChangeSupport;

  public AbstractBean() {
    _propertyChangeSupport = new PropertyChangeSupport(this);
  }

  public void addPropertyChangeListener(final PropertyChangeListener listener) {
    _propertyChangeSupport.addPropertyChangeListener(listener);
  }

  public void addPropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
    _propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
  }

  public void removePropertyChangeListener(final PropertyChangeListener listener) {
    _propertyChangeSupport.removePropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
    _propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
  }

  protected void firePropertyChange(final String propertyName, final Object oldValue, final Object newValue) {
    _propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
  }
}
