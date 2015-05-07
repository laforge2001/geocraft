/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.defs;


/**
 * Enumeration for shape types.
 * Common shape types include point groups, polylines, polygons, etc.
 */
public enum ShapeType {
  /** A point group. */
  POINT_GROUP("Point Group"),
  /** A polyline. */
  POLYLINE("Polyline"),
  /** A closed polygon. */
  POLYGON("Polygon"),
  /** A polyline with only 2 points. */
  LINE("Line"),
  /** A polygon with 4 rectangular points. */
  RECTANGLE("Rectangle"),
  /** A buffered image. */
  IMAGE("Image"),
  /** A seismic trace. */
  TRACE("Trace");

  /** The name of the shape type. */
  private String _name;

  /**
   * Constructs a shape type.
   * @param name the name of the shape type.
   */
  ShapeType(final String name) {
    _name = name;
  }

  /**
   * Returns the name of the shape type.
   * @return the name of the shape type.
   */
  public String getName() {
    return _name;
  }

  @Override
  public String toString() {
    return getName();
  }
}
