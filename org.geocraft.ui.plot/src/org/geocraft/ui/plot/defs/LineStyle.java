/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.defs;


import java.util.EnumSet;


/**
 * Enumeration for line styles.
 * Options include SOLID, DASHED and NONE.
 * These are used in line properties for lines, polylines, etc.
 */
public enum LineStyle {
  /** A solid line. */
  SOLID("Solid"),
  /** A dashed line. */
  DASHED("Dashed"),
  /** No line. */
  NONE("None");

  /** The name of the line style. */
  private String _name;

  /**
   * Constructs a line style.
   * @param name the name of the line style.
   */
  LineStyle(final String name) {
    _name = name;
  }

  /**
   * Returns the name of the line style.
   * @return the name of the line style.
   */
  public String getName() {
    return _name;
  }

  @Override
  public String toString() {
    return getName();
  }

  /**
   * Looks up a line style based on its name.
   * @param name the line style name.
   * @return the line style matching name, or <i>NONE</i> if not found.
   */
  public static LineStyle lookup(final String name) {
    EnumSet<LineStyle> set = EnumSet.range(SOLID, NONE);
    for (LineStyle lineStyle : set) {
      if (lineStyle.getName().equals(name)) {
        return lineStyle;
      }
    }
    return NONE;
  }
}
