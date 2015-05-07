/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.well;


public class WellUtil {

  /**
   * Looks up a value in a target array based on a value from a source array.
   * Linear interpolation is done if the source value is within the first and last points.
   * If outside the bounds, then an extrapolation is done.
   * 
   * @param sourceValue
   * @param sourceData
   * @param targetData
   * @return
   */
  public static float lookupValue(final float sourceValue, final float[] sourceData, final String sourceName,
      final float[] targetData, final String targetName) {
    // Validate the input arrays.
    if (sourceData.length < 2) {
      throw new RuntimeException("Could not convert " + sourceName + " to " + targetName + ". The " + sourceName
          + " array must contain at least 2 points.");
    }
    if (targetData.length < 2) {
      throw new RuntimeException("Could not convert " + sourceName + " to " + targetName + ". The " + targetName
          + " array must contain at least 2 points.");
    }
    if (sourceData.length != targetData.length) {
      throw new RuntimeException("Could not convert " + sourceName + " to " + targetName + ". The " + sourceName
          + " and " + targetName + " arrays are not the same length.");
    }

    int index;
    for (index = 0; index < sourceData.length; index++) {
      if (sourceData[index] >= sourceValue) {
        break;
      }
    }

    float targetValue = 0;
    if (index == 0) {
      // Extrapolate back from the first two points.
      targetValue = (sourceValue - sourceData[index]) / (sourceData[index + 1] - sourceData[index])
          * (targetData[index + 1] - targetData[index]) + targetData[index];
    } else if (index == sourceData.length && sourceData[index - 1] < sourceValue) {
      // Extrapolate forward from the last two points.
      float ratio = (targetData[index - 1] - targetData[index - 2]) / (sourceData[index - 1] - sourceData[index - 2]);
      targetValue = targetData[index - 1] + (sourceValue - sourceData[index - 1]) * ratio;
    } else {
      // Linear interpolate between the points.
      targetValue = (sourceValue - sourceData[index - 1]) / (sourceData[index] - sourceData[index - 1])
          * (targetData[index] - targetData[index - 1]) + targetData[index - 1];
    }
    return targetValue;
  }

}
