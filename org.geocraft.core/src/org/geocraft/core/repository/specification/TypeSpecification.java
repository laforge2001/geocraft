/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.core.repository.specification;


/**
 * The specification that does filtering on data types.
 */
public class TypeSpecification extends AbstractSpecification {

  /** The supported classes. */
  private Class[] _klasses;

  /**
   * The constructor.
   * 
   * @param klass
   *          the supported class for filtering
   */
  public TypeSpecification(Class klass) {
    _klasses = new Class[] { klass };
  }

  /**
   * The constructor.
   * 
   * @param klasses
   *          the supported classes for filtering
   */
  public TypeSpecification(Class[] klasses) {
    _klasses = klasses;
  }

  /**
   * Check if the provided object satisfies the data type specification.
   * 
   * @param obj
   *          the object to be tested
   * @return <code>true</code> if the object is of the data types, <code>false</code> otherwise
   */
  public boolean isSatisfiedBy(Object obj) {
    for (Class<?> klass : _klasses) {
      if (obj != null && klass.isAssignableFrom(obj.getClass())) {
        return true;
      }
    }
    return false;
  }
}
