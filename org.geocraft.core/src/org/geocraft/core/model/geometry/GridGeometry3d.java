/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.geometry;


import java.io.Serializable;

import org.geocraft.core.common.math.GeometryUtil;
import org.geocraft.core.common.math.MathUtil;
import org.geocraft.core.common.util.HashCode;
import org.geocraft.core.model.base.ValueObject;
import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.CoordinateSystem;
import org.geocraft.core.model.datatypes.CornerPointsSeries;
import org.geocraft.core.model.datatypes.Orientation;
import org.geocraft.core.model.datatypes.Point3d;


/**
 * This class defines a two-dimension grid geometry.
 * The geometry is defined by supplying the 4 corner points and the number
 * of rows and columns. The ordering of the corner points is defined by
 * the following row,column pairs: (0, 0), (0, ncols-1), (nrows-1, ncols-1), (nrows-1, 0).
 * This ordering is illustrated below:
 * 
 *   3---------------2   nrows-1 
 *   |   |   |   |   | 
 *   |---+---+---+---|     ^
 *   |   |   |   |   |     |
 *   |---+---+---+---|     |
 *   |   |   |   |   |
 *   0---------------1     0
 *   
 *   0     --->    ncols-1
 *
 * Once created, the grid geometry is immutable and it attributes cannot be changed.
 * A grid geometry is considered equal to another grid geometry if their corner points
 * and row,column definitions are the same.
 */
public class GridGeometry3d extends ValueObject implements IGridGeometry, Serializable {

  /** The number of I columns in the grid. */
  private final int _numI;

  /** The number of J rows in the grid. */
  private final int _numJ;

  /** The 4 corner points of the grid geometry. */
  private final CornerPointsSeries _cornerPoints;

  /** The orientation (clockwise or counter-clockwise) of the grid geometry corner points. */
  private Orientation _orientation;

  /** The spacing between adjacent I columns in the grid geometry. */
  private double _iSpacing;

  /** The spacing between adjacent J rows in the grid geometry. */
  private double _jSpacing;

  /** The rotation angle of the grid geometry (valid only for rectangular geometries). */
  private double _rotationInDeg;

  /**
   * Constructs a grid geometry by supplying 4 corner points and the number of rows and columns.
   * The ordering of points in the grid geometry is defined as follows:
   * 
   *   3---------------2   nrows-1 
   *   |   |   |   |   | 
   *   |---+---+---+---|     ^
   *   |   |   |   |   |     |
   *   |---+---+---+---|     |
   *   |   |   |   |   |
   *   0---------------1     0
   *   
   *   0     --->    ncols-1
   *   
   * @param name the name the new grid geometry.
   * @param numRows the number of rows in the grid geometry.
   * @param numColumns the number of columns in the grid geometry.
   * @param cornerPoints the series containing the 4 corner points of the grid geometry.
   */
  public GridGeometry3d(final String name, final int numRows, final int numColumns, final CornerPointsSeries cornerPoints) {
    super(name);
    _numJ = numRows;
    _numI = numColumns;
    _cornerPoints = cornerPoints;
    initialize();
  }

  /**
   * Constructs a grid geometry copied from another grid geometry.
   * 
   * @param name the name of the new grid geometry.
   * @param geometry the grid geometry from which to copy.
   */
  public GridGeometry3d(final String name, final GridGeometry3d geometry) {
    this(name, geometry.getNumRows(), geometry.getNumColumns(), geometry.getCornerPoints());
  }

  /**
   * Constructs a grid geometry based on an origin, row and column spacings and a rotation angle.
   * 
   * @param name the name of the new grid geometry.
   * @param xOrigin the x-coordinate of the origin.
   * @param yOrigin the y-coordinate of the origin.
   * @param colSpacing the column spacing.
   * @param rowSpacing the row spacing.
   * @param numRows the number of rows.
   * @param numColumns the number of columns.
   * @param rotation the rotation angle (in degrees).
   */
  public GridGeometry3d(final String name, final double xOrigin, final double yOrigin, final double colSpacing, final double rowSpacing, final int numRows, final int numColumns, final double rotation) {
    super(name);
    Point3d[] points = new Point3d[4];
    double costerm1 = Math.cos(Math.toRadians(rotation));
    double sinterm1 = Math.sin(Math.toRadians(rotation));
    double costerm2 = Math.cos(Math.toRadians(rotation + 90));
    double sinterm2 = Math.sin(Math.toRadians(rotation + 90));
    int[] rows = { 0, 0, numRows - 1, numRows - 1 };
    int[] cols = { 0, numColumns - 1, numColumns - 1, 0 };
    for (int i = 0; i < 4; i++) {
      double xVal = xOrigin + cols[i] * colSpacing * costerm1 + rows[i] * rowSpacing * costerm2;
      double yVal = yOrigin + cols[i] * colSpacing * sinterm1 + rows[i] * rowSpacing * sinterm2;
      points[i] = new Point3d(xVal, yVal, 0);
    }

    CoordinateSystem coordSys = null;
    _cornerPoints = CornerPointsSeries.createDirect(points, coordSys);
    _numI = numColumns;
    _numJ = numRows;
    initialize();
  }

  public int getNumRows() {
    return _numJ;
  }

  public int getNumColumns() {
    return _numI;
  }

  public int getNumI() {
    return getNumColumns();
  }

  public int getNumJ() {
    return getNumRows();
  }

  public CornerPointsSeries getCornerPoints() {
    return _cornerPoints;
  }

  public boolean isRectangular() {
    return _cornerPoints.isRectangular();
  }

  public double getRowSpacing() {
    return _jSpacing;
  }

  public double getColumnSpacing() {
    return _iSpacing;
  }

  public double getRotation() {
    return _rotationInDeg;
  }

  public Orientation getClockwise() {
    return _orientation;
  }

  public boolean containsRowCol(final int row, final int col) {
    return containsIJ(col, row);
  }

  public double[] transformRowColToXY(final double row, final double col) {
    return transformRowColToXY(row, col, true);
  }

  public double[] transformRowColToXY(final double row, final double col, final boolean checkBounds) {
    return transformIJToXY(col, row, checkBounds);
  }

  public double[] transformXYToRowCol(final double x, final double y, final boolean round) {
    double[] ij = transformXYToIJ(x, y, round);
    return new double[] { ij[1], ij[0] };
  }

  public boolean containsIJ(final int i, final int j) {
    return i >= 0 && i < _numI && j >= 0 && j < _numJ;
  }

  public double[] transformIJToXY(final double i, final double j) {
    return transformIJToXY(i, j, true);
  }

  public double[] transformIJToXY(final double i, final double j, final boolean checkBounds) {
    if (checkBounds && (j < 0 || j > _numJ - 1 || i < 0 || i > _numI - 1)) {
      throw new IndexOutOfBoundsException("Invalid I,J: " + i + "," + j + "." + " NumI=" + _numI + " NumJ=" + _numJ);
    }
    Point3d point = _cornerPoints.interpolate(i, j, _numI, _numJ);
    return new double[] { point.getX(), point.getY() };
  }

  public double[] transformXYToIJ(final double x, final double y, final boolean round) {
    return _cornerPoints.transformXYToIJ(x, y, round, _numI, _numJ);
  }

  public boolean matchesGeometry(final GridGeometry3d gridGeometry) {
    // Check that the number of I,J are equal.
    if (getNumI() != gridGeometry.getNumI() || getNumJ() != gridGeometry.getNumJ()) {
      return false;
    }
    CornerPointsSeries points1 = getCornerPoints();
    CornerPointsSeries points2 = gridGeometry.getCornerPoints();
    // Check that the number of points are equal.
    if (points1.getNumPoints() != points2.getNumPoints()) {
      return false;
    }
    // Check that the x,y coordinates of the corner points are equal.
    for (int i = 0; i < points1.getNumPoints(); i++) {
      if (!MathUtil.isEqual(points1.getX(i), points2.getX(i)) || !MathUtil.isEqual(points1.getY(i), points2.getY(i))) {
        return false;
      }
    }
    // The match conditions have been satisfied.
    return true;
  }

  @Override
  public boolean equals(final Object object) {
    // Check that it is an instance of a grid geometry.
    if (object != null && object instanceof GridGeometry3d) {
      GridGeometry3d grid = (GridGeometry3d) object;
      // Compare the display names.
      if (!getDisplayName().equals(grid.getDisplayName())) {
        return false;
      }
      // If the display names match, then compare the geometries.
      return matchesGeometry(grid);
    }
    return false;
  }

  @Override
  public int hashCode() {
    HashCode hashCode = new HashCode();
    hashCode.add(getDisplayName());
    hashCode.add(getNumI());
    hashCode.add(getNumJ());
    hashCode.add(getCornerPoints());
    return hashCode.getHashCode();
  }

  @Override
  public String toString() {
    return getDisplayName();
  }

  /**
   * Computes the row spacing, column spacing and corner point orientation of the grid geometry.
   */
  private void initialize() {
    int numI = getNumI();
    int numJ = getNumJ();

    // Compute the I spacing.
    double[] xy1 = transformRowColToXY(0, 0);
    double[] xy2 = transformRowColToXY(0, numI - 1);
    double dx = xy2[0] - xy1[0];
    double dy = xy2[1] - xy1[1];
    _iSpacing = Math.sqrt(dx * dx + dy * dy) / (numI - 1);

    // Compute the J spacing.
    xy1 = transformRowColToXY(0, 0);
    xy2 = transformRowColToXY(numJ - 1, 0);
    dx = xy2[0] - xy1[0];
    dy = xy2[1] - xy1[1];
    _jSpacing = Math.sqrt(dx * dx + dy * dy) / (numJ - 1);

    // Compute the corner points orientation.
    CoordinateSeries cps = getCornerPoints();
    double[] normal = GeometryUtil.computeNormal(cps.getX(0), cps.getY(0), 0, cps.getX(1), cps.getY(1), 0, cps.getX(2),
        cps.getY(2), 0);
    double distance = GeometryUtil.distancePointToPlane(cps.getX(0), cps.getY(0), 0, normal, cps.getX(0), cps.getY(0),
        10);
    if (distance > 0) {
      _orientation = Orientation.COLUMN;
    } else {
      _orientation = Orientation.ROW;
    }

    // Compute the rotation angle.
    double rotation = Math.atan2(cps.getY(1) - cps.getY(0), cps.getX(1) - cps.getX(0));
    _rotationInDeg = Math.toDegrees(rotation);
  }
}
