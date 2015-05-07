/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.grid;


import org.geocraft.core.model.mapper.IMapper;


public class CubeGridCornerPointProperty extends AbstractCubeGridProperty {

  private float[][] _attributes;

  private final static int NUM_CORNER_POINTS = 8;

  public CubeGridCornerPointProperty(final String attributeName, final IMapper mapper, final CubeGrid grid) {
    super(attributeName, mapper, grid);
  }

  /**
   * @return the _propertyName
   */
  @Override
  public String getPropertyName() {
    load();
    return getDisplayName();
  }

  /**
   * 
   * @param cellNum the cell number to apply attributes to
   * @param index 
   * @param attribute the attributes for each corner point to set
   */
  public void setAttribute(final int index, final float[] attribute) {
    System.arraycopy(attribute, 0, _attributes[index], 0, NUM_CORNER_POINTS);
    setDirty(true);
  }

  /**
   * USE AT YOUR OWN RISK!
   * @return the a REFERENCE to the entire array of attributes USE AT YOUR OWN RISK!
   */
  public float[] getAttribute(final int elementNumber) {
    load();
    return _attributes[elementNumber];
  }

  @Override
  public void setNumElements(final int numElements) {
    _attributes = new float[numElements][NUM_CORNER_POINTS];
  }

  @Override
  public int getSize() {
    load();
    return _attributes.length;
  }
}
