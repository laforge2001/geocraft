/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.geomath.algorithm.util.parameters;


import org.geocraft.core.model.datatypes.Domain;


/**
 * A factory for creating parameters.
 */
public final class ParameterFactory {

  /**
   * Creates a domain parameter.
   * @param name the name of the parameter.
   * @param initValue the initial value of the parameter.
   * @return the domain parameter.
   */
  public static DomainParameter createDomainParameter(final String uniqueId, final String name, final Domain initValue) {
    return new DomainParameter(uniqueId, name, initValue);
  }

  /**
   * Creates a double parameter.
   * @param name the name of the parameter.
   * @param initValue the initial value of the parameter.
   * @return the double parameter.
   */
  public static DoubleParameter createDoubleParameter(final String uniqueId, final String name, final double initValue) {
    return new DoubleParameter(uniqueId, name, initValue);
  }

  /**
   * Creates a float parameter.
   * @param name the name of the parameter.
   * @param initValue the initial value of the parameter.
   * @return the double parameter.
   */
  public static FloatParameter createFloatParameter(final String uniqueId, final String name, final float initValue) {
    return new FloatParameter(uniqueId, name, initValue);
  }

  /**
   * Creates a string parameter.
   * @param name the name of the parameter.
   * @param initValue the initial value of the parameter.
   * @return the string parameter.
   */
  public static StringParameter createStringParameter(final String uniqueId, final String name, final String initValue) {
    return new StringParameter(uniqueId, name, initValue);
  }

  /**
   * Creates a string parameter.
   * @param name the name of the parameter.
   * @param initValue the initial value of the parameter.
   * @param validValues the array of valid values.
   * @return the string parameter.
   */
  public static StringParameter createStringParameter(final String uniqueId, final String name, final String initValue,
      final String[] validValues) {
    return new StringParameter(uniqueId, name, initValue, validValues);
  }

  /**
   * Creates a mult-line string parameter.
   * @param uniqueID the unique ID of the parameter.
   * @param name the name of the parameter.
   * @param help the help of the parameter.
   * @param initValue the initial value of the parameter.
   * @return the string parameter.
   */
  public static StringParameter createMultiLineParameter(final String uniqueId, final String name,
      final String initValue) {
    return new StringParameter(uniqueId, name, initValue, true);
  }

}
