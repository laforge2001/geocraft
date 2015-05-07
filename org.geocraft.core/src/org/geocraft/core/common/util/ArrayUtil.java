/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */
package org.geocraft.core.common.util;


import org.geocraft.core.service.ServiceProvider;


/**
 * This is interesting. Previous projects suggest that we should not put algorithm-like methods into the fundamental domain objects.
 * One persons useful algorithm may not be of any relevance to someone in a slightly different domain. So this is kind of where OO
 * seems to break down a bit for geophysical software. OO is great for the GUI and the framework but it would not be appropriate to
 * add FFT methods directly to the horizon class.
 * 
 * If we add behavior by extending classes we hit the problem that Java does not support multiple inheritance. The domain objects
 * are hard enough to understand when they have none of these optional methods in them. Sub-classing just makes them even more
 * opaque.
 * 
 * Even so, ArrayUtil is probably not the best name for this class :-)
 */
public class ArrayUtil {

  /**
   * No need to instantiate this class.
   */
  private ArrayUtil() {
    // left blank
  }

  /**
   * Used to convert from a single column array into an m x n array (assumed row-major). TODO check if (numRows * numCols ==
   * array1D.length).
   * 
   * @param array1D
   *                the 1d data array.
   * @param numRows
   *                the number of rows.
   * @param numCols
   *                the number of columns.
   * @return the 2d data array.
   */
  public static double[][] oneDtoTwoD(final double[] array1D, final int numRows, final int numCols) {
    double[][] array2D = new double[numRows][numCols];
    // TODO this should be faster but I seem to be too tired to make it work today :-)
    // for (int i = 0; i < numCols; i++) {
    // double[] tmpCol = new double[numRows];
    // System.arraycopy(array1D, i * numRows, tmpCol, 0, numRows);
    // array2D[i] = tmpCol;
    // }
    int k = 0;
    for (int i = 0; i < numRows; i++) {
      for (int j = 0; j < numCols; j++) {
        array2D[i][j] = array1D[k++];
      }
    }
    return array2D;
  }

  /**
   * Used to convert from a 2d array to a 1d array (assumes row-major).
   * 
   * @param array2D
   *                the 2d data array.
   * @return the 1d data array.
   */
  public static double[] twoDtoOneD(final double[][] array2D) {
    int numColumns = array2D[0].length;
    int numRows = array2D.length;
    ServiceProvider.getLoggingService().getLogger(ArrayUtil.class).debug(numColumns + " " + numRows);
    double[] array1D = new double[numColumns * numRows];
    int k = 0;
    for (int j = 0; j < numRows; j++) {
      for (int i = 0; i < numColumns; i++) {
        array1D[k++] = array2D[j][i];
      }
    }
    // TODO this should be faster but I seem to be too tired to make it work today :-)
    // double[] tmpCol = null;
    // for (int i = 0; i < numColumns; i++) {
    // tmpCol = array2D[i];
    // System.arraycopy(tmpCol, 0, array1D, i * numRows, numRows);
    // }
    return array1D;
  }

  /**
   * This method returns the number with the most characters.
   * 
   * @param values the values to examine.
   * @return the number value in the horizon.
   */
  public static double getMaxCharValue(final float[] values) {
    double maxCharValue = 0.0;
    for (float value : values) {
      if (Math.abs(maxCharValue) < Math.abs(value)) {
        maxCharValue = value;
      }
    }
    return maxCharValue;
  }

  /**
   * This method returns the number with the most characters.
   * 
   * @param values the values to examine.
   * @return the number value in the horizon.
   */
  public static double getMaxCharValue(final float[][] values) {
    double maxCharValue = 0.0;
    for (float[] value : values) {
      for (float element : value) {
        if (Math.abs(maxCharValue) < Math.abs(element)) {
          maxCharValue = element;
        }
      }
    }
    return maxCharValue;
  }
}
