/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer;


/**
 * Enumeration of the available methods for interpolation.
 */
public enum InterpolationMethod {
  /** No interpolation (step-wise). */
  STEPWISE("Stepwise"),
  /** Linear interpolation. */
  LINEAR("Linear");

  private final String _text;

  private InterpolationMethod(final String text) {
    _text = text;
  }

  public String getName() {
    return _text;
  }

  @Override
  public String toString() {
    return _text;
  }

  public static InterpolationMethod lookup(final String name) {
    if (name == null) {
      return null;
    }
    for (InterpolationMethod method : InterpolationMethod.values()) {
      if (method.getName().equals(name)) {
        return method;
      }
    }
    return null;
  }
}
