/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.core.common.math;


import java.util.Arrays;


/**
 * Generic math functions. I plan to add methods for checking floating point equality etc. into
 * here.
 */
public class MathUtil {

  /**
   *
   */
  private MathUtil() {
    // left blank
  }

  /**
   * Determine the format string that will display the number in the specified number of characters
   * preserving the maximum possible precision. Java's formatter is not like the F77 one so this
   * convoluted code is needed to work around that. One issue is that Java takes the width to be the
   * minimum width and will take up more space if it needs it. This messes up F77 parsers which
   * often assume a fixed record width.
   * @param value the number to format.
   * @param maxWidth the available number of characters to display the number.
   * @return a format string.
   */
  public static String computeFormat(final double value, final int maxWidth) {

    String fmtstr;

    // make room for a minus sign
    int minus = 0;

    // this could be -0 hence the =
    if (value <= 0) {
      minus = 1;
    }

    // -1 < value < 1 need special treatment
    if (Math.abs(value) < 1) {
      fmtstr = "%" + maxWidth + "." + (maxWidth - 2 - minus) + "f";

    } else {

      int numDigits = 1 + (int) Math.log10(Math.abs(value)) + minus;

      if (numDigits > maxWidth) {

        // numbers larger than maxWidth need to be in scientific format
        fmtstr = "%" + maxWidth + "." + (maxWidth - 5 - minus) + "g";
      } else if (numDigits == maxWidth) {

        // numbers the exact width are a special case
        fmtstr = "%" + maxWidth + ".0f";
      } else {

        // values that are shorter than maxwidth and abs(value) > 1
        fmtstr = "%" + maxWidth + "." + (maxWidth - numDigits - 1) + "f";
      }
    }
    return fmtstr;
  }

  /**
   * Java's modulus operator is disappointingly different to other languages. I wanted to take a
   * number and corral it into the range 0 ... divisor which you can do with a % in Python. However,
   * in Java you get the 'wrong' answer for negative dividends. So the hack is to add (n * divisor)
   * to the dividend to make it positive.
   */

  public static int mod(final int dividend, final int divisor) {

    assert divisor > 0 : "have not implemented negative divisors yet";

    int dividend2 = dividend;

    if (dividend2 < 0) {
      int makePositive = (1 + -1 * dividend2 / divisor) * divisor;

      dividend2 = dividend2 + makePositive;
    }

    return dividend2 % divisor;

  }

  /**
   * Convert a number in a range between start2 and end2 into a number in the range start1 and end1.
   * Example: scale(0, 100, 0, 1, 0.5) returns 50
   */
  public static double scale(final double start1, final double end1, final double start2, final double end2,
      final double actual2) {
    return start1 + (end1 - start1) * (actual2 - start2) / (end2 - start2);
  }

  /**
   * Compute whether two double precision numbers are roughly or exactly equal.
   * <p>
   * 1) check if both numbers are trivially equal. This also takes care of the special case where both arguments are zero.
   * <p>
   * 2) if the second argument is zero then the division will give a NaN and will always return false.
   * <p>
   * I guessed that a/b-1 would cope with rounding better than (a-b)/b but it might not.
   */
  public static boolean isEqual(final double number1, final double number2) {
    if (number1 == number2) {
      return true;
    } else if (new Double(number1).equals(new Double(number2))) {
      // this will handle infinitives and NaNs
      return true;
    } else if (Math.abs((number1 - number2) / number1) < 0.000001) {
      return true;
    }
    return false;
  }

  /**
   * Compute whether two floats are roughly or exactly equal.
   * <p>
   * 1) check if both numbers are trivially equal. This also takes care of the special case where both arguments are zero.
   * <p>
   * 2) if the second argument is zero then the division will give a NaN and will always return false.
   * <p>
   * I guessed that a/b-1 would cope with rounding better than (a-b)/b but it might not.
   */
  public static boolean isEqual(final float number1, final float number2) {
    if (number1 == number2) {
      return true;
    } else if (new Float(number1).equals(new Float(number2))) {
      // this will handle infinitives and NaNs
      return true;
    } else if (Math.abs((number1 - number2) / number1) < 0.000001f) {
      return true;
    }
    return false;
  }

  /**
   * Compute whether two double precision numbers are roughly or exactly equal.
   * <p>
   * 1) check if both numbers are trivially equal. This also takes care of the special case where both arguments are zero.
   * <p>
   * 2) if the second argument is zero then the division will give a NaN and will always return false.
   * <p>
   * I guessed that a/b-1 would cope with rounding better than (a-b)/b but it might not.
   */
  public static boolean isEqual(final double number1, final double number2, final double tolerance) {
    if (number1 == number2) {
      return true;
    } else if (new Double(number1).equals(new Double(number2))) {
      // this will handle infinitives and NaNs
      return true;
    } else if (Math.abs((number1 - number2) / number1) < tolerance) {
      return true;
    }
    return false;
  }

  /**
   * Compute whether two floats are roughly or exactly equal.
   * <p>
   * 1) check if both numbers are trivially equal. This also takes care of the special case where both arguments are zero.
   * <p>
   * 2) if the second argument is zero then the division will give a NaN and will always return false.
   * <p>
   * I guessed that a/b-1 would cope with rounding better than (a-b)/b but it might not.
   */
  public static boolean isEqual(final float number1, final float number2, final float tolerance) {
    if (number1 == number2) {
      return true;
    } else if (new Float(number1).equals(new Float(number2))) {
      // this will handle infinitives and NaNs
      return true;
    } else if (Math.abs((number1 - number2) / number1) < tolerance) {
      return true;
    }
    return false;
  }

  public static float[][] convert(final float[] data, final int rows, final int cols) {
    float[][] result = new float[rows][cols];
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        result[row][col] = data[row * cols + col];
      }
    }
    return result;
  }

  public static float[] convert(final double[][] data) {
    int cols = data[0].length;
    int rows = data.length;
    int i = 0;
    float[] result = new float[rows * cols];
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        result[i++] = (float) data[row][col];
      }
    }
    return result;
  }

  public static float[] convert(final double[] data) {
    int nVals = data.length;
    float[] result = new float[nVals];
    for (int i1 = 0; i1 < nVals; i1++) {
      result[i1] = (float) data[i1];
    }
    return result;
  }

  public static double[] convert(final float[] data) {
    int nVals = data.length;
    double[] result = new double[nVals];
    for (int i1 = 0; i1 < nVals; i1++) {
      result[i1] = data[i1];
    }
    return result;
  }

  /**
   * Convert a 2D array of float values to an 1D array of double values. Works for 2D input arrays
   * with rows having different number of elements.
   * @param data the 2D array
   * @return the 1D array
   */
  public static double[] convert2D(final float[][] data) {
    int nr = 0;
    for (float[] element : data) {
      nr += element.length;
    }
    int rows = data.length;
    int i = 0;
    double[] result = new double[nr];
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < data[row].length; col++) {
        result[i++] = data[row][col];
      }
    }
    return result;
  }

  public static float[][] convert2(final float[] data, final int rows, final int cols) {
    float[][] result = new float[rows][cols];
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        result[row][col] = data[row * cols + col];
      }
    }
    return result;
  }

  public static float[] convert(final float[][] data) {
    int cols = data[0].length;
    int rows = data.length;
    int i = 0;
    float[] result = new float[rows * cols];
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        result[i++] = data[row][col];
      }
    }
    return result;
  }

  /**
   * Compute the minimum and maximum values in an array of doubles.
   * Handle the case where all points are null.
   * @param data the array data
   * @return the range
   */
  public static double[] computeRange(final double[] data, final double nullValue) {

    double min = Double.MAX_VALUE;
    double max = -1 * Double.MAX_VALUE;

    for (int i = 0; i < data.length; i++) {
      if (!isEqual(data[i], nullValue)) {
        min = Math.min(min, data[i]);
        max = Math.max(max, data[i]);
      }
    }

    if (min == Double.MAX_VALUE) {
      min = 0;
    }

    if (max == -1 * Double.MAX_VALUE) {
      max = 0;
    }

    return new double[] { min, max };
  }

  /**
   * Compute the minimum and maximum values in an array of floats.
   * Handle the case where all points are null.
   * @param data the array data
   * @param nullValue the null value
   * @return the range
   */
  public static float[] computeRange(final float[] data, final float nullValue) {

    float min = Float.MAX_VALUE;
    float max = -1 * Float.MAX_VALUE;

    for (int i = 0; i < data.length; i++) {
      if (!MathUtil.isEqual(data[i], nullValue)) {
        min = Math.min(min, data[i]);
        max = Math.max(max, data[i]);
      }
    }

    if (min == Float.MAX_VALUE) {
      min = 0;
    }

    if (max == -1 * Float.MAX_VALUE) {
      max = 0;
    }

    return new float[] { min, max };
  }

  public static float[] computePercentiles(final float[][] data, final float nullValue, final float percentile) {
    int cols = data[0].length;
    int rows = data.length;
    int numNulls = 0;
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        if (isEqual(data[row][col], nullValue)) {
          numNulls++;
        }
      }
    }

    float noNulls[] = new float[rows * cols - numNulls];

    int i = 0;
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        if (!isEqual(data[row][col], nullValue)) {
          noNulls[i++] = data[row][col];
        }
      }
    }

    Arrays.sort(noNulls);

    int index = (int) (noNulls.length * percentile / 100);

    return new float[] { noNulls[index], noNulls[noNulls.length - 1 - index] };
  }

  public static float[] computePercentiles(final float[] data, final float nullValue, final float percentile) {

    float[] noNulls = removeNulls(data, nullValue);

    Arrays.sort(noNulls);

    int index = (int) (noNulls.length * percentile / 100);

    return new float[] { noNulls[index], noNulls[noNulls.length - 1 - index] };
  }

  public static float[] removeNulls(final float[] data, final float nullValue) {
    int numNulls = 0;
    int rows = data.length;
    for (int row = 0; row < rows; row++) {
      if (isEqual(data[row], nullValue)) {
        numNulls++;
      }
    }

    float noNulls[] = new float[rows - numNulls];

    int i = 0;
    for (int row = 0; row < rows; row++) {
      if (!isEqual(data[row], nullValue)) {
        noNulls[i++] = data[row];
      }
    }

    return noNulls;
  }

  /**
   * Simple utility method that is useful for things like setting a results array to 
   * a null value before starting a computation. 
   * 
   * @param data
   * @param constant
   * @return
   */
  public static float[][] setConstantArray(final float[][] data, final float constant) {
    for (int row = 0; row < data.length; row++) {
      for (int col = 0; col < data[0].length; col++) {
        data[row][col] = constant;
      }
    }
    return data;
  }

  /**
   * Simple utility method that is useful for things like setting a results array to 
   * a null value before starting a computation. 
   * 
   * @param row
   * @param col
   * @param constant
   * @return
   */
  public static float[][] createArray(final int row, final int col, final float constant) {
    float[][] data = new float[row][col];
    return setConstantArray(data, constant);
  }

  /**
   * Resamples a float array.
   * The output array will be of length 2N-1, where N is the length of the input array.
   * @param dataIn the input array.
   * @return the resampled array.
   */
  public static float[] resample(final float[] dataIn) {
    double[] syncFilter = { -2.6805043e-03, 3.9292198e-03, -7.5119166e-03, 1.4123353e-02, -2.4716860e-02,
        4.0884181e-02, -6.5915602e-02, 1.0842265e-01, -2.0044142e-01, 6.3262575e-01, 6.3262575e-01, -2.0044142e-01,
        1.0842265e-01, -6.5915602e-02, 4.0884181e-02, -2.4716860e-02, 1.4123353e-02, -7.5119166e-03, 3.9292198e-03,
        -2.6805043e-03 };

    float[] convolved = new float[dataIn.length + syncFilter.length];
    float[] dataOut = new float[dataIn.length * 2 - 1];

    convolve(dataIn, syncFilter, convolved);

    // Interleave original wavelet samples and sync-interpolated samples.
    for (int i = 0, j = 10, k = 0; i < dataIn.length * 2 - 1; i++) {
      if ((i & 1) != 0) {
        dataOut[i] = convolved[j];
        j++;
      } else {
        dataOut[i] = dataIn[k];
        k++;
      }
    }
    convolved = null;
    return dataOut;
  }

  private static int convolve(final float[] x, final double[] y, final float[] out) {
    int length = x.length + y.length;
    for (int i = 0; i < length; i++) {
      out[i] = 0;
    }
    for (int i = 0; i < x.length; i++) {
      for (int j = 0; j < y.length; j++) {
        out[i + j] += x[i] * y[j];
      }
    }
    return length;
  }
}
