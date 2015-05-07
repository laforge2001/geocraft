/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.math;


/**
 * Defines the float version of the complex value class.
 */
public class ComplexFloat {

  // TODO this class is not immutable. Leave in internal package until it is cleaned up.
  /** The real part. */
  public float _real;

  /** The imaginary part. */
  public float _imag;

  /**
   * Constructs an instance of ComplexFloat, with specified real/imaginary values.
   * 
   * @param real the real part.
   * @param imag the imaginary part.
   */
  public ComplexFloat(float real, float imag) {

    _real = real;
    _imag = imag;
  }

  public String toString() {
    return "(" + _real + "," + _imag + ")";
  }

  /**
   * Gets the real part.
   * 
   * @return the real part.
   */
  public float getReal() {
    return _real;
  }

  /**
   * Gets the imaginary part.
   * 
   * @return the imaginary part.
   */
  public float getImag() {
    return _imag;
  }
}
