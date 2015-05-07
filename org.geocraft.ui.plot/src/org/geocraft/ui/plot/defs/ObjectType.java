/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.defs;


/**
 * Enumeration for plot object types.
 * Object types include only points and shapes.
 */
public enum ObjectType {
  /** A plot point. */
  POINT("Point"),
  /** A plot shape (line, polyline, etc). */
  SHAPE("Shape");

  /** The name of the object type. */
  private String _name;

  /**
   * Constructs a object type.
   * @param name the name of the object type.
   */
  ObjectType(final String name) {
    _name = name;
  }

  /**
   * Returns the name of the object type.
   * @return the name of the object type.
   */
  public String getName() {
    return _name;
  }

  @Override
  public String toString() {
    return getName();
  }
}
