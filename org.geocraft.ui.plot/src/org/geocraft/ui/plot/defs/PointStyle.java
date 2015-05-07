/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.defs;


/**
 * Enumeration for the point styles.
 * Options include CIRCLE, SQUARE, TRIANGLE, DIAMOND, etc.
 * These are used in point properties for points and shapes.
 */
public enum PointStyle {
  /** No point symbol. */
  NONE("None"),
  /** An 'X' symbol. */
  X("X"),
  /** A horizontal tick symbol. */
  HORIZONTAL_TICK("-"),
  /** A vertical tick symbol. */
  VERTICAL_TICK("|"),
  /** An '+' symbol. */
  CROSS("Cross"),
  /** A circle symbol. */
  CIRCLE("Circle"),
  /** A square symbol. */
  SQUARE("Square"),
  /** A diamond symbol. */
  DIAMOND("Diamond"),
  /** A triangle symbol. */
  TRIANGLE("Triangle"),
  /** A filled circle symbol. */
  FILLED_CIRCLE("Filled Circle"),
  /** A filled square symbol. */
  FILLED_SQUARE("Filled Square"),
  /** A filled diamond symbol. */
  FILLED_DIAMOND("Filled Diamond"),
  /** A filled triangle symbol. */
  FILLED_TRIANGLE("Filled Triangle");

  /** The name of the point style. */
  private String _name;

  /**
   * Constructs a point style.
   * @param name the name of the point style.
   */
  PointStyle(final String name) {
    _name = name;
  }

  /**
   * Returns the name of the point style.
   * @return the name of the point style.
   */
  public String getName() {
    return _name;
  }

  @Override
  public String toString() {
    return getName();
  }

  public static PointStyle lookup(final String symbol) {
    for (PointStyle style : values()) {
      if (style.getName().equals(symbol)) {
        return style;
      }
    }
    return null;
  }
}
