/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.datatypes;


/**
 * Enumeration for the onset type related to grids.
 */
public enum OnsetType {
  MINIMUM("Minimum"),
  MAXIMUM("Maximum"),
  ZERO_CROSSING("Zero Crossing"),
  ZERO_CROSSING_NEG2POS("Zero Crossing -/+"),
  ZERO_CROSSING_POS2NEG("Zero Crossing +/-");

  private String _name;

  OnsetType(final String name) {
    _name = name;
  }

  @Override
  public String toString() {
    return _name;
  }
}
