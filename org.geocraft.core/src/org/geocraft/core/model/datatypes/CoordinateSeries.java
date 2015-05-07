/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.core.model.datatypes;


import java.io.Serializable;
import java.util.Arrays;


/**
 * Class encapsulating an array of points and the coordinate system of the points.
 * All the points have the same coordinate system.
 */
public class CoordinateSeries implements Serializable {

  /** An array of 3d points of arbitrary length. */
  protected final Point3d[] _points;

  /** The projection information. */
  protected final CoordinateSystem _coordSystem;

  /**
   * Constructs a coordinates series.
   * <p>
   * NOTE: A defensive copy of the points array is automatically made, so any changes to the array
   * afterward <i>WILL NOT</i> not affect this series.
   *
   * @param points the array of points.
   * @param coordSystem the coordinate system of the points
   */
  public static CoordinateSeries create(final Point3d[] points, final CoordinateSystem coordSystem) {
    return new CoordinateSeries(Arrays.copyOf(points, points.length), coordSystem);
  }

  /**
   * Constructs a coordinates series.
   * <p>
   * WARNING: No defensive copy of the points array is made, so any changes to the array
   * afterward <i>WILL</i> affect this series.
   *
   * @param points the array of points.
   * @param coordSystem the coordinate system of the points
   */
  public static CoordinateSeries createDirect(final Point3d[] points, final CoordinateSystem coordSystem) {
    return new CoordinateSeries(points, coordSystem);
  }

  /**
   * Constructs a coordinates series.
   * <p>
   * WARNING: No defensive copy of the points array is made, so any changes to the array
   * afterward will affect this series.
   *
   * @param points the actual points.
   * @param coordSystem the coordinate system of the points
   */
  protected CoordinateSeries(final Point3d[] points, final CoordinateSystem coordSystem) {
    _coordSystem = coordSystem;
    _points = points;
  }

  /**
   * Returns an array of the points in the coordinate series.
   * <p>
   * NOTE: A defensive copy of the points array is automatically made, so any changes to the array
   * afterward <i>WILL NOT</i> not affect this series.
   *
   * @return a copy of the array of points in the series.
   */
  public Point3d[] getCopyOfPoints() {
    return copyPoints(_points);
  }

  /**
   * Returns the array of the points in the coordinate series.
   * <p>
   * WARNING: No defensive copy of the points array is made, so any changes to the array
   * afterward <i>WILL</i> affect this series.
   * 
   * @return the array of points in the series.
   */
  public Point3d[] getPointsDirect() {
    return _points;
  }

  /**
   * Returns a point.
   *
   * @param index the index of the point to get.
   * @return the point.
   */
  public Point3d getPoint(final int index) {
    return _points[index];
  }

  /**
   * @return the coordinate system of the points
   */
  public CoordinateSystem getCoordinateSystem() {
    return _coordSystem;
  }

  /**
   * Faster than returning the points array.
   * @return the number of points in the coordinate system.
   */
  public int getNumPoints() {
    return _points.length;
  }

  /**
   * Returns an x coordinate. Faster than returning a copy
   * of the entire series.
   *
   * @param index of the desired point.
   * @return the x coordinate of the index'th point.
   */
  public double getX(final int index) {
    return _points[index].getX();
  }

  /**
   * Returns a y coordinate. Faster than returning a copy
   * of the entire series.
   *
   * @param index of the desired point.
   * @return the y coordinate of the index'th point.
   */
  public double getY(final int index) {
    return _points[index].getY();
  }

  /**
   * Returns a z coordinate. Faster than returning a copy
   * of the entire series.
   *
   * @param index of the desired point.
   * @return the z coordinate of the index'th point.
   */
  public double getZ(final int index) {
    return _points[index].getZ();
  }

  /**
   * Overridden for displaying readable strings for CoordinateSeries objects on the user interface,
   * for example in the properties view.
   */
  @Override
  public String toString() {
    return "CoordinateSeries: " + Arrays.toString(getPointsDirect()) + " Units: " + _coordSystem;
  }

  private Point3d[] copyPoints(final Point3d[] points) {
    Point3d[] pts = new Point3d[points.length];
    for (int i = 0; i < points.length; i++) {
      pts[i] = new Point3d(points[i]);
    }
    return pts;
  }

  @Override
  public boolean equals(final Object object) {
    if (object != null && object instanceof CoordinateSeries) {
      CoordinateSeries other = (CoordinateSeries) object;
      return Arrays.equals(_points, other.getPointsDirect());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(_points);
  }
}
