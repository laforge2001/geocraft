/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.specification;


import org.geocraft.core.model.well.Well;
import org.geocraft.core.repository.specification.AbstractSpecification;


public class WellIdentifierSpecification extends AbstractSpecification {

  private String _identifier;

  public WellIdentifierSpecification(String identifier) {
    _identifier = identifier;
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.repository.specification.ISpecification#isSatisfiedBy(java.lang.Object)
   */
  @Override
  public boolean isSatisfiedBy(Object obj) {
    if (obj instanceof Well) {
      return ((Well) obj).getIdentifier().equals(_identifier);
    }
    return false;
  }

}
