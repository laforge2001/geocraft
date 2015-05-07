/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.specification;


import org.geocraft.core.model.Entity;
import org.geocraft.core.repository.specification.AbstractSpecification;


public class EntityUniqueIdSpecification extends AbstractSpecification {

  private String _id;

  public EntityUniqueIdSpecification(final String id) {
    _id = id;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.repository.specification.ISpecification#isSatisfiedBy(java.lang.Object)
   */
  @Override
  public boolean isSatisfiedBy(Object obj) {
    if (obj instanceof Entity) {
      Entity testName = (Entity) obj;
      if (testName.getUniqueID().equals(_id)) {
        return true;
      }
    }
    return false;
  }

}
