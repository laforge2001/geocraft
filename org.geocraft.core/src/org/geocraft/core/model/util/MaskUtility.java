/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.util;


import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.geometry.GridGeometry2d;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.seismic.SeismicSurvey2d;


public class MaskUtility {

  /**
   * Creates a boolean mask array for a given 3D geometry and area of
   * interest.
   * 
   * @param geometry the 3D geometry.
   * @param aoi the area of interest.
   * @return the boolean mask array (true=inside AOI, false=outside AOI).
   */
  public static boolean[][] createMask(final GridGeometry3d geometry, final AreaOfInterest aoi) {
    int numRows = geometry.getNumRows();
    int numCols = geometry.getNumColumns();
    boolean[][] mask = new boolean[numRows][numCols];
    for (int row = 0; row < numRows; row++) {
      for (int col = 0; col < numCols; col++) {
        double[] xy = geometry.transformRowColToXY(row, col);
        double x = xy[0];
        double y = xy[1];
        mask[row][col] = aoi.contains(x, y);
      }
    }
    return mask;
  }

  /**
   * Creates a boolean mask array for a given 2D geometry and area of
   * interest.
   * 
   * @param geometry the 2D geometry.
   * @param aoi the area of interest.
   * @return the boolean mask array (true=inside AOI, false=outside AOI).
   */
  public static boolean[][] createMask(final GridGeometry2d geometry, final AreaOfInterest aoi) {
    if (geometry instanceof SeismicSurvey2d) {
      return createMask((SeismicSurvey2d) geometry, aoi);
    }
    int numRows = geometry.getNumRows();
    boolean[][] mask = new boolean[numRows][];
    for (int row = 0; row < numRows; row++) {
      int numCols = geometry.getNumColumns(row);
      mask[row] = new boolean[numCols];
      for (int col = 0; col < numCols; col++) {
        double[] xy = geometry.getLine(row).transformBinToXY(col);
        double x = xy[0];
        double y = xy[1];
        mask[row][col] = aoi.contains(x, y);
      }
    }
    return mask;
  }

  /**
   * Creates a boolean mask array for a given 2D survey and area of interest.
   * 
   * @param survey the 2D survey.
   * @param aoi the area of interest.
   * @return the boolean mask array (true=inside AOI, false=outside AOI).
   */
  public static boolean[][] createMask(final SeismicSurvey2d survey, final AreaOfInterest aoi) {
    int numRows = survey.getNumRows();
    boolean[][] mask = new boolean[numRows][];
    for (int row = 0; row < numRows; row++) {
      int numCols = survey.getNumColumns(row);
      mask[row] = new boolean[numCols];
      for (int col = 0; col < numCols; col++) {
        double[] xy = survey.getLine(row).transformBinToXY(col);
        double x = xy[0];
        double y = xy[1];
        mask[row][col] = aoi.contains(x, y, survey);
      }
    }
    return mask;
  }

}
