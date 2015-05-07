/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.defs;


/**
 * Enumeration for axis directions.
 * Options include only start-to-end and end-to-start.
 */
public enum AxisDirection {
  /** Draw the axis from left to right. */
  LEFT_TO_RIGHT("Left to Right", true),
  /** Draw the axis from right to left. */
  RIGHT_TO_LEFT("Right to Left", false),
  /** Draw the axis from bottom to top. */
  BOTTOM_TO_TOP("Bottom to Top", true),
  /** Draw the axis from top to bottom. */
  TOP_TO_BOTTOM("Top to Bottom", false);

  /** The name of the axis direction. */
  private String _name;

  private boolean _isStartToEnd;

  /**
   * Constructs an axis direction.
   * @param name the name of the axis direction.
   */
  AxisDirection(final String name, final boolean isStartToEnd) {
    _name = name;
    _isStartToEnd = isStartToEnd;
  }

  /**
   * Returns the name of the axis direction.
   * @return the name of the axis direction.
   */
  public String getName() {
    return _name;
  }

  public boolean isStartToEnd() {
    return _isStartToEnd;
  }

  @Override
  public String toString() {
    return getName();
  }
}
