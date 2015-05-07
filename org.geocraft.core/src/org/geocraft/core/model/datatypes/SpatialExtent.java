/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.core.model.datatypes;


/**
 * A class for defining the spatial extent (x,y,z minimum & maximum values) of an entity.
 */
public class SpatialExtent {

  /** The minimum x. */
  private double _xmin;

  /** The maximum x. */
  private double _xmax;

  /** The minimum y. */
  private double _ymin;

  /** The maximum y. */
  private double _ymax;

  /** The minimum z. */
  private double _zmin;

  /** The maximum z. */
  private double _zmax;

  /** The domain type. */
  private final Domain _domain;

  /** The x,y units. */
  private Unit _xyUnits;

  /** The z units. */
  private Unit _zUnits;

  /**
   * Constructs an empty spatial extent object. TODO 360 make the fields final?
   */
  public SpatialExtent() {
    this(new double[0], new double[0], new double[0], Domain.TIME);
  }

  /**
   * Construct a spatial extent object.
   * @param xs the array of x values.
   * @param ys the array of y values.
   * @param zs the array of z values.
   * @param domainType the domain type (TIME or DEPTH).
   */
  public SpatialExtent(final double[] xs, final double[] ys, final double[] zs, final Domain domain) {
    // TODO: should not reference outside the org.geocraft.core.model package
    // ApplicationPreferences appPrefs = ApplicationPreferences.getApplicationPreferences();
    // _xyUnits = appPrefs.getHorizontalDistanceUnit();
    // _zUnits = null;
    // if (domainType.equals(Domain.TIME)) {
    // _zUnits = appPrefs.getTimeUnit();
    // } else if (domainType.equals(Domain.LENGTH)) {
    // _zUnits = appPrefs.getVerticalDistanceUnit();
    // } else {
    // assert false : "Invalid domain type.";
    // }
    boolean first = true;
    _xmin = Double.NaN;
    _xmax = Double.NaN;
    for (int i = 0; i < xs.length; i++) {
      if (!Double.isNaN(xs[i])) {
        if (!first) {
          _xmin = Math.min(_xmin, xs[i]);
          _xmax = Math.max(_xmax, xs[i]);
        } else {
          _xmin = xs[i];
          _xmax = xs[i];
          first = false;
        }
      }
    }
    first = true;
    _ymin = Double.NaN;
    _ymax = Double.NaN;
    for (int i = 0; i < ys.length; i++) {
      if (!Double.isNaN(ys[i])) {
        if (!first) {
          _ymin = Math.min(_ymin, ys[i]);
          _ymax = Math.max(_ymax, ys[i]);
        } else {
          _ymin = ys[i];
          _ymax = ys[i];
          first = false;
        }
      }
    }
    first = true;
    _zmin = Double.NaN;
    _zmax = Double.NaN;
    for (int i = 0; i < zs.length; i++) {
      if (!Double.isNaN(zs[i])) {
        if (!first) {
          _zmin = Math.min(_zmin, zs[i]);
          _zmax = Math.max(_zmax, zs[i]);
        } else {
          _zmin = zs[i];
          _zmax = zs[i];
          first = false;
        }
      }
    }
    _domain = domain;
  }

  /** The x,y units. */
  public Unit getXYUnits() {
    return _xyUnits;
  }

  /** The z units. */
  public Unit getZUnits() {
    return _zUnits;
  }

  /**
   * Gets the domain type (TIME or DEPTH).
   * @return the domain type.
   */
  public Domain getDomainType() {
    return _domain;
  }

  /**
   * Gets the minimum x.
   * @return the minimum x.
   */
  public double getMinX() {
    return _xmin;
  }

  /**
   * Gets the maximum x.
   * @return the maximum x.
   */
  public double getMaxX() {
    return _xmax;
  }

  /**
   * Gets the minimum y.
   * @return the minimum y.
   */
  public double getMinY() {
    return _ymin;
  }

  /**
   * Gets the maximum y.
   * @return the maximum y.
   */
  public double getMaxY() {
    return _ymax;
  }

  /**
   * Gets the minimum z.
   * @return the minimum z.
   */
  public double getMinZ() {
    return _zmin;
  }

  /**
   * Gets the maximum z.
   * @return the maximum z.
   */
  public double getMaxZ() {
    return _zmax;
  }

  @Override
  public String toString() {
    String result = "SpatialExtent:" + "\nMin X " + getMinX() + " Max X " + getMaxX() + "\nMin Y " + getMinY()
        + " Max Y " + getMaxY() + "\nHorizontal Units: " + getXYUnits();

    if (!Double.isNaN(getMinZ()) && !Double.isNaN(getMaxZ())) {
      result += "\nMin Z " + getMinZ() + " Max Z " + getMaxZ() + "\nVertical Units:" + getZUnits();
    }

    return result;
  }
}
