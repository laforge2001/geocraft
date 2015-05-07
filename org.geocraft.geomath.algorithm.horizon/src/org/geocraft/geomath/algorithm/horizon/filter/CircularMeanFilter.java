/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.horizon.filter;


/**
 * Implementation of the mean with uniform weights and circular support linear filer.
 */
public class CircularMeanFilter extends AbstractFilter {

  public String getName() {
    return "Circular mean";
  }

  public float[][] getDefaultKernel(final int size) {
    float[][] kernel = new float[size][size];
    int center = size / 2;
    int value = center + 2;
    for (int i = 0; i < size; i++) {
      for (int k = 0; k < size; k++) {
        if (Math.abs(center - i) + Math.abs(center - k) < value) {
          kernel[i][k] = 1;
        }
      }
    }
    return kernel;
  }
}
