/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.defs;


/**
 * Enumeration for axis placements.
 * Options include top, left, right, bottom.
 * These are used to define placement the axes on a plot.
 */
public enum AxisPlacement {
  /** The top side. */
  TOP("Top"),
  /** The left side. */
  LEFT("Left"),
  /** The right side. */
  RIGHT("Right"),
  /** The bottom side. */
  BOTTOM("Bottom");

  /** The name of the axis placement. */
  private String _name;

  /**
   * Constructs a axis placement.
   * @param name the name of the axis placement.
   */
  AxisPlacement(final String name) {
    _name = name;
  }

  /**
   * Returns the name of the axis placement.
   * @return the name of the axis placement.
   */
  public String getName() {
    return _name;
  }

  @Override
  public String toString() {
    return getName();
  }
}
