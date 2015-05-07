/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.fault;


/**
 * Enumeration for the various types of faults.
 * This includes Normal, Thrust (or Reverse) and Strike-slip.
 */
public enum FaultType {
  NORMAL("Normal"),
  THRUST("Thrust", "Reverse"),
  STRIKE_SLIP("Strike-slip"),
  OTHER("Other");

  /** The name of the fault type. */
  private String _name;

  private String _alias;

  FaultType(final String name) {
    this(name, name);
  }

  FaultType(final String name, final String alias) {
    _name = name;
    _alias = alias;
  }

  /**
   * Returns the name of the fault type.
   */
  public String getName() {
    return _name;
  }

  /**
   * Returns the alias of the fault type (or name if no alias exists).
   */
  public String getAlias() {
    return _alias;
  }

  @Override
  public String toString() {
    return _name;
  }

  /**
   * Looks up the fault type by the given name (or alias).
   * 
   * @param name the name (or alias) on which to search.
   * @return the corresponding fault type, or <i>null</i> if no match.
   */
  public static FaultType lookupByName(final String name) {
    for (FaultType type : values()) {
      if (name.equals(type.getName()) || name.equals(type.getAlias())) {
        return type;
      }
    }
    return null;
  }
}
