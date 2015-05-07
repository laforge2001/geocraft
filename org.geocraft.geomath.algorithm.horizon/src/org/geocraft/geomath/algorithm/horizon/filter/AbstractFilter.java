/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.horizon.filter;


import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.core.model.grid.Grid3d;


/**
 * Abstract implementation of the linear filers interface.
 */
public abstract class AbstractFilter implements IFilter {

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public boolean validateSize(final int size) {
    return size >= 3 && size % 2 != 0;
  }

  public String getMessage() {
    return "Size must be an odd value greater or equal to 3";
  }

  @Override
  public float[][] execute(Grid3d property, int size, final float[][] kernel, final IProgressMonitor monitor) {

    // apply the filter on the horizon
    return filter(property, kernel, size, monitor);
  }

  public float[][] filter(final Grid3d property, final float[][] kernel, final int size,
      final IProgressMonitor monitor) {
    // Get size of horizon
    int nRows = property.getNumRows();
    int nCols = property.getNumColumns();

    monitor.beginTask("Linear filter", nRows);
    // convert series data to a 2d array
    float[][] inputValues = property.getValues();
    float[][] outputValues = new float[nRows][nCols];
    int offset = (size - 1) / 2;
    float nullValue = property.getNullValue();
    float n = 0;
    for (int i = 0; i < size; i++) {
      for (int k = 0; k < size; k++) {
        n += kernel[i][k];
      }
    }

    // Apply the linear filter on the horizon
    for (int row = 0; row < nRows && !monitor.isCanceled(); row++) {
      for (int col = 0; col < nCols; col++) {
        outputValues[row][col] = inputValues[row][col];
        if (row >= offset && col >= offset && row < nRows - offset && col < nCols - offset) {
          boolean isNull = false;
          for (int i = row - offset; i <= row + offset && !isNull; i++) {
            for (int k = col - offset; k <= col + offset && !isNull; k++) {
              isNull = property.isNull(i, k);
            }
          }
          if (isNull) {
            outputValues[row][col] = nullValue;
          } else {
            float sum = 0;
            for (int i = 0; i < size; i++) {
              for (int k = 0; k < size; k++) {
                sum += inputValues[row - offset + i][col - offset + k] * kernel[i][k];
              }
            }
            outputValues[row][col] = sum / n;
          }
        }
      }
      monitor.worked(1);
    }
    monitor.done();
    return outputValues;
  }
}
