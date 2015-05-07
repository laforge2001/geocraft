/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.defs;


/**
 * Enumeration for corner placements.
 * Options include top-left, top-right, bottom-left, bottom-right.
 * These are used to define placement the corners on a plot
 */
public enum CornerPlacement {
  /** The top-left side. */
  TOP_LEFT("Top Left"),
  /** The top-right side. */
  TOP_RIGHT("Top Right"),
  /** The bottom-left side. */
  BOTTOM_LEFT("Bottom Left"),
  /** The bottom-right side. */
  BOTTOM_RIGHT("Bottom Right");

  /** The name of the corner placement. */
  private String _name;

  /**
   * Constructs a corner placement.
   * @param name the name of the corner placement.
   */
  CornerPlacement(final String name) {
    _name = name;
  }

  /**
   * Returns the name of the corner placement.
   * @return the name of the corner placement.
   */
  public String getName() {
    return _name;
  }

  @Override
  public String toString() {
    return getName();
  }
}
