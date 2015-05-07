/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.curvature;


import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.core.common.math.QuadraticSurface;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.seismic.SeismicSurvey3d;


/**
 * Abstract implementation of the attribute interface.
 */
public abstract class AbstractAttribute implements IAttribute {

  protected float _nullValue;

  @Override
  public String toString() {
    return getName();
  }

  public float[][] attribute(final GridGeometry3d geometry, final Grid3d property, final int aperture,
      final IProgressMonitor monitor) {

    int nRows = geometry.getNumRows();
    int nCols = geometry.getNumColumns();

    SeismicSurvey3d survey = (SeismicSurvey3d) geometry;

    monitor.beginTask("Computing " + getName() + " attribute", nRows);

    float dx = (float) geometry.getColumnSpacing();
    float dy = (float) geometry.getRowSpacing();

    float[][] outputValues = new float[nRows][nCols];
    _nullValue = property.getNullValue();

    for (int r = 0; r < nRows; r++) {
      for (int c = 0; c < nCols; c++) {
        outputValues[r][c] = _nullValue;
      }
    }

    for (int r = aperture; r < nRows - aperture && !monitor.isCanceled(); r++) {

      skipNulls:

      for (int c = aperture; c < nCols - aperture; c++) {
        QuadraticSurface g = getQuadratic(property, dx, dy, r, c, aperture);
        if (g == null) {
          continue skipNulls;
        }

        double attribute = (float) calculate(g);

        if (Double.isNaN(attribute)) {
          attribute = _nullValue;
        }

        outputValues[r][c] = (float) attribute;
      }
      monitor.worked(1);
    }

    monitor.done();
    return outputValues;
  }

  /**
   * @param property
   * @param dx
   * @param dy
   * @param row
   * @param col
   * @param aperture
   * @return
   */
  public QuadraticSurface getQuadratic(final Grid3d property, final float dx, final float dy, final int row,
      final int col, final int aperture) {

    double[][] z = new double[3][3];

    // the row column order here is important! 
    int i = 2;

    for (int r = row - aperture; r < row + aperture + 1; r = r + aperture) {
      int j = 0;
      for (int c = col - aperture; c < col + aperture + 1; c = c + aperture) {

        if (property.isNull(r, c)) {
          return null;
        }

        z[i][j++] = property.getValueAtRowCol(r, c);
      }
      i--;
    }

    QuadraticSurface g = new QuadraticSurface(z, dx, dy);
    return g;
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof IAttribute)) {
      return false;
    }
    return super.equals(getName().equals(((IAttribute) obj).getName()));
  }

  @Override
  public int hashCode() {
    return getName().hashCode();
  }

}
