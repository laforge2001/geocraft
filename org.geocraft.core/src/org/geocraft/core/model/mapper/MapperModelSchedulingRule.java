/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.mapper;


import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;


public class MapperModelSchedulingRule implements ISchedulingRule {

  private final String _uniqueId;

  public MapperModelSchedulingRule(final String uniqueID) {
    _uniqueId = uniqueID;
  }

  public MapperModelSchedulingRule(final MapperModel model) {
    _uniqueId = model.getUniqueId();
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(org.eclipse.core.runtime.jobs.ISchedulingRule)
   */
  @Override
  public boolean contains(final ISchedulingRule rule) {
    if (this == rule) {
      return true;
    }
    if (rule instanceof MapperModelSchedulingRule) {
      return _uniqueId.equals(((MapperModelSchedulingRule) rule)._uniqueId);
    }
    if (rule instanceof MultiRule) {
      MultiRule multi = (MultiRule) rule;
      ISchedulingRule[] children = multi.getChildren();
      for (ISchedulingRule child : children) {
        if (!contains(child)) {
          return false;
        }
        return true;
      }
    }
    return false;
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(org.eclipse.core.runtime.jobs.ISchedulingRule)
   */
  @Override
  public boolean isConflicting(final ISchedulingRule rule) {
    if (!(rule instanceof MapperModelSchedulingRule)) {
      return false;
    }
    String otherUniqueId = ((MapperModelSchedulingRule) rule)._uniqueId;
    return _uniqueId.equals(otherUniqueId);
  }

}
