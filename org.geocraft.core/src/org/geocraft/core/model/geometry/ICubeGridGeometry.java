/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.geometry;




public interface ICubeGridGeometry extends IGridGeometry {

  /**
   * @return the number of values in the Z direction
   */
  public abstract int getNumZIndices();

  public abstract boolean containsZIndex(int z);

  /**
   * @return the smallest depth Index
   */
  public abstract int getMinimumZIndex();

  /**
   * @return the maximum depth Index
   */
  public abstract int getMaximumZIndex();

}
