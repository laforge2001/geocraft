/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.math;


/**
 * Implementation of the Douglas Peucker Algorithm vaguely inspired by discussion here:
 * http://www.softsurfer.com/Archive/algorithm_0205/algorithm_0205.htm#dot%20macros
 */
public class Decimate {

  /**
   * Decimates a set of points using a tolerance that is the same as the average
   * distance between each pair of neighboring points. I guessed this criteria and
   * it may not work on your data set. 
   * 
   * @param points array of points.
   * @return a decimated array of points. 
   */
  public static double[][] decimate(final double[][] points) {
    double sum = 0;
    for (int i = 0; i < points.length - 1; i++) {
      sum += Math.sqrt((points[i][0] - points[i + 1][0]) * (points[i][0] - points[i + 1][0]) + (points[i][1] - points[i + 1][1]) * (points[i][1] - points[i + 1][1]));
    }

    double tolerance = sum / points.length;
    return decimate(points, tolerance);
  }

  /**
   * Decimates a set of points using a user specified error tolerance. 
   * 
   * @param points array of points.
   * @param tolerance used to reject / accept points
   * @return a decimated array of points. 
   */
  public static double[][] decimate(final double[][] points, final double tolerance) {

    boolean[] mark = new boolean[points.length];

    // we will always want the two end points .... 
    mark[0] = true;
    mark[mark.length - 1] = true;

    // recursively add back the points that are the most significant
    simplify(points, tolerance, mark, 0, mark.length - 1);

    // how many points are there in the decimated array?
    int count = 0;
    for (boolean m : mark) {
      if (m) {
        count++;
      }
    }
    double[][] results = new double[count][2];

    // copy the decimated points into the results array .... 
    for (int i = 0, k = 0; i < mark.length; i++) {
      if (mark[i]) {
        results[k++] = points[i];
      }
    }

    return results;
  }

  private static void simplify(final double[][] points, final double tolerance, final boolean[] mark, final int start, final int end) {

    // find worst point 
    double maxDistance = -1e30;

    // if we don't find a worst point we will just reset the first point again which is harmless. 
    int index = 0;

    for (int i = start + 1; i < end; i++) {
      double distance = GeometryUtil.distancePointToLine(points[start][0], points[start][1], points[end][0], points[end][1], points[i][0], points[i][1]);
      if (distance > maxDistance) {
        index = i;
        maxDistance = distance;
      }
    }

    // if the maxDistance was larger than the tolerance then we are not done yet... 
    if (maxDistance > tolerance) {
      mark[index] = true;

      if (index > start + 1) {
        simplify(points, tolerance, mark, start, index);
      }

      if (index < end - 1) {
        simplify(points, tolerance, mark, index, end);
      }
    }

  }
}
