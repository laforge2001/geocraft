/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer;


/**
 * Enumeration of the available projection modes.
 */
public enum ProjectionMode {
  ORTHOGRAPHIC("Orthographic"),
  PERSPECTIVE("Perspective");

  private String _name;

  private ProjectionMode(final String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }

  @Override
  public String toString() {
    return _name;
  }

  public static ProjectionMode lookup(final String name) {
    if (name == null) {
      return null;
    }
    for (final ProjectionMode mode : ProjectionMode.values()) {
      if (mode.getName().equals(name)) {
        return mode;
      }
    }
    return null;
  }
}
