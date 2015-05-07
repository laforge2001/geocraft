/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.validation;


import org.eclipse.core.runtime.IStatus;
import org.geocraft.core.model.property.Property;


/**
 * The interface for model validation.
 */
public interface IValidation {

  /**
   * Sets a status in the validation.
   * The status can be an error, warning or informational one.
   * 
   * @param property the property with which the status is associated.
   * @param status the status to set.
   */
  void setStatus(final Property property, final IStatus status);

  /**
   * Sets an error status in the validation.
   * An error is used to indicate an state that should prevent
   * the continuation or execution. An example of this would be
   * informing the user that an value is illegal or undefined.
   * 
   * @param property the property with which the error is associated.
   * @param message the error message.
   */
  void error(final Property property, final String message);

  /**
   * Sets an warning status in the validation.
   * A warning is used to indicate a state that may or may need to prevent
   * continuation or execution. An example of this would be informing the
   * user that an output file exists and will be overwritten.
   * 
   * @param property the property with which the warning is associated.
   * @param message the warning message.
   */
  void warning(final Property property, final String message);

  /**
   * Sets an info status in the validation.
   * An info is used to indicate a state that converys information but does not
   * prevent continuation or execution.
   * 
   * @param property the property with which the info is associated.
   * @param message the info message.
   */
  void info(final Property property, final String message);

  /**
   * Sets a status in the validation.
   * The status can be an error, warning or informational one.
   * 
   * @param key the key of the property with which the status is associated.
   * @param status the status to set.
   */
  void setStatus(final String key, final IStatus status);

  /**
   * Sets an error status in the validation.
   * An error is used to indicate an state that should prevent
   * the continuation or execution. An example of this would be
   * informing the user that an value is illegal or undefined.
   * 
   * @param key the key of the property with which the error is associated.
   * @param message the error message.
   */
  void error(final String key, final String message);

  /**
   * Sets an warning status in the validation.
   * A warning is used to indicate a state that may or may need to prevent
   * continuation or execution. An example of this would be informing the
   * user that an output file exists and will be overwritten.
   * 
   * @param key the key of the property with which the warning is associated.
   * @param message the warning message.
   */
  void warning(final String key, final String message);

  /**
   * Sets an info status in the validation.
   * An info is used to indicate a state that converys information but does not
   * prevent continuation or execution.
   * 
   * @param key the key of the property with which the info is associated.
   * @param message the info message.
   */
  void info(final String key, final String message);

  /**
   * Returns <i>true</i> if the validaton contains at least one error status; <i>false</i> if not.
   */
  boolean containsError();

  /**
   * Returns the validation status associated with the given key.
   * 
   * @param key the key to check.
   * @return the associated status; or <i>null</i> if none.
   */
  IStatus getStatus(final String key);

  /**
   * Returns a array of all the status currently in the validation.
   */
  IStatus[] getStatus();

  /**
   * Returns an array of all the keys currently in the validation.
   */
  String[] getStatusKeys();

  /**
   * Returns a string containing all the status messages at or above the given severity.
   * @return
   */
  String getStatusMessages(int minSverity);
}