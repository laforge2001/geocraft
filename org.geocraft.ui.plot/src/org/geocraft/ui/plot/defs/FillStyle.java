/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.defs;


import java.util.EnumSet;


/**
 * Enumeration for fill styles.
 * Options include SOLID, TEXTURE and NONE.
 * These are used in fill properties for polygons, etc.
 */
public enum FillStyle {
  /** A solid color fill style. */
  SOLID("Solid"),
  /** A textured fill style. */
  TEXTURE("Texture"),
  /** No fill style. */
  NONE("None");

  /** The name of the fill style. */
  private String _name;

  /**
   * Constructs a fill style.
   * @param name the name of the fill style.
   */
  FillStyle(final String name) {
    _name = name;
  }

  /**
   * Returns the name of the fill style.
   * @return the name of the fill style.
   */
  public String getName() {
    return _name;
  }

  @Override
  public String toString() {
    return getName();
  }

  /**
   * Looks up a fill style based on its name.
   * @param name the fill style name.
   * @return the fill style matching name, or <i>NONE</i> if not found.
   */
  public static FillStyle lookup(final String name) {
    EnumSet<FillStyle> set = EnumSet.range(SOLID, NONE);
    for (FillStyle fillStyle : set) {
      if (fillStyle.getName().equals(name)) {
        return fillStyle;
      }
    }
    return NONE;
  }
}
