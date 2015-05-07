/*
 * Copyright (C) ConocoPhillips 2009 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.velocity.flood;


public class VelocityFloodConstants {

  /** Flood Type. */
  public enum FloodType {
    Constant("Constant"),
    Gradient("Gradient: V0 +G*(z-Z0)"),
    Dataset("Dataset");

    private final String _displayName;

    FloodType(final String displayName) {
      _displayName = displayName;
    }

    @Override
    public String toString() {
      return _displayName;
    }
  }

  /** Direction to flood. */
  public enum FloodDirection {
    Below("Below top grid"),
    Above("Above base grid"),
    Between("Between top and base grids");

    private final String _displayName;

    FloodDirection(final String displayName) {
      _displayName = displayName;
    }

    @Override
    public String toString() {
      return _displayName;
    }
  }

  /** For velocity and/or gradient, use a constant or a variable horizon. */
  public enum ConstantOrGridSelection {
    Constant("Use a constant"),
    Grid("Use a grid");

    private final String _displayName;

    ConstantOrGridSelection(final String displayName) {
      _displayName = displayName;
    }

    @Override
    public String toString() {
      return _displayName;
    }
  }

  /** Gradient reference selection. Use a constant depth or a variable horizon. */
  public enum ReferenceSelection {
    Constant("Use a constant"),
    Grid("Use a grid");

    private final String _displayName;

    ReferenceSelection(final String displayName) {
      _displayName = displayName;
    }

    @Override
    public String toString() {
      return _displayName;
    }
  }
}
