/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.core.model.datatypes;

import java.io.Serializable;


/**
 * The CoordinateSystem will eventually define the one set of 
 * projection parameters that are in use in the current project. 
 * 
 * The data mappers will eventually be responsible for reprojecting 
 * data when necessary. 
 * 
 * Today all it defines is whether the data is in the time or 
 * depth domain. 
 * 
 * Will eventually need to support proper definitions of projections
 * and datums that are more than just java Strings. 
 */
public class CoordinateSystem implements Serializable {

  /** 
   * Name of the coordinate system. 
   *
   * For example: UTM Zone 38
   */
  private final String _projection;

  /** 
   * The geodetic datum used by this coordinate system. 
   * 
   * For example: WGS84
   */
  private final String _datum;

  /** Domain of the coordinate system:  time/depth. */
  private final Domain _domain;

  /**
   * Constructs a coordinate system. 
   * 
   * @param name of the coordinate system
   * @param domain (time/depth) of the coordinate system
   */
  public CoordinateSystem(final String name, final Domain domain) {
    _projection = name;
    _datum = "unknown";
    _domain = domain;
  }

  /**
   * Constructs a coordinate system.
   * 
   * @param projection eg UTM Zone 15
   * @param datum eg WGS84
   * @param domain time or depth
   */
  public CoordinateSystem(final String projection, final String datum, final Domain domain) {
    _projection = projection;
    _datum = datum;
    _domain = domain;
  }

  /**
   * Copy constructor.
   * @param prototype
   */
  public CoordinateSystem(final CoordinateSystem prototype) {
    _projection = prototype.getProjection();
    _datum = prototype.getDatum();
    _domain = prototype.getDomain();
  }

  /**
   * @return the coordinate system name
   */
  @Deprecated
  public String getName() {
    return _projection;
  }

  /**
   * @return domain of the coordinate system
   */
  public Domain getDomain() {
    return _domain;
  }

  /**
   * @return The map projection eg UTM Zone 15
   */
  public String getProjection() {
    return _projection;
  }

  /**
   * @return the geodetic datum eg WGS84 or NAD27
   */
  public String getDatum() {
    return _datum;
  }

  /**
   * @return string representation
   */
  @Override
  public String toString() {
    return "Projection: " + _projection + " Datum: " + _datum + " Domain: " + _domain;
  }
}
