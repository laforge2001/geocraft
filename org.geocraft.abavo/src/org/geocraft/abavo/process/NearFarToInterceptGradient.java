/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.process;


/**
 * Transforms a pair of near/far traces to intercept/gradient traces,
 * based on the near/far angles. Also optionally scales the far and
 * gradient traces.
 */
public class NearFarToInterceptGradient {

  /** The far angle. */
  private final float _farAngle;

  /** The near angle. */
  private final float _nearAngle;

  /** The near scalar. */
  private final float _nearScalar;

  /** The far scalar. */
  private final float _farScalar;

  /** Constant for sin^2 of the near angle. */
  private final double _sin2near;

  /** Constant for sin^2 of the far angle. */
  private final double _sin2far;

  /** Constant for sin(far)^2 - sin(near)^2 */
  private final double _deltaS;

  /**
   * The default constructor (without scaling).
   * @param nearAngle the near angle.
   * @param farAngle the far angle.
   */
  public NearFarToInterceptGradient(final float nearAngle, final float farAngle) {
    this(nearAngle, farAngle, 1, 1);
  }

  /**
   * The default constructor (with scaling).
   * @param nearAngle the near angle.
   * @param farAngle the far angle.
   * @param farScalar the far scalar.
   */
  public NearFarToInterceptGradient(final float nearAngle, final float farAngle, final float nearScalar, final float farScalar) {
    _nearAngle = nearAngle;
    _farAngle = farAngle;
    _sin2near = Math.pow(Math.sin(_nearAngle * Math.PI / 180), 2);
    _sin2far = Math.pow(Math.sin(_farAngle * Math.PI / 180), 2);
    _deltaS = _sin2far - _sin2near;
    _nearScalar = nearScalar;
    _farScalar = farScalar;
  }

  /**
   * Converts arrays of near/far data to intercept/gradient data.
   * @param numSamples the number of sample to convert.
   * @param dataA the array of near values to covert.
   * @param fndxA the first (starting) index for the near values.
   * @param dataB the array of far values to covert.
   * @param fndxB the first (starting) index for the far values.
   */
  public void process(final int numSamples, final float[] dataA, final int fndxA, final float[] dataB, final int fndxB) {
    for (int k = 0; k < numSamples; k++) {
      float near = dataA[fndxA + k] * _nearScalar;
      float far = dataB[fndxB + k] * _farScalar;
      float intercept = (float) ((near * _sin2far - far * _sin2near) / _deltaS);
      float gradient = (float) ((far - near) / _deltaS);
      dataA[fndxA + k] = intercept;
      dataB[fndxB + k] = gradient;
    }
  }
}
