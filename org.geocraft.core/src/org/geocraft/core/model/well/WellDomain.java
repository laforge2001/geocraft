/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.well;


/**
 * Enumeration of the various domains for well log traces.
 * These include one-way-time (OWT), two-way-time (TWT), measured-depth (MD),
 * true-vertical-depth (TVD) and true-vertical-depth sub-sea (TVDSS).
 */
public enum WellDomain {
  ONE_WAY_TIME("One-Way-Time", "OWT"),
  TWO_WAY_TIME("Two-Way-Time", "TWT"),
  MEASURED_DEPTH("Measured Depth", "MD"),
  TRUE_VERTICAL_DEPTH("True Vertical Depth", "TVD"),
  TRUE_VERTICAL_DEPTH_SUBSEA("True Vertical Depth (Sub-Sea)", "TVDSS");

  /** The name of the domain. */
  private String _name;

  /** The mnemonic of the domain. */
  private String _mnemonic;

  WellDomain(String name, String mnemonic) {
    _name = name;
    _mnemonic = mnemonic;
  }

  /**
   * Returns the name of the domain ("Measured Depth", "True Vertical Depth", etc).
   */
  public String getName() {
    return _name;
  }

  /**
   * Returns the mnemonic of the domain ("MD", "TVD", "TVDSS", etc).
   */
  public String getMnemonic() {
    return _mnemonic;
  }

  @Override
  public String toString() {
    return getMnemonic();
  }
}
