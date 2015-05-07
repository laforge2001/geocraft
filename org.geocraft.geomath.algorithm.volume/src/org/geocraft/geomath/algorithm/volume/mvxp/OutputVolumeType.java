/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.volume.mvxp;


/**
 * Enumeration of the available output volume types.
 */
public enum OutputVolumeType {
  /** Use the same type as the input volume. */
  SAME_AS_INPUT("Same as input"),
  /** An 8-bit integer volume. */
  INTEGER_08("8-bit integer"),
  /** A 16-bit integer volume. */
  INTEGER_16("16-bit integer"),
  /** A 32-bit floating point volume. */
  FLOAT_32("32-bit float");

  private String _name;

  OutputVolumeType(final String name) {
    _name = name;
  }

  @Override
  public String toString() {
    return _name;
  }
}
