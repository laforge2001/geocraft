/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.math;


public class Clip {

  /** Enumeration for the difference clipping methods. */
  public enum ClipType {
    /** Replaces clipped values with minimum/maximum limits. */
    REPLACE_WITH_LIMITS("Replace with Clip Limits"),
    /** Replaces clipped values with nulls. */
    REPLACE_WITH_NULLS("Replace with Nulls"),
    /** Replaces clipped values with a constant value. */
    REPLACE_WITH_CONSTANT("Replace with Constant");

    private final String _text;

    private ClipType(final String text) {
      _text = text;
    }

    @Override
    public String toString() {
      return _text;
    }
  }

  /**
   * Clips an array of float values.
   * 
   * @param inputData the array of input values.
   * @param nullValue the value representing "null".
   * @param clipType the type of clipping to perform.
   * @param clipMin the clipping minimum value.
   * @param clipMax the clipping maximum value.
   * @param clipValue the clipping constant value.
   * @return an array of clipped values.
   */
  public static float[] clipData(final float[] inputData, final float nullValue, final ClipType clipType,
      final float clipMin, final float clipMax, final float clipValue) {
    int numSamples = inputData.length;
    float[] outputData = new float[numSamples];

    // Loop over the # of values.
    for (int k = 0; k < numSamples; k++) {

      // If the input value if non-null, then continue to the next test.
      if (!MathUtil.isEqual(inputData[k], nullValue)) {
        // Get the value and clip it if necessary.
        float value = inputData[k];
        outputData[k] = value;
        if (value < clipMin) {
          switch (clipType) {
            case REPLACE_WITH_LIMITS:
              outputData[k] = clipMin;
              break;
            case REPLACE_WITH_NULLS:
              outputData[k] = nullValue;
              break;
            case REPLACE_WITH_CONSTANT:
              outputData[k] = clipValue;
              break;
          }
        } else if (value > clipMax) {
          switch (clipType) {
            case REPLACE_WITH_LIMITS:
              outputData[k] = clipMax;
              break;
            case REPLACE_WITH_NULLS:
              outputData[k] = nullValue;
              break;
            case REPLACE_WITH_CONSTANT:
              outputData[k] = clipValue;
              break;
          }
        }
      } else {
        // The input value is null, then set the output value to null.
        outputData[k] = nullValue;
      }
    }
    return outputData;
  }
}
