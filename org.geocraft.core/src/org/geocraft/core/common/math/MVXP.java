package org.geocraft.core.common.math;


import org.geocraft.core.model.datatypes.Trace;


/**
 * This defines a utility class of MVXP-related methods.
 */
public class MVXP {

  /**
   * Performs MVXP on a trace.
   * This method receives a trace, integrates it and if necessary removes the low frequency drift.
   * 
   * @param trace the trace to MVXP.
   * @return trace values the array of MVXP'd trace values.
   */
  public static float[] mvxpTrace(final Trace trace, final int zStartIndex, final int zEndIndex,
      final int windowLength, final float scaleFactor, final float clipFactor) {

    float[] outputValues = trace.getData();

    // Determine if user wants to use the clip factor.
    boolean useClipFactor = false;
    if (clipFactor > 0) {
      useClipFactor = true;
    }

    // Don't guess the first median.
    float zero = 0.0f;
    boolean useGuess = false;
    float guess = zero;

    // Ignore trace values that are zero
    float nullValue = zero;

    // No null traces are allowed.
    if (outputValues.length > 1) {
      int firstLive = firstLive(outputValues, zStartIndex, 0.0001);
      int lastLive = lastLive(outputValues, zEndIndex, 0.0001);

      // initialize array of median values
      int nVals = lastLive + 1;
      float[] medVals = new float[nVals];

      // determine the first window
      int i1 = firstLive;
      boolean startFlag = true;
      boolean endFlag = false;
      float medValLast = 0;
      int winMidLast = 0;

      // process windows until we are at the end of the trace
      while (i1 <= lastLive) {
        // determine window
        int winStart = i1;
        int winEnd = i1 + windowLength - 1;
        if (winEnd >= lastLive) {
          winEnd = lastLive;
          endFlag = true;
        }
        int winMid = (winEnd + winStart) / 2;

        // Determine the values in the current window
        int curWinLen = winEnd - winStart + 1;
        float[] wvals = new float[curWinLen];
        int iPntr2 = 0;
        for (int i2 = winStart; i2 <= winEnd; i2++) {
          wvals[iPntr2] = Math.abs(outputValues[i2]);
          iPntr2++;
        }

        // Determine the median value of the window
        // (The guess is the first guess at the median value)
        float medValue = median(wvals, nullValue, guess, useGuess);

        // If start window then fill in median values
        if (startFlag) {
          for (int i2 = winStart; i2 <= winMid; i2++) {
            medVals[i2] = medValue;
          }
          startFlag = false;
          // Otherwise perform a linear interpolation between medians
        } else {
          for (int i2 = winMidLast + 1; i2 < winMid; i2++) {
            medVals[i2] = medValLast + (medValue - medValLast) / (winMid - winMidLast) * (i2 - winMidLast);
          }
          medVals[winMid] = medValue;
        }

        // If last window then fill in median values
        if (endFlag) {
          for (int i2 = winMid + 1; i2 <= winEnd; i2++) {
            medVals[i2] = medValue;
          }
        }

        // Save current median value
        medValLast = medValue;
        winMidLast = winMid;

        // set the guess value for the next window
        useGuess = true;
        guess = medValue;

        // Set the Index for the next window
        i1 = winEnd + 1;
      }

      for (i1 = firstLive; i1 <= lastLive; i1++) {

        // the new value is now the (old trace value / median values)
        float newValue = 0;
        if (!MathUtil.isEqual(medVals[i1], 0)) {
          newValue = outputValues[i1] / medVals[i1] * scaleFactor;
        }

        // Clip the new value if it greater than the clip Factor
        if (useClipFactor) {
          if (newValue > clipFactor) {
            newValue = clipFactor;
          }
        }

        // Set the trace value to the new value.
        outputValues[i1] = newValue;
      }

      // Re-zero to the original mute positions, if necessary.
      for (i1 = 0; i1 < firstLive; i1++) {
        outputValues[i1] = zero;
      }

      for (i1 = lastLive + 1; i1 < outputValues.length; i1++) {
        outputValues[i1] = zero;
      }
    }

    return outputValues;
  }

  /**
   * Top mute picker.
   * Finds the index of the first "live" (greater than a threshold) value in an array.
   * 
   * @param values is an array of trace values.
   * @param indexStart index of the start time the user wanted
   * @param threshold provides cutoff value for the trace samples.
   * @return index of first value exceeding threshold, starting from the top.
   */
  private static int firstLive(final float[] values, final int indexStart, final double threshold) {
    int firstLive = 0;
    for (int i = indexStart; i < values.length; i++) {
      if (Math.abs(values[i]) > threshold) {
        firstLive = i;
        i = values.length + 1;
      }
    }
    return firstLive;
  }

  /**
   * Bottom mute picker.
   * Finds the index of the last "live" (greater than a threshold) value in an array.
   * 
   * @param values is an array of trace values.
   * @param indexEnd index of the end time the user wanted
   * @param threshold provides cutoff value for the trace samples.
   * @return index of first value exceeding threshold, starting from the bottom
   */
  private static int lastLive(final float[] values, final int indexEnd, final double threshold) {
    int lastIndx = values.length - 1;
    int lastLive = values.length - 1;
    if (indexEnd < lastIndx) {
      lastIndx = indexEnd;
      lastLive = indexEnd;
    }
    for (int i = lastIndx; i >= 0; i--) {
      if (Math.abs(values[i]) > threshold) {
        lastLive = i;
        i = -1;
      }
    }
    return lastLive;
  }

  public static float median(final float[] values, final float nullValue, final float guess, final boolean useGuess) {

    // Save valid values.
    float[] validVals = new float[values.length + 1];

    // the min and max values in the array
    // (Sum values in order to guess the median)
    float sum = 0;
    int nVals = 0;
    boolean startVal = true;
    float minValue = nullValue;
    float maxValue = nullValue;

    for (float value : values) {
      if (!Float.isNaN(value) && !MathUtil.isEqual(value, nullValue)) {
        if (startVal) {
          minValue = value;
          maxValue = value;
          startVal = false;
        } else {
          minValue = Math.min(minValue, value);
          maxValue = Math.max(maxValue, value);
        }
        sum += value;
        nVals++;
        validVals[nVals] = value;
      }
    }

    // When minValue=maxValue the answer is at hand
    if (MathUtil.isEqual(minValue, maxValue)) {
      return minValue;
    }

    // Initialize median value
    float medValue = minValue;

    // make a guess
    float guessVal = guess;
    if (useGuess) {
      guessVal = guess;
    } else {
      guessVal = sum / nVals;
    }

    float a1 = minValue;
    float a2 = maxValue;
    int n1 = 1;
    int n2 = nVals;
    //  Loop over points in array to isolate the median
    for (int iter = 1; iter <= nVals; iter++) {
      int nle = 0;
      float x1 = a1;
      float x2 = a2;

      // -------------------------------------------------------------------
      // Each time the guess is updated,
      // this loop evaluates every sample
      // in the array. If this happens
      // many times, it might be faster
      // to sort the array and pluck out
      // the median
      for (int j1 = 1; j1 <= nVals; j1++) {
        if (validVals[j1] <= guessVal) {
          nle = nle + 1;
          x1 = Math.max(x1, validVals[j1]);
        } else {
          x2 = Math.min(x2, validVals[j1]);
        }
      }
      // -------------------------------------------------------------------
      boolean endFlag = true;
      int nle2 = 2 * nle;
      if (nle2 < nVals) {
        a1 = x2;
        n1 = nle + 1;
        endFlag = false;
      } else if (nle2 > nVals + 1) {
        a2 = x1;
        n2 = nle;
        endFlag = false;
      } else if (nle2 == nVals) {
        medValue = (x1 + x2) * 0.5f;
      } else if (nle2 < nVals) {
        medValue = x2;
      } else {
        medValue = x1;
      }

      // if endFlag set then we're done.
      if (endFlag) {
        return medValue;
      }

      if (MathUtil.isEqual(a1, a2)) {
        medValue = a1;
        return medValue;
      }

      // We haven't found it yet so update the guess and cycle again
      guessVal = (float) (a1 + (a2 - a1) * Math.rint(nVals + 1 - 2 * n1) / Math.rint(2 * (n2 - n1)));

      // To avoid endless loop in case of numerical precision problem
      if (MathUtil.isEqual(guessVal, a2)) {
        medValue = guessVal;
        return medValue;
      }
    }

    //  We shouldn't get here except in case of severe precision problem
    medValue = guessVal;
    return medValue;
  }
}
