/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.defs;


/**
 * Enumeration for orientations.
 * Options include only horizontal and vertical.
 * These are used by plot axes and labels.
 */
public enum Orientation {
  /** Horizontal orientation. */
  HORIZONTAL("Horizontal"),
  /** Vertical orientation. */
  VERTICAL("Vertical");

  /** The name of the orientation. */
  private String _name;

  /**
   * Constructs a orientation.
   * @param name the name of the orientation.
   */
  Orientation(final String name) {
    _name = name;
  }

  /**
   * Returns the name of the orientation.
   * @return the name of the orientation.
   */
  public String getName() {
    return _name;
  }

  @Override
  public String toString() {
    return getName();
  }
}
