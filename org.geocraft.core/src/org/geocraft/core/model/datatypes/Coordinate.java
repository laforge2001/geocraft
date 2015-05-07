/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.core.model.datatypes;


/**
 * Class encapsulating a point and the coordinate system of the point.
 */
public final class Coordinate {

  private final Point3d _point;

  private final CoordinateSystem _system;

  /**
   * Constructor
   * @param point the point
   * @param cs the coordinate system of the point
   */
  public Coordinate(final Point3d point, final CoordinateSystem cs) {
    _point = point;
    _system = cs;
  }

  /**
   * @return the point associated with the coordinate
   */
  public Point3d getPoint() {
    return new Point3d(_point);
  }

  /**
   * @return the x value associated with the coordinate
   */
  public double getX() {
    return _point.getX();
  }

  /**
   * @return the y value associated with the coordinate
   */
  public double getY() {
    return _point.getY();
  }

  /*
   * @return the z value associated with the coordinate
   */
  public double getZ() {
    return _point.getZ();
  }

  /**
   * @return the coordinate system associated with the coordinate
   */
  public CoordinateSystem getSystem() {
    return _system;
  }

  @Override
  public String toString() {

    String s = "";

    if (_point != null) {
      s += _point.toString() + " ";
    }
    if (_system != null) {
      s += _system.toString();
    }
    return s;
  }
}
