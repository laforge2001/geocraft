/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.mapper;


import org.geocraft.core.model.grid.Grid2d;


/**
 * The interface for mapping between a datastore and a <code>Grid2d</code> entity.
 */
public interface IGrid2dMapper extends IMapper<Grid2d> {

  /**
   * Gets the array of grid values from the datastore.
   * 
   * @param grid the grid to read from.
   * @param lineNumber the line number.
   * @return the array of grid values.
   */
  float[] getValues(final Grid2d grid, final int lineNumber);

  /**
   * Puts an array of grid values into the datastore.
   * 
   * @param grid the grid to write to.
   * @param lineNumber the line number.
   * @param values the array of grid values.
   */
  void putValues(final Grid2d grid, final int lineNumber, float[] values);
}
