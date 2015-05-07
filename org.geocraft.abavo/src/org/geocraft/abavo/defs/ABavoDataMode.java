/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.defs;


/**
 * Enumeration for the ABAVO data mode. Only 2 options are currently available, and they
 * include "All Data" and "Peaks and Troughs".
 */
public enum ABavoDataMode {

  ALL_DATA("All Data"),
  PEAKS_AND_TROUGHS("Peaks and Troughs");

  private String _name;

  ABavoDataMode(final String name) {
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
