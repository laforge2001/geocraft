/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */
/*
 * Method roundup() was based on stuff rewritten twice:
 * I copied from http://www.superliminal.com/sources/Axis.java.html
 * who apparently copied from Ptolemy
 * who apparently copied from xgraph.c by David Harrison
 */
package org.geocraft.core.common.util;


import static org.geocraft.core.common.math.Constants.DOUBLE_DELTA;

import java.util.ArrayList;
import java.util.List;


/**
 * Computes esthetically pleasing labels for use in charts and reports. 
 * 
 * Can automatically compute the optimum increment for the labels or 
 * use whatever you specify. 
 * 
 * Handles cases involving huge and tiny numbers and won't return 
 * ugly labels like -0 or 2.499999999 etc. 
 * 
 * I was surprised how hard this class was to write so if you can
 * see a way to simplify it please have it. 
 */
public class Labels {

  /** The z values of the labels. */
  private double[] _zValues;

  /** Nicely formatted string representation of the labels. */
  private String[] _sValues;

  /** The z increment for the labels. */
  private final double _zStep;

  /**
   * Use this constructor when you already have a known desired
   * label increment. The start and end values will still be adjusted
   * to make the labels pretty. 
   * 
   * Example:
   * 
   * 3.3, 10.2 , 1 ---> 4, 5, 6, 7, 8, 9, 10
   * 
   * @param start
   * @param end
   * @param step
   */
  public Labels(final double start, final double end, final double step) {
    _zStep = step;
    init(start, end, step);
  }

  /**
   * Use this constructor when you only know the approximate number
   * of labels you want. 
   * 
   * Example:
   * 
   * 3.3, 10.2, 5 ---> 4, 5, 6, 7, 8, 9, 10
   * 
   * @param start the starting value
   * @param end the end value
   * @param approxNumLabels approx number of desired labels. 
   */
  public Labels(final double start, final double end, final int approxNumLabels) {

    if (approxNumLabels < 1) {
      throw new IllegalArgumentException("Number of labels cannot be less than one");
    }

    _zStep = computeStep(start, end, approxNumLabels);

    init(start, end, _zStep);
  }

  public double[] getZValues() {
    return _zValues;
  }

  public String[] getZLabels() {
    return _sValues;
  }

  /** 
   * The increment between the labels. 
   * 
   * eg 0, 1, 2, 3 -> 1
   * eg 3, 2, 1, 0 -> -1
   * @return
   */
  public double getIncrement() {
    return _zStep;
  }

  /**
   * Determine the optimum label values, and the appropriate format for the
   * String representation. 
   * 
   * @param start
   * @param end
   * @param step negative if start < end
   */
  private void init(final double start, final double end, final double step) {

    if (start > end && step >= 0) {
      throw new IllegalArgumentException("When start is greater than end; step must be negative: " + start + " " + end
          + " " + step);
    }

    // trim end values so the start and end are pretty
    // eg 10.1 might be rounded to 10

    double s = start;
    double e = end;

    // if they are the same number step == 0 and Math.ceil would compute s as NaN
    if (step != 0) {
      s = step * Math.ceil(start / step);
      e = step * Math.floor(end / step);
    }

    // compute number of labels and special case where start and end are same
    int numLabels = 1;
    if (e != s) {
      numLabels = 1 + (int) ((e - s) / step);
    }

    _zValues = new double[numLabels];
    _sValues = new String[numLabels];

    for (int i = 0; i < numLabels; i++) {
      _zValues[i] = s + i * step;
      _sValues[i] = String.format(getFormat(_zValues[i]), _zValues[i]);
    }
  }

  /**
   * Compute a nice step size that will result in approximately the requested 
   * number of labels. 
   * 
   * @param start 
   * @param end
   * @param approxNumLabels
   * @return a nice step size
   */
  private double computeStep(final double start, final double end, final int approxNumLabels) {

    // handle the case where the start and end values are almost identical
    if (Math.abs(start - end) < DOUBLE_DELTA) {
      return 0;
    }

    double step = roundUp(Math.abs(start - end) / approxNumLabels);

    // handle the case where the labels are decreasing. eg 0, -1, -2
    if (start > end) {
      return step * -1;
    }

    return step;
  }

  /**
   * Compute a reasonable set of labels for a data interval and number of labels.
   * 
   * @param start
   *                intial zvalue
   * @param end
   *                final zvalue
   * @param approxNumLabels
   *                desired number of labels
   * @return collection containing {start value, end value, increment}
   */
  @Deprecated
  public static List<Double> getLabels(final double start, final double end, final int approxNumLabels) {
    List<Double> labels = new ArrayList<Double>();
    double[] labelParams = computeLabels(start, end, approxNumLabels);
    // when the start > end the zinc will be negative so it will still work
    int numLabels = 1 + (int) ((labelParams[1] - labelParams[0]) / labelParams[2]);
    // we want the range to be inclusive but we don't want to blow up when
    // looping for the case where the min and max are the same. So we loop on
    // numLabels not on the zvalues.
    for (int i = 0; i < numLabels; i++) {
      double z = labelParams[0] + i * labelParams[2];
      labels.add(z);
    }
    return labels;
  }

  /**
   * Compute a reasonable number of labels for a data range.
   * 
   * @param start
   *                intial zvalue
   * @param end
   *                final zvalue
   * @param approxNumLabels
   *                desired number of labels
   * @return double[] array containing {start value, end value, increment}
   */
  @Deprecated
  public static double[] computeLabels(final double start, final double end, final int approxNumLabels) {
    assert approxNumLabels > 0 : "Number of labels must be greater than zero.";
    // this handles the case where a horizon is totally flat.....
    if (Math.abs(start - end) < DOUBLE_DELTA) {
      return new double[] { start, start, 0 };
    }
    double s = start;
    double e = end;
    boolean switched = false;
    if (s > e) {
      switched = true;
      double tmp = s;
      s = e;
      e = tmp;
    }
    double xStep = roundUp(Math.abs(s - e) / approxNumLabels);
    // Compute x starting point so it is a multiple of xStep.
    double xStart = xStep * Math.ceil(s / xStep);
    double xEnd = xStep * Math.floor(e / xStep);
    // ServiceProvider.getLoggingService().getLogger(getClass()).debug(start + " " + end + " " + approxNumLabels);
    // ServiceProvider.getLoggingService().getLogger(getClass()).debug(xStart + " " + xEnd + " " + xStep);
    if (switched) {
      return new double[] { xEnd, xStart, -1.0 * xStep };
    }
    return new double[] { xStart, xEnd, xStep };
  }

  /**
   * Given a number, round up to the nearest power of ten times 1, 2, or 5.
   * 
   * I tried rounding to 2.5 but then I couldn't get the logic
   * to work for computing the length of the labels past the decimal place. 
   * 
   * Note: The argument must be strictly positive.
   */
  private static double roundUp(final double val) {
    if (val <= 0) {
      throw new RuntimeException("Cannot round up a number that is zero");
    }

    int exponent = (int) Math.floor(Math.log10(val));
    double rval = val * Math.pow(10, -exponent);
    if (rval >= 5.0) {
      rval = 10.0;
    } else if (rval >= 2.0) {
      rval = 5.0;
    } else if (rval > 1.0) {
      rval = 2.0;
    }
    rval *= Math.pow(10, exponent);
    return rval;
  }

  /**
   * Compute a format string to display a number with a pretty label.
   * 
   * The format string in java does not seem to include a leading minus sign
   * in the total formatted width. 
   * 
   * If you pass in -0 it will not display as 0. You should use the other
   * methods in this class to get a reasonable set of numbers to work with. 
   */
  @Deprecated
  public static String getFormat(final double value) {
    String result = "%f";

    if (Math.abs(value) < DOUBLE_DELTA) {
      // the number is almost exactly zero
      result = "%1.0f";
    } else {
      int iWidth = integerWidth(value);
      int fWidth = fractionalWidth(value);
      result = "%" + (iWidth + fWidth) + "." + fWidth + "f";
    }

    return result;
  }

  /**
   * Compute the number of digits required to display the whole
   * part of a number. eg for val = 95.1 return 2 for the 95 part. 
   * @param val
   * @return number of digits
   */
  private static int integerWidth(final double value) {
    if (Math.abs(value) >= 1) {
      return 1 + (int) Math.log10(Math.abs(value));
    }
    return 1;
  }

  /**
   * Compute the number of digits required to display the fractional
   * part of a number. eg for val = 95.1 return 1 for the .1 part. 
   * 
   * This presumes that the steps are 1, 2 and 5 and it would get
   * the wrong answer if the step were not integer like 2.5
   * 
   * @param val
   * @return number of digits
   */
  private static int fractionalWidth(final double value) {

    // remove the integer part, handle negative numbers. 
    double fraction = Math.abs(value - (long) value);

    if (fraction <= DOUBLE_DELTA) {
      return 0;
    }

    // bump the number away from 1,.1, .01 etc which has a confusing log value. 
    fraction += DOUBLE_DELTA;

    return (int) (1 + -1 * Math.log10(fraction));
  }
}
