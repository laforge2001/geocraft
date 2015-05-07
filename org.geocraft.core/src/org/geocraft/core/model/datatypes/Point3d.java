/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.core.model.datatypes;

import java.io.Serializable;


/**
 * This class defines a 3D point, consisting of an x,y,z in world coordinates.
 * <p>
 * We do not use the <i>Java3d</i> one because that API has 
 * been discontinued by Sun.
 * <p>
 * This class is immutable and thus thread-safe.
 */
public final class Point3d implements Serializable {

  /** The world x-coordinate. */
  private final double _x;

  /** The world y-coordinate. */
  private final double _y;

  /** The world z-coordinate. */
  private final double _z;

  /**
   * Constructs a 3D point.
   * 
   * @param x the world x-coordinate.
   * @param y the world y-coordinate.
   * @param z the world z-coordinate.
   */
  public Point3d(final double x, final double y, final double z) {
    _x = x;
    _y = y;
    _z = z;
  }

  /**
   * Constructs a 3D point, copied from another.
   * 
   * @param point the point to copy.
   */
  public Point3d(final Point3d point) {
    _x = point.getX();
    _y = point.getY();
    _z = point.getZ();
  }

  /**
   * Returns the x-coordinate of this point.
   * 
   * @return the x-coordinate.
   */
  public double getX() {
    return _x;
  }

  /**
   * Returns the y-coordinate of this point.
   * 
   * @return the y-coordinate.
   */
  public double getY() {
    return _y;
  }

  /**
   * Returns the z-coordinate of this point.
   * 
   * @return the z-coordinate.
   */
  public double getZ() {
    return _z;
  }

  /**
   * Compute the Euclidean distance from this 3D point to a second 3D point. 
   * 
   * @param point the other 3D point.
   * @return the distance between the two points. 
   */
  public double distanceTo(final Point3d point) {
    double dx = _x - point.getX();
    double dy = _y - point.getY();
    double dz = _z - point.getZ();
    return Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  @Override
  public String toString() {
    return "(" + _x + ", " + _y + ", " + _z + ")";
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj != null && obj instanceof Point3d) {
      // 3D points are "equal" if their x,y,z coordinates match.
      Point3d other = (Point3d) obj;
      if (other._x == _x && other._y == _y && other._z == _z) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int hashCode() {

    // Generate a custom hash code using the x,y,z coordinates.
    long x = Double.doubleToLongBits(_x);
    long y = Double.doubleToLongBits(_y);
    long z = Double.doubleToLongBits(_z);

    int result = 17;
    result = 31 * result + (int) (x ^ x >>> 32);
    result = 31 * result + (int) (y ^ y >>> 32);
    result = 31 * result + (int) (z ^ z >>> 32);

    return result;
  }

  /**
   * Linearly interpolates between 3D points <i>p1</i> and <i>p2</i>. 
   * <p>
   * The following equation is used:<br>
   * x = (1 - alpha) * p1.getX() + alpha * p2.getX()
   * @param p1 first 3D point.
   * @param p2 second 3D point.
   * @param alpha the interpolation parameter.
   * @return a new 3D point containing the interpolated value.
   */
  public static Point3d interpolate(final Point3d p1, final Point3d p2, final double alpha) {
    double x = (1 - alpha) * p1.getX() + alpha * p2.getX();
    double y = (1 - alpha) * p1.getY() + alpha * p2.getY();
    double z = (1 - alpha) * p1.getZ() + alpha * p2.getZ();
    return new Point3d(x, y, z);
  }
}
