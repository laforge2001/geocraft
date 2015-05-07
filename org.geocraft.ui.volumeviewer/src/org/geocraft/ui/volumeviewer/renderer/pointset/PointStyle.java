/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer.renderer.pointset;


/**
 * Enumeration for the point styles.
 * Options include circle, square, filled circle, etc.
 * These are used in point properties for points and shapes.
 */
public enum PointStyle {
  DOT("Dot"),
  SPHERE("Sphere"),
  CUBE("Cube"),
  PYRAMID("Pyramid");

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
    for (final PointStyle style : values()) {
      if (style.getName().equals(symbol)) {
        return style;
      }
    }
    return null;
  }
}
