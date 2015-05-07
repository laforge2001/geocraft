/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.grid;


import org.geocraft.core.model.mapper.IMapper;


public class CubeGridCellProperty extends AbstractCubeGridProperty {

  private float[] _attributes;

  public CubeGridCellProperty(final String propertyName, final IMapper mapper, final CubeGrid grid) {
    super(propertyName, mapper, grid);
  }

  /**
   * @return the propertyName
   */
  @Override
  public String getPropertyName() {
    load();
    return getDisplayName();
  }

  /**
   * @param propertyName the propertyName to set
   */
  public void setPropertyName(final String propertyName) {
    setDisplayName(propertyName);
    setDirty(true);
  }

  /**
   * USE AT YOUR OWN RISK!
   * @return the REFERENCE to the entire array of attributes for the grid 
   */
  public float getAttribute(final int eventNum) {
    load();
    return _attributes[eventNum];
  }

  /**
   * @param attribute the attribute to set
   */
  public void setAttribute(final int index, final float attribute) {
    _attributes[index] = attribute;
    setDirty(true);
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.grid.AbstractCubeGridProperty#setNumCells(int, int)
   */
  @Override
  public void setNumElements(final int numCells) {
    _attributes = new float[numCells];
  }

  /* (non-Javadoc)
   * @see org.geocraft.core.model.grid.AbstractCubeGridProperty#getSize()
   */
  @Override
  public int getSize() {
    load();
    return _attributes.length;
  }

}
