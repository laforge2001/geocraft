/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.geomath.algorithm.velocity;


import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.repository.specification.AbstractSpecification;


/**
 * Defines a filter for <code>Grid3d</code> entities with data in the depth domain.
 */
public class DepthGridSpecification extends AbstractSpecification {

  @Override
  public boolean isSatisfiedBy(Object obj) {
    if (obj != null && Grid3d.class.isAssignableFrom(obj.getClass())) {
      Grid3d grid = (Grid3d) obj;
      // Check that the grid is in depth.
      return grid.isDepthGrid();
    }
    return false;
  }
}
