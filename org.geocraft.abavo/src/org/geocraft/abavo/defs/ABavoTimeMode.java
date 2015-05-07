/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.defs;


public enum ABavoTimeMode {

  BETWEEN_TIMES("Between Times or Depths"), RELATIVE_TO_HORIZON("Relative to Horizon"), RELATIVE_TO_HORIZONS("Relative to Horizons");

  private String _name;

  ABavoTimeMode(final String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }

  @Override
  public String toString() {
    return getName();
  }
}
