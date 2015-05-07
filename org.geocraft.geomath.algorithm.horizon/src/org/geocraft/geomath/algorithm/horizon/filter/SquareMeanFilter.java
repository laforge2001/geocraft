/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.horizon.filter;


/**
 * Implementation of the mean with uniform weights and square support linear filer.
 */
public class SquareMeanFilter extends AbstractFilter {

  public String getName() {
    return "Square mean";
  }

  public float[][] getDefaultKernel(final int size) {
    float[][] kernel = new float[size][size];
    for (int i = 0; i < size; i++) {
      for (int k = 0; k < size; k++) {
        kernel[i][k] = 1;
      }
    }
    return kernel;
  }
}
