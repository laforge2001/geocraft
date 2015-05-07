/*
 * Copyright (C) ConocoPhillips 2009 All Rights Reserved. 
 */
package org.geocraft.core.model.specification;


import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.repository.specification.AbstractSpecification;


public class GridSpecification extends AbstractSpecification {

  private final Domain _unitDomain;

  public GridSpecification(final Domain unitDomain) {
    _unitDomain = unitDomain;
  }

  public boolean isSatisfiedBy(final Object obj) {
    if (!(obj instanceof Grid3d)) {
      return false;
    }

    Grid3d grid = (Grid3d) obj;
    // TODO this implies a bigger problem with the lazy 
    // loading because getDataUnit() should never return null.
    Unit unit = grid.getDataUnit();
    return unit != null && unit.getDomain() == _unitDomain ? true : false;
  }
}
