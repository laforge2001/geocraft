/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.ui.volumeviewer.canvas;


import com.ardor3d.math.MathUtils;


public enum Orientation {

  // XXX: Map View needs to be close to but not quite 90degrees elevation
  // XXX: Indicated in degrees, but stored in radians. where azimuth 0 is north
  MAP_VIEW("Map View", 180, 90), SOUTH_WEST("South West", 225, 45);

  private String _name;

  private double _azimuth;

  private double _elevation;

  private Orientation(final String name, final double azimuthDegrees, final double elevationDegrees) {
    _name = name;
    _azimuth = azimuthDegrees * MathUtils.DEG_TO_RAD;
    _elevation = elevationDegrees * MathUtils.DEG_TO_RAD;
  }

  @Override
  public String toString() {
    return _name;
  }

  public double getAzimuthRadians() {
    return _azimuth;
  }

  public double getElevationRadians() {
    return _elevation;
  }
}