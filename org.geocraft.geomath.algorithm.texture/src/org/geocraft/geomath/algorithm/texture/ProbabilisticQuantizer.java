/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.texture;


import java.util.Arrays;

import org.geocraft.core.common.math.MathUtil;


/**
 * Partitions the data so that there is an approximately number of data points 
 * in each bin. Any rounding error in the bin sizes gets put into the final bin. 
 */
public class ProbabilisticQuantizer {

  float[] _minBinValues;

  /**
   * 
   * @param values - the data to be quantized
   * @param nullValue - null values to be ignored
   * @param numBins - number of bins to create
   */
  public ProbabilisticQuantizer(final float[] values, final float nullValue, final int numBins) {
    _minBinValues = new float[numBins + 1];
    init(values, nullValue, numBins);
  }

  /**
   * Lookup the corresponding bin for this value. 
   * @param value data value to look up
   * @return the bin 0 <= bin < numBins
   */
  public int getBin(final float value) {

    for (int i = 0; i < _minBinValues.length - 1; i++) {
      if (value >= _minBinValues[i] && value <= _minBinValues[i + 1]) {
        return i;
      }
    }

    throw new RuntimeException("Could not quantize " + value + " " + _minBinValues[0] + " "
        + _minBinValues[_minBinValues.length - 1]);
  }

  private void init(final float[] values, final float nullValue, final int numBins) {

    float[] noNulls = MathUtil.removeNulls(values, nullValue);

    Arrays.sort(noNulls);

    // this rounds down so that there may be slightly more points in the top bin. 
    int valsPerBin = noNulls.length / numBins;

    for (int bin = 0; bin < numBins; bin++) {
      int index = bin * valsPerBin;
      _minBinValues[bin] = noNulls[index];
    }

    _minBinValues[numBins] = noNulls[noNulls.length - 1];

    noNulls = null;
  }

  public static void main(final String[] args) {
    float[] vals = new float[1000];

    for (int i = 0; i < 1000; i++) {
      vals[i] = (float) (10 * Math.random());
    }

    ProbabilisticQuantizer q = new ProbabilisticQuantizer(vals, -999, 5);

    System.out.println(q.getBin(1));
    System.out.println(q.getBin(2));
    System.out.println(q.getBin(3));
    System.out.println(q.getBin(5));
    System.out.println(q.getBin(9));
  }
}
