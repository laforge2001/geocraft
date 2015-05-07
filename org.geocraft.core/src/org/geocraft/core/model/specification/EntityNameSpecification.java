/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.specification;


import org.geocraft.core.model.Entity;
import org.geocraft.core.repository.specification.AbstractSpecification;


public class EntityNameSpecification extends AbstractSpecification {

  private final String _name;

  public EntityNameSpecification(final String name) {
    _name = name;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.repository.specification.ISpecification#isSatisfiedBy(java.lang.Object)
   */
  @Override
  public boolean isSatisfiedBy(final Object obj) {
    if (obj instanceof Entity) {
      Entity testName = (Entity) obj;
      if (testName.getDisplayName().equals(_name)) {
        return true;
      }
    }
    return false;
  }

}
