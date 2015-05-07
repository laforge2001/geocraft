/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.defs;


/**
 * Enumeration for axis scales.
 * Options include only linear and logarithmic.
 */
public enum AxisScale {
  /** Linear scale. */
  LINEAR("Linear"),
  /** Log scale. */
  LOG("Log");

  /** The name of the axis scale. */
  private String _name;

  /**
   * Constructs an axis scale.
   * @param name the name of the axis scale.
   */
  AxisScale(final String name) {
    _name = name;
  }

  /**
   * Returns the name of the axis scale.
   * @return the name of the axis scale.
   */
  public String getName() {
    return _name;
  }

  @Override
  public String toString() {
    return getName();
  }
}
