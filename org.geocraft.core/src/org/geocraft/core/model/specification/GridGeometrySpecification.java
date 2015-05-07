/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.specification;


import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.repository.specification.AbstractSpecification;


public class GridGeometrySpecification extends AbstractSpecification {

  GridGeometry3d _geometry;

  public GridGeometrySpecification(final GridGeometry3d geometry) {
    _geometry = geometry;
  }

  @Override
  public boolean isSatisfiedBy(final Object obj) {
    if (obj instanceof Grid3d) {
      Grid3d grid = (Grid3d) obj;
      // TODO this implies a bigger problem with the lazy 
      // loading because getDataUnit() should never return null. 
      if (grid.getDataUnit() == null) {
        return false;
      }
      if (_geometry.matchesGeometry(grid.getGeometry())) {
        return true;
      }
    }
    return false;
  }
}
