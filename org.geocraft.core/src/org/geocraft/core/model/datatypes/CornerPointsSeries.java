/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.core.model.datatypes;


import java.util.Arrays;


/**
 * This class is a specialized case of CoordinateSeries that strictly handles
 * all of the corner point operations. This is due in large part to alleviate
 * a great deal of the needless creation and copying of Point3d objects.
 */

public class CornerPointsSeries extends CoordinateSeries {

  private boolean _isRectilinear = false;

  private boolean _isRectangular = false;

  /**
   * Constructs a corner points series.
   * <p>
   * NOTE: A defensive copy of the points array is automatically made, so any changes to the array
   * afterward <i>WILL NOT</i> affect this series.
   *
   * @param points the array of points.
   * @param coordSystem the coordinate system of the points
   */
  public static CornerPointsSeries create(final Point3d[] points, final CoordinateSystem coordSystem) {
    return new CornerPointsSeries(Arrays.copyOf(points, points.length), coordSystem);
  }

  /**
   * Constructs a corner points series.
   * <p>
   * WARNING: No defensive copy of the points array is made, so any changes to the array
   * afterward <i>WILL</i> affect this series.
   *
   * @param points the array of points.
   * @param coordSystem the coordinate system of the points
   */
  public static CornerPointsSeries createDirect(final Point3d[] points, final CoordinateSystem coordSystem) {
    return new CornerPointsSeries(points, coordSystem);
  }

  private CornerPointsSeries(final Point3d[] points, final CoordinateSystem cs) {
    super(points, cs);

    if (points.length != 4) {
      throw new IllegalArgumentException("CornerPointSeries expects 4 points but was sent " + points.length);
    }

    _isRectilinear = computeRectilinear();
    _isRectangular = computeRectangular();
  }

  /**
   * Returns the interpolated Point3d given a I,J (column,row).
   *
   * @return Interpolated Point3d
   */
  public Point3d interpolate(final double i, final double j, final int numI, final int numJ) {
    Point3d p1 = Point3d.interpolate(_points[0], _points[3], j / (numJ - 1));
    Point3d p2 = Point3d.interpolate(_points[1], _points[2], j / (numJ - 1));
    if (numJ == 1) {
      p1 = _points[0];
      p2 = _points[1];
    }
    Point3d p3 = Point3d.interpolate(p1, p2, i / (numI - 1));
    if (numI == 1) {
      p3 = p1;
    }
    return p3;
  }

  /**
   * Transforms x,y coordinates to I,J coordinates.
   *
   * @param x the x coordinate.
   * @param y the y coordinate.
   * @param round true to round off to the nearest integer I,J; otherwise false.
   * @param numJ number of I columns
   * @param numI number of J rows
   * @return the I,J coordinates (as an array of length=2).
   */
  public double[] transformXYToIJ(final double x, final double y, final boolean round, final int numI, final int numJ) {

    double dx1 = _points[1].getX() - _points[0].getX();
    double dy1 = _points[1].getY() - _points[0].getY();
    double dx3 = _points[3].getX() - _points[0].getX();
    double dy3 = _points[3].getY() - _points[0].getY();
    double dx4 = _points[2].getX() - _points[1].getX();
    double dy4 = _points[2].getY() - _points[1].getY();
    double dx = x - _points[0].getX();
    double dy = y - _points[0].getY();
    double aTerm = dx1 * dy3 - dx1 * dy4 - dy1 * dx3 + dy1 * dx4;
    double bTerm = dx * dy4 - dx * dy3 - dx1 * dy3 + dy * dx3 - dy * dx4 + dy1 * dx3;
    double cTerm = dx * dy3 - dy * dx3;
    double tsol = Double.NaN;
    double usol = Double.NaN;

    if (isRectilinear()) {
      tsol = (dy * dx3 - dx * dy3) / (dy1 * dx3 - dx1 * dy3);
      if (Double.isNaN(tsol)) {
        if (_points[0].equals(_points[1]) && _points[2].equals(_points[3])) {
          double icoord = x - _points[0].getX();
          double jcoord = (y - _points[0].getY()) / (dy3 / (numJ - 1));
          return new double[] { icoord, jcoord };
        } else if (_points[0].equals(_points[3]) && _points[1].equals(_points[2])) {
          double jcoord = y - _points[0].getY();
          double icoord = (x - _points[0].getX()) / (dx1 / (numI - 1));
          return new double[] { icoord, jcoord };
        }
      }
    } else {
      // TODO: this is a quick-fix for grids that are nearly rectangular and align with the x,y axes
      // the a term is very small but not exactly zero.
      if (Math.abs(aTerm) < 0.0001) {
        tsol = -cTerm / bTerm;
      } else {

        double sqrt = Math.sqrt(bTerm * bTerm - 4 * aTerm * cTerm);
        double tsol1 = (-bTerm + sqrt) / (2 * aTerm);
        double tsol2 = (-bTerm - sqrt) / (2 * aTerm);

        if (tsol1 >= 0 && tsol1 <= 1) {
          tsol = tsol1;
        } else if (tsol2 >= 0 && tsol2 <= 1) {
          tsol = tsol2;
        } else {
          tsol = Math.abs(tsol1) < Math.abs(tsol2) ? tsol1 : tsol2;
        }
      }
    }

    double icoord = Double.NaN;
    double jcoord = Double.NaN;

    if (!Double.isNaN(tsol)) {

      double denom1 = dx3 - dx3 * tsol + dx4 * tsol;
      double denom2 = dy3 - dy3 * tsol + dy4 * tsol;

      if (Math.abs(denom1) > Math.abs(denom2)) {
        usol = (dx - dx1 * tsol) / denom1;
      } else {
        usol = (dy - dy1 * tsol) / denom2;
      }
      icoord = tsol * (numI - 1);
      jcoord = usol * (numJ - 1);
    }
    if (round && !Double.isNaN(icoord) && !Double.isNaN(jcoord)) {
      icoord = Math.round(icoord);
      jcoord = Math.round(jcoord);
    }

    return new double[] { icoord, jcoord };
  }

  /**
   * Returns true if the grid corner points are rectilinear (not necessary rectangular).
   *
   * @return true if the grid corner points are rectilinear; false if not.
   */
  public boolean isRectilinear() {
    return _isRectilinear;
  }

  /**
   * Returns true if the grid corner points are rectangular. 
   * 
   * This means the axes are at 90 degrees to one another which
   * can make some algorithms simpler to write. 
   * 
   * @return true if the grid corner points are rectangular; false if not.
   */
  public boolean isRectangular() {
    return _isRectangular;
  }

  private boolean computeRectilinear() {

    boolean result = false;

    double dx01 = _points[1].getX() - _points[0].getX();
    double dx32 = _points[2].getX() - _points[3].getX();
    double dy01 = _points[1].getY() - _points[0].getY();
    double dy32 = _points[2].getY() - _points[3].getY();
    double dx03 = _points[3].getX() - _points[0].getX();
    double dx12 = _points[2].getX() - _points[1].getX();
    double dy03 = _points[3].getY() - _points[0].getY();
    double dy12 = _points[2].getY() - _points[1].getY();

    // TODO these hardcoded tolerances might not always work.
    if (Math.abs(dx01 - dx32) < 0.0001 && Math.abs(dy01 - dy32) < 0.0001 && Math.abs(dx03 - dx12) < 0.0001
        && Math.abs(dy03 - dy12) < 0.0001) {
      result = true;
    }

    return result;
  }

  private boolean computeRectangular() {

    boolean result = false;

    // First check if grid is rectilinear.
    if (isRectilinear()) {

      // Then check if grid angles are perpendicular (within a tolerance).
      double dx01 = _points[1].getX() - _points[0].getX();
      double dy01 = _points[1].getY() - _points[0].getY();
      double dx03 = _points[3].getX() - _points[0].getX();
      double dy03 = _points[3].getY() - _points[0].getY();
      double angle1 = Math.atan2(dy01, dx01);
      double angle2 = Math.atan2(dy03, dx03);

      if (angle2 >= angle1 + Math.PI / 2 - 0.0001 && angle2 <= angle1 + Math.PI / 2 + 0.0001
          || angle2 >= angle1 - Math.PI / 2 - 0.0001 && angle2 <= angle1 - Math.PI / 2 + 0.0001) {
        result = true;
      }
    }
    return result;
  }

}
