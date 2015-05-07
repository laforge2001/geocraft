/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer.renderer.grid;


import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.repository.specification.AbstractSpecification;


public class Grid3dSpecification extends AbstractSpecification {

  private final Grid3d _referenceGrid;

  public Grid3dSpecification(final Grid3d referenceGrid) {
    _referenceGrid = referenceGrid;
  }

  public boolean isSatisfiedBy(Object object) {
    // Match if the object is a grid with a matching geometry in the same z domain.
    if (object != null && object instanceof Grid3d) {
      Grid3d grid = (Grid3d) object;
      if (grid.getZDomain().equals(_referenceGrid.getZDomain())) {
        if (grid.getGeometry().matchesGeometry(_referenceGrid.getGeometry())) {
          return true;
        }
      }
    }
    return false;
  }

}
