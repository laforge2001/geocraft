/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.horizon.filter;


import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.core.model.grid.Grid3d;


/**
 * Interface to be implemented by the linear filers.
 */
public interface IFilter {

  /**
   * Return the name of the filter.
   * @return the filter name
   */
  String getName();

  /**
   * Return the name of the filter.
   * @return the filter name
   */
  String toString();

  /**
   * Validate the provided size for the kernel.
   * @param size the algorithm kernel size
   * @return if the size is valid
   */
  boolean validateSize(int size);

  /**
   * Return a message in case the size is not valid.
   * @return a warning message
   */
  String getMessage();

  /**
   * Return the default kernel values for the given size.
   * @param size the kernel size
   * @return the kernel values
   */
  float[][] getDefaultKernel(int size);

  /**
   * Execute the linear filtering algorithm. 
   * @param model the filter model
   * @param kernel the kernel
   * @param monitor the progress monitor
   * @return the output values after the filter was applied
   */
  float[][] execute(Grid3d property, int size, float[][] kernel, final IProgressMonitor monitor);
}
