/*
 * Copyright (C) ConocoPhillips 2006 - 2007 All Rights Reserved.
 */

package org.geocraft.core.repository.specification;


/**
 * Used to lookup a variable name given the object reference.
 */
public class ObjectSpecification extends AbstractSpecification {

  private Object _object;

  /**
   * The constructor.
   * 
   * @param obj
   *          the object we are looking for.
   */
  public ObjectSpecification(Object obj) {
    _object = obj;
  }

  public boolean isSatisfiedBy(Object obj) {
    // TODO what happens when we wrap horizons?
    if (obj == _object) {
      return true;
    }
    return false;
  }
}
