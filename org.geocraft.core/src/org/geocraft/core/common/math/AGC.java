/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.math;


public class AGC {

  /** Enumeration of the AGC types. */
  public enum Type {
    BOXCAR("Boxcar (rectangular) window"),
    GAUSSIAN("Gaussian window");

    private String _text;

    Type(final String text) {
      _text = text;
    }

    public String getName() {
      return _text;
    }

    @Override
    public String toString() {
      return _text;
    }

    public static Type lookup(final String name) {
      if (name == null) {
        return null;
      }
      for (Type type : Type.values()) {
        if (type.getName().equals(name)) {
          return type;
        }
      }
      return null;
    }

  }

  private final Type _type;

  boolean _isFirstTrace = true;

  float[] _dataAGC;

  float[] _weights; // Gaussian window weights.

  float[] _dataSquared; // square of input data.

  float[] _weightedSum; // Weighted sum of squares of the data.

  public AGC(final Type type) {
    _type = type;
  }

  public void applyAGC(final float[] data, final int iWindowLength, final int numSamples) {
    if (_type.equals(Type.BOXCAR)) {
      applyBoxcarAGC(data, iWindowLength, numSamples);
    } else if (_type.equals(Type.GAUSSIAN)) {
      applyGaussianAGC(data, iWindowLength, numSamples);
    }
  }

  protected void applyBoxcarAGC(final float[] data, final int iWindowLength, final int numSamples) {
    int i;
    double val; // Use double to reduce rounding error.
    double sum; // Use double to reduce rounding error.
    int nwin;
    float rms;

    if (_isFirstTrace) {
      _isFirstTrace = false;
      _dataAGC = new float[numSamples];
    }

    if (iWindowLength < 0 || iWindowLength >= numSamples) {
      return;
    }

    // Compute initial window for first datum.
    sum = 0.0;
    for (i = 0; i < iWindowLength + 1; ++i) {
      val = data[i];
      sum += val * val;
    }
    nwin = iWindowLength + 1;
    rms = (float) sum / nwin;
    _dataAGC[0] = (float) (rms <= 0.0 ? 0.0 : data[0] / Math.sqrt(rms));

    for (i = 1; i < numSamples; ++i) {
      if (i + iWindowLength < numSamples) {
        val = data[i + iWindowLength];
        sum += val * val;
        nwin++;
      }
      if (i - iWindowLength > 0) {
        val = data[i - iWindowLength - 1];
        sum -= val * val;
        nwin--;
      }
      rms = (float) sum / nwin;
      _dataAGC[i] = (float) (rms <= 0.0 ? 0.0 : data[i] / Math.sqrt(rms));
    }

    System.arraycopy(_dataAGC, 0, data, 0, numSamples);

    return;
  }

  /** 
   * Automatic Gain Control--gaussian window,
   *
   * It is slower than boxcar window, which use moving average algorithm
   */
  protected void applyGaussianAGC(final float[] data, final int iWindowLength, final int numSamples) {

    if (_isFirstTrace) {
      _isFirstTrace = false;

      // Allocate room for agc data.
      _dataAGC = new float[numSamples];

      // Allocate and compute Gaussian window weights.
      _weights = new float[iWindowLength];
      float u = (float) (3.8090232 / iWindowLength); // Related to reciprocal of std dev.
      float uSquared = u * u;
      {
        int i;
        float floati;

        for (i = 1; i < iWindowLength; ++i) {
          floati = i;
          _weights[i] = (float) Math.exp(-(uSquared * floati * floati));
        }
      }

      // Allocate sum of squares and weighted sum of squares.
      _dataSquared = new float[numSamples];
      _weightedSum = new float[numSamples];
    }

    if (iWindowLength < 0 || iWindowLength >= numSamples) {
      return;
    }

    // Apply AGC on the trace data.
    {
      float val;
      float wtmp;
      float stmp;

      // Put sum of squares of data in dataSquared and initialize weightedSum to dataSquared to get center point set.
      for (int i = 0; i < numSamples; ++i) {
        val = data[i];
        _weightedSum[i] = _dataSquared[i] = val * val;
      }

      // Compute weighted sum; use symmetry of Gaussian.
      for (int j = 1; j < iWindowLength; ++j) {
        wtmp = _weights[j];
        for (int i = j; i < numSamples; ++i) {
          _weightedSum[i] += wtmp * _dataSquared[i - j];
        }
        int k = numSamples - j;
        for (int i = 0; i < k; ++i) {
          _weightedSum[i] += wtmp * _dataSquared[i + j];
        }
      }

      for (int i = 0; i < numSamples; ++i) {
        stmp = _weightedSum[i];
        if (Float.compare(stmp, 0f) == 0) {
          _dataAGC[i] = 0f;
        } else {
          _dataAGC[i] = (float) (data[i] / Math.sqrt(stmp));
        }
      }

      System.arraycopy(_dataAGC, 0, data, 0, numSamples);
    }

    return;
  }
}
