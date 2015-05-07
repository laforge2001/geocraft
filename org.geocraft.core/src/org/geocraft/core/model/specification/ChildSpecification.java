/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.core.model.specification;


import org.geocraft.core.model.well.WellLogTrace;
import org.geocraft.core.model.well.WellPick;
import org.geocraft.core.repository.specification.AbstractSpecification;


/**
 * The specification that does filtering on data types.
 */
public class ChildSpecification extends AbstractSpecification {

  /** The child classes. */
  private final Class _klass;

  /** Entity we want look up the children of. */
  private final Object _parent;

  /**
   * The constructor.
   * 
   * @param klass
   *                the supported class for filtering
   */
  public ChildSpecification(final Class klass, final Object parent) {
    _klass = klass;
    _parent = parent;
  }

  /**
   * Check if the provided object satisfies the data type specification.
   * 
   * @param obj the object to be tested.
   * @return <code>true</code> if the object is of the data types, <code>false</code> otherwise.
   */
  public boolean isSatisfiedBy(final Object obj) {
    if (_klass == WellPick.class && obj instanceof WellPick) {
      if (((WellPick) obj).getWell() == _parent) {
        return true;
      }
    } else if (_klass == WellLogTrace.class && obj instanceof WellLogTrace) {
      if (((WellLogTrace) obj).getWell() == _parent) {
        return true;
      }
    }
    return false;
  }
}
