/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model;


import java.beans.PropertyChangeListener;
import java.util.Map;

import org.geocraft.core.model.property.Property;
import org.geocraft.core.model.validation.IValidation;


/**
 * Defines the interface for a property model.
 */
public interface IModel extends PropertyChangeListener {

  /**
   * Validates the current status of the model.
   * 
   * @param results the object in which to put the validation results.
   */
  void validate(IValidation results);

  /**
   * Adds a listener to the model. The listener will be notified when
   * any of the properties in the model are changed.
   * 
   * @param listener the listener to add.
   */
  void addListener(IModelListener listener);

  /**
   * Removed a listener to the model. The listener will no longer be notified when
   * any of the properties in the model are changed.
   * 
   * @param listener the listener to remove.
   */
  void removeListener(IModelListener listener);

  /**
   * "Pickles" the model parameters for storage to a preferences file.
   * 
   * @return the "pickled" map of parameter key-value pairs.
   */
  Map<String, String> pickle();

  /**
   * "Unpickles" the model parameters retrieved from a preferences file.
   * 
   * @param parms the "pickled" map of parameter key-value pairs.
   */
  void unpickle(final Map<String, String> parms);

  /**
   * Gets the value of the model property with the specified key.
   * 
   * @param key the property key.
   * @return the value of the specified property, as an object.
   */
  Object getValueObject(String key);

  /**
   * Sets the value of the model property with the specified key.
   * 
   * @param key the property key.
   * @param valueObject the object representing the value to set.
   */
  void setValueObject(String key, Object valueObject);

  Property getProperty(String key);

  String[] getPropertyKeys();

  /**
   * Updates the values of the model properties with those from the specified model.
   * 
   * @param model the model from which to update the model properties.
   */
  void updateFrom(IModel model);
}
