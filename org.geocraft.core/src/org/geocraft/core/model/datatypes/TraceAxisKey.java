/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.datatypes;


/**
 * Enumeration for the various trace axis keys used in trace sections.
 */
public enum TraceAxisKey {
  /** The key for the inline axis. */
  INLINE("Inline"),
  /** The key for the xline axis. */
  XLINE("Xline"),
  /** The key for the offset axis. */
  OFFSET("Offset"),
  /** The key for the CDP axis. */
  CDP("CDP");

  /** The axis name. */
  private String _name;

  /**
   * Constructs a trace axis key.
   * 
   * @param name the axis name.
   */
  private TraceAxisKey(final String name) {
    _name = name;
  }

  public static TraceAxisKey fromString(final String name) {
    if (name != null) {
      for (TraceAxisKey type : TraceAxisKey.values()) {
        if (name.equals(type._name)) {
          return type;
        }
      }
    }
    throw new IllegalArgumentException("No TraceAxisKey \'" + name + "\'");
  }

  /**
   * Returns the axis name.
   */
  @Override
  public String toString() {
    return _name;
  }
}
