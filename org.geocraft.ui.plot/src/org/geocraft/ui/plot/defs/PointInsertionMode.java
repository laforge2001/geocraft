/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.defs;


/**
 * Enumeration for point insertion modes.
 * These include first, last, by x value, by y value, by z value.
 * These are used to specify where to insert new points
 * into shapes such as point groups, polylines, etc.
 */
public enum PointInsertionMode {

  FIRST("First"), LAST("Last"), BY_X_VALUE("By X"), BY_Y_VALUE("By Y"), BY_Z_VALUE("By Z");

  /** The name of the point insertion mode. */
  private String _name;

  /**
   * Constructs a point insertion mode.
   * @param name the name of the point insertion mode.
   */
  PointInsertionMode(final String name) {
    _name = name;
  }

  /**
   * Returns the name of the point insertion mode.
   * @return the name of the point insertion mode.
   */
  public String getName() {
    return _name;
  }

  @Override
  public String toString() {
    return getName();
  }
}
