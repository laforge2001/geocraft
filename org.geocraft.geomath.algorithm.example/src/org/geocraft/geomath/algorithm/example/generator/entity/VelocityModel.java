/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.example.generator.entity;


/**
 * This class represents a simple model of instantaneous velocity increasing
 * linearly with depth. Vinst= v0 + kz.
 * <p>
 * It provides methods to convert from time to depth
 */
public class VelocityModel {

  /** The v0 parameter. */
  private double _v0;

  /** The k parameter. */
  private double _k;

  /**
   * Constructs a simple model of instantaneous velocity.
   * <p>
   * Vinst = v0 + kz;
   * 
   * @param v0 the v0 parameter.
   * @param k the k parameter.
   */
  public VelocityModel(double v0, double k) {
    _v0 = v0;
    _k = k;
  }

  /**
   * Compute the two-way-time (TWT) from the given depth (Z) value.
   * 
   * @param z the depth value.
   * @return the computed two-way time value.
   */
  public float getTwoWayTime(float z) {
    float twt = 0;
    twt = (float) (2 * Math.log(1 + _k * Math.abs(z) / _v0) / _k);
    return twt;
  }
}
