/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.texture;


import org.geocraft.core.common.math.MathUtil;


/**
 * Grey-Level Co-occurrence Matrix (GLCM).
 * 
 * References: http://www.fp.ucalgary.ca/mhallbey/tutorial.htm
 * 
 */
public class GLCM2d {

  /** Optional classification - use when the GLCM is being used for training a neural net. */
  String _className;

  /** The subset of the data used to compute the GLCM - 0 < value < _numLevels. */
  int[][] _data;

  /** The number of data bins in the quantized data - controls size of GLCM. */
  int _numLevels;

  /** The offsets used to compute the GLCM from the quantized data. */
  int[] _offsets;

  /** The normalized representation of the GLCM. */
  float[][] _glcm;

  /** The number of data pairs that contributed to the GLCM construction. */
  int _numHits = 0;

  /**
   * The constructor expects data that has already been normalized so that it has 
   * a range of zero < value < numLevels. 
   * 
   * @param numLevels - the dimensions of the square GLCM 
   * @param data - scaled data to compute the attributes from
   * @param offsets - directions to use to construct the GLCM
   * @param className - an optional tag describing the classification of this data
   */
  public GLCM2d(final int numLevels, final int[][] data, final int[] offsets, final String className) {
    _numLevels = numLevels;
    _data = data;
    _offsets = offsets;
    _className = className;

    _glcm = new float[_numLevels][_numLevels];

    init();
    normalizeGLCM();
  }

  /**
   * Access the type of the data that corresponds to this GLCM, 
   * useful when training a neural network. 
   * 
   * @return text description of this data eg 'sandstones'. 
   */
  public String getClassName() {
    return _className;
  }

  /**
   * Convenience method to access all of the attributes. 
   * 
   * @return
   */
  public double[] getVector() {
    return new double[] { getEnergy(), getEntropy(), getHomogeneity(), getInertia() };
  }

  /**
   * Sanity check to ensure that things are working. 
   * 
   * @return
   */
  public int getNumHits() {
    return _numHits;
  }

  /**
   * A measure of the overall smoothness of the image.
   * 
   * @return
   */
  public float getHomogeneity() {
    float result = 0;
    for (int i = 0; i < _numLevels; i++) {
      for (int j = 0; j < _numLevels; j++) {
        int diff = i - j;
        result += _glcm[i][j] / (1 + diff * diff);
      }
    }
    return result;
  }

  /**
   * Marfurt calls this 'contrast' - a measure of the amount of local variation.
   * 
   * @return
   */
  public float getInertia() {
    float result = 0;
    for (int i = 0; i < _numLevels; i++) {
      for (int j = 0; j < _numLevels; j++) {
        int diff = i - j;
        result += _glcm[i][j] * diff * diff;
      }
    }
    // return result;
    // commented out the divisor because it just seems to make the numbers tiny.
    return (float) (result / Math.pow(_numLevels - 1, 2));
  }

  /**
   * A measure of the disorder in the image.
   * 
   * @return
   */
  public float getEntropy() {
    float result = 0;
    for (int i = 0; i < _numLevels; i++) {
      for (int j = 0; j < _numLevels; j++) {
        double tmp = Math.log(_glcm[i][j]);
        if (Double.isInfinite(tmp)) {
          tmp = 0;
        }
        result += _glcm[i][j] * tmp;
      }
    }
    return (float) (-result / (2. * Math.log(_numLevels)));
  }

  /**
   * A measure of textural uniformity.
   * 
   * @return
   */
  public float getEnergy() {
    float result = 0;
    for (int i = 0; i < _numLevels; i++) {
      for (int j = 0; j < _numLevels; j++) {
        result += _glcm[i][j] * _glcm[i][j];
      }
    }
    return result;
  }

  /**
   * http://www.soi.city.ac.uk/~jwo/phd/
   * @return
   */
  public float getAsymmetry() {
    float result = 0;
    for (int i = 0; i < _numLevels; i++) {
      for (int j = 0; j < _numLevels; j++) {
        result += (_glcm[i][j] - _glcm[j][i]) * (_glcm[i][j] - _glcm[j][i]);
      }
    }
    return result;
  }

  private void init() {
    for (int row = 0; row < _data.length; row++) {
      for (int col = 0; col < _data[0].length; col++) {
        for (int off = 0; off < _offsets.length; off = off + 2) {
          compute(row, col, row + _offsets[off], col + _offsets[off + 1]);
        }
      }
    }
  }

  private void compute(final int row1, final int col1, final int row2, final int col2) {
    if (row2 < 0 || row2 >= _data.length || col2 < 0 || col2 >= _data[0].length) {
      return;
    }
    int val1 = _data[row1][col1];
    int val2 = _data[row2][col2];
    if (val1 != Integer.MAX_VALUE && val2 != Integer.MAX_VALUE) {
      _glcm[val1][val2]++;
      _numHits++;
    }
  }

  public void dump() {

    System.out.format("%10.3f %10.3f %10.3f %10.3f\n", getEnergy(), getEntropy(), getHomogeneity(), getInertia());
    System.out.println("Num hits = " + _numHits);

    for (int[] element : _data) {
      for (int col = 0; col < _data[0].length; col++) {
        System.out.format("%4d ", element[col]);
      }
      System.out.println();
    }

    System.out.println();

    for (int row = 0; row < _numLevels; row++) {
      for (int col = 0; col < _numLevels; col++) {
        System.out.format("%6.4f ", _glcm[row][col]);
      }
      System.out.println();
    }
  }

  @Override
  public String toString() {
    return String.format("%10.8f,%10.8f,%10.8f,%10.8f", getEnergy(), getEntropy(), getHomogeneity(), getInertia());
  }

  private void normalizeGLCM() {
    int sum = 0;
    for (int i = 0; i < _numLevels; i++) {
      for (int j = 0; j < _numLevels; j++) {
        sum += _glcm[i][j];
      }
    }
    for (int i = 0; i < _numLevels; i++) {
      for (int j = 0; j < _numLevels; j++) {
        _glcm[i][j] /= sum;
      }
    }
  }

  /**
   * The answer should be a scaled version of this ....
   * 
   * 1 2 0 0 1 0 0 0 
   * 0 0 1 0 1 0 0 0 
   * 0 0 0 0 1 0 0 0 
   * 0 0 0 0 1 0 0 0 
   * 1 0 0 0 0 1 2 0 
   * 0 0 0 0 0 0 0 1 
   * 2 0 0 0 0 0 0 0 
   * 0 0 0 0 1 0 0 0
   * 
   * @param args
   */
  public static void main(final String[] args) {
    float[][] data = new float[][] { { 80, 50, 10, 20, 50 }, { 40, 50, 70, 10, 20 }, { 20, 30, 50, 70, 10 },
        { 10, 10, 50, 60, 80 } };
    int[] offsets = new int[] { 0, 1 };
    ProbabilisticQuantizer q = new ProbabilisticQuantizer(MathUtil.convert(data), 99, 8);

    int[][] quantized = new int[data.length][data[0].length];
    for (int i = 0; i < data.length; i++) {
      for (int j = 0; j < data[0].length; j++) {
        quantized[i][j] = q.getBin(data[i][j]);
      }
    }

    GLCM2d glcm = new GLCM2d(8, quantized, offsets, "bogus");
    glcm.dump();
  }
}
