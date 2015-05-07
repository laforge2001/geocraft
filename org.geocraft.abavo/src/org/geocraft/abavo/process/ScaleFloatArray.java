/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.process;


/**
 * Scales an array of float values (in place).
 */
public class ScaleFloatArray {

  /** The scalar. */
  private final float _scalar;

  /**
   * The default constructor.
   * @param scalar the scalar.
   */
  public ScaleFloatArray(final float scalar) {
    _scalar = scalar;
  }

  /**
   * Gets the scalar value.
   * @return the scalar value.
   */
  public float getScalar() {
    return _scalar;
  }

  /**
   * Scales the values in the data array.
   * @param data the array of values to scale.
   */
  public void process(final float[] data) {
    process(data, 0, data.length - 1);
  }

  /**
   * Scales the values in the data array.
   * @param data the array of values to scale.
   * @param fndx the first (starting) index.
   * @param lndx the last (ending) index.
   */
  public void process(final float[] data, final int fndx, final int lndx) {
    if (_scalar == 1) {
      return;
    }
    for (int i = fndx; i <= lndx; i++) {
      data[i] *= _scalar;
    }
  }
}
