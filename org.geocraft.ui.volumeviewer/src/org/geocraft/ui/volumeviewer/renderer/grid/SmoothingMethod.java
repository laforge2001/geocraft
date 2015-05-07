/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer.renderer.grid;


public enum SmoothingMethod {
  NONE("None"),
  INTERPOLATION("Interpolation");

  private String _text;

  private SmoothingMethod(final String text) {
    _text = text;
  }

  public String getName() {
    return _text;
  }

  @Override
  public String toString() {
    return _text;
  }

  public static SmoothingMethod lookup(final String name) {
    if (name == null || name.length() == 0) {
      return SmoothingMethod.NONE;
    }
    for (final SmoothingMethod method : SmoothingMethod.values()) {
      if (method.getName().equals(name)) {
        return method;
      }
    }
    return SmoothingMethod.NONE;
  }
}
