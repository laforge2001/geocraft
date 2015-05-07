/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.defs;


import java.util.EnumSet;


/**
 * Enumeration for alignment types.
 * Options include TOP, LEFT, RIGHT, BOTTOM and CENTER.
 * These are used in placement of labels, etc.
 */
public enum Alignment {
  TOP("Top"),
  LEFT("Left"),
  RIGHT("Right"),
  BOTTOM("Bottom"),
  CENTER("Center");

  /** The name of the alignment. */
  private String _name;

  /**
   * Constructs an alignment.
   * @param name the name of the alignment.
   */
  Alignment(final String name) {
    _name = name;
  }

  /**
   * Returns the name of the alignment.
   * @return the name of the alignment.
   */
  public String getName() {
    return _name;
  }

  @Override
  public String toString() {
    return getName();
  }

  public static Alignment lookup(final String name) {
    EnumSet<Alignment> set = EnumSet.range(TOP, CENTER);
    for (Alignment alignment : set) {
      if (alignment.getName().equals(name)) {
        return alignment;
      }
    }
    return CENTER;
  }
}
