/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.horizon.filter;


import java.awt.image.Kernel;


/**
 * Implementation of the Gaussian linear filer.
 */
public class GaussianFilter extends AbstractFilter {

  public String getName() {
    return "Gaussian";
  }

  /**
   * Calculates the discrete value at x,y of the 
   * 2D gaussian distribution.
   *
   * @param theta     the theta value for the gaussian distribution
   * @param x         the point at which to calculate the discrete value
   * @param y         the point at which to calculate the discrete value
   * @return          the discrete gaussian value
   */
  public static float gaussianDiscrete2D(final float theta, final int x, final int y) {
    float g = 0;
    for (float ySubPixel = (float) (y - 0.5); ySubPixel < y + 0.55; ySubPixel += 0.1) {
      for (float xSubPixel = (float) (x - 0.5); xSubPixel < x + 0.55; xSubPixel += 0.1) {
        g = (float) (g + 1 / (2 * Math.PI * theta * theta)
            * Math.pow(Math.E, -(xSubPixel * xSubPixel + ySubPixel * ySubPixel) / (2 * theta * theta)));
      }
    }
    g = g / 121;
    //System.out.println(g);
    return g;
  }

  /**
   * Calculates several discrete values of the 2D gaussian distribution.
   *
   * @param theta     the theta value for the gaussian distribution
   * @param size      the number of discrete values to calculate (pixels)
   * @return          2Darray (size*size) containing the calculated 
   * discrete values
   */
  public static float[][] gaussian2D(final float theta, final int size) {
    float[][] kernel = new float[size][size];
    for (int j = 0; j < size; ++j) {
      for (int i = 0; i < size; ++i) {
        kernel[i][j] = gaussianDiscrete2D(theta, i - size / 2, j - size / 2);
      }
    }

    double sum = 0;
    for (int j = 0; j < size; ++j) {
      for (int i = 0; i < size; ++i) {
        sum = sum + kernel[i][j];

      }
    }

    return kernel;
  }

  /**
   * Make a Gaussian blur kernel.
   */
  public float[][] getDefaultKernel(final int radius) {
    return gaussian2D(1f, radius);
  }

  public static Kernel makeKernel(final float radius) {
    int r = (int) Math.ceil(radius);
    int rows = r * 2 + 1;
    float[] matrix = new float[rows];
    float sigma = radius / 3;
    float sigma22 = 2 * sigma * sigma;
    float sigmaPi2 = (float) (2 * Math.PI * sigma);
    float sqrtSigmaPi2 = (float) Math.sqrt(sigmaPi2);
    float radius2 = radius * radius;
    float total = 0;
    int index = 0;
    for (int row = -r; row <= r; row++) {
      float distance = row * row;
      if (distance > radius2) {
        matrix[index] = 0;
      } else {
        matrix[index] = (float) Math.exp(-distance / sigma22) / sqrtSigmaPi2;
      }
      total += matrix[index];
      index++;
    }
    for (int i = 0; i < rows; i++) {
      matrix[i] /= total;
    }

    return new Kernel(rows, rows, matrix);
  }

}
