/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.geomath.algorithm.util.parameters;


/**
 * The interface for a parameter.
 */
public interface IParameter {

  /**
   * Gets the unique ID of the parameter.
   * @return the unique ID of the parameter.
   */
  String getUniqueID();

  /**
   * Gets the parameter value as a string.
   * @return the parameter value as a string.
   */
  String getValueAsString();

  /**
   * Gets the value object of the parameter.
   * @return the value object to set.
   */
  Object getValueObject();

  /**
   * Sets the value object of the parameter.
   * @param valueObject the value object to set.
   */
  void setValueObject(Object valueObject);
}
