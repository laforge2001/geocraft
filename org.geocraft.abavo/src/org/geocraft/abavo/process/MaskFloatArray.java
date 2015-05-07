/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.process;


import org.geocraft.abavo.defs.ABavoDataMode;


public class MaskFloatArray {

  private static final int _stepIncreasing = 1;

  private static final int _stepDecreasing = -1;

  private static final int _stepConstant = 0;

  private static final int _signPositive = 1;

  private static final int _signNegative = -1;

  private static final int _signZero = 0;

  public static void process(final float[] samples, ABavoDataMode dataMode) {
    // If mask is 'All', just return.
    if (dataMode.equals(ABavoDataMode.ALL_DATA)) {
      return;
    }

    float[] mask = new float[0];

    if (dataMode.equals(ABavoDataMode.PEAKS_AND_TROUGHS)) {
      mask = maskPeaksAndTroughs(samples);
    } else {
      throw new RuntimeException("Invalid mask operation.");
    }

    // Copy the mask array to the samples array.
    System.arraycopy(mask, 0, samples, 0, mask.length);
  }

  private static float[] maskPeaksAndTroughs(final float[] samples) {
    int numSamples = samples.length;
    int j;
    int k;
    int ndxPrevInc;
    int ndxPrevDec;
    int step;
    int stepPrev;
    int sign;
    int signPrev;
    float diff;
    float[] mask = new float[numSamples];
    for (int i = 0; i < numSamples; i++) {
      mask[i] = Float.NaN;
    }

    ndxPrevInc = 0;
    ndxPrevDec = 0;
    stepPrev = _stepConstant; // assume plateau for 1st sample
    signPrev = Math.round(Math.signum(samples[0]));
    for (k = 1; k < numSamples; k++) {

      diff = samples[k] - samples[k - 1];
      sign = Math.round(Math.signum(samples[k]));
      step = Math.round(Math.signum(diff));
      switch (step) {

        case _stepConstant: // current plateau - continue

          // do nothing.
          break;

        case _stepDecreasing: // current decreasing - possible peak,

          // check previous
          if (stepPrev == _stepIncreasing) { // previous increasing -

            // possible peak, check
            // sign
            if (signPrev == _signPositive) { // positive peak found,

              // backfill
              for (j = ndxPrevInc; j < k; j++) {
                mask[j] = samples[j];
              }
            }
          }
          ndxPrevDec = k;
          stepPrev = step;
          signPrev = sign;

          break;

        case _stepIncreasing: // current increasing - possible trough,

          // check previous
          if (stepPrev == _stepDecreasing) { // previous decreasing -

            // possible trough, check
            // sign
            if (signPrev == _signNegative) { // negative trough

              // found, backfill
              for (j = ndxPrevDec; j < k; j++) {
                mask[j] = samples[j];
              }
            }
          }
          ndxPrevInc = k;
          stepPrev = step;
          signPrev = sign;

          break;

        default:
          throw new RuntimeException("Invalid step.");
      }
    }
    return mask;
  }

  public static int[] maskPeaksAndTroughsBlock(final float[] samples) {
    int numSamples = samples.length;
    int k;
    int step;
    int sign;
    int idZero = 0;
    int idPeak = 1;
    int idTrough = -1;
    float diff;
    int[] mask = new int[numSamples];
    for (int i = 0; i < numSamples; i++) {
      mask[i] = idZero;
    }

    int id = 0;
    int stepPrev = _stepConstant; // Assume plateau for 1st sample
    int signPrev = _signZero;
    if (samples[0] > 0) {
      signPrev = _signPositive;
      id = idPeak;
    }
    if (samples[0] < 0) {
      signPrev = _signNegative;
      id = idTrough;
    }
    for (k = 1; k < numSamples; k++) {

      diff = samples[k] - samples[k - 1];
      sign = Math.round(Math.signum(samples[k]));
      step = Math.round(Math.signum(diff));
      switch (sign) {
        case _signZero: // zero.
          id = idZero;
          break;
        case _signPositive: // peak.
          if (signPrev != _signPositive) { // new peak.
            idPeak++;
            id = idPeak;
          } else {
            if (step == _stepConstant) { // plateau.
              id = idPeak;
            } else if (step == _stepIncreasing) { // peak leading

              // edge.
              if (stepPrev == _stepDecreasing) { // new peak.
                idPeak++;
                id = idPeak;
              } else { // continue.
                id = idPeak;
              }
            } else if (step == _stepDecreasing) { // peak trailing

              // edge.
              id = idPeak;
            }
          }

          break;

        case _signNegative: // trough.
          if (signPrev != _signNegative) { // new trough.
            idTrough--;
            id = idTrough;
          } else {

            if (step == _stepConstant) { // plateau.
              id = idTrough;
            } else if (step == _stepDecreasing) { // trough leading

              // edge.
              if (stepPrev == _stepIncreasing) { // new trough.
                idTrough--;
                id = idTrough;
              } else { // continue.
                id = idTrough;
              }
            } else if (step == _stepDecreasing) { // trough trailing

              // edge.
              id = idTrough;
            }
          }

          break;

        default:
          throw new RuntimeException("Invalid sign.");
      }
      mask[k] = id;
      if (step != _stepConstant) {
        signPrev = sign;
        stepPrev = step;
      }
    }
    return mask;
  }
}
