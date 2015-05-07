/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.horizon.mediansmooth;


import org.geocraft.algorithm.StandaloneAlgorithm;


/**
 * The abstract base class for the 2D and 3D implementations of the median smooth algorithm.
 */
public abstract class MedianSmooth extends StandaloneAlgorithm {

  public enum FilterType {
    COLS_ROWS("Filter Based on Rows/Columns"),
    SQUARE_FILTER("Square Filter Based on # of Points");

    private String _label;

    FilterType(final String label) {
      _label = label;
    }

    @Override
    public String toString() {
      return _label;
    }
  }

  public enum InterpolateOption {
    NON_NULLS_ONLY("Non-Null Values Only"),
    NULLS_ONLY("Null Values Only"),
    ALL_VALUES("All Values");

    private String _label;

    InterpolateOption(final String label) {
      _label = label;
    }

    @Override
    public String toString() {
      return _label;
    }
  }
}
