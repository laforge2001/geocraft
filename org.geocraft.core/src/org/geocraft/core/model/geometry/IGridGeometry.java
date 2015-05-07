/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.geometry;


import org.geocraft.core.model.datatypes.CornerPointsSeries;
import org.geocraft.core.model.datatypes.Orientation;


public interface IGridGeometry {

  /**
   * Gets the number of rows in the grid geometry.
   * 
   * @return the number of rows.
   */
  public abstract int getNumRows();

  /**
   * Gets the number of columns in the grid geometry.
   * 
   * @return the number of columns.
   */
  public abstract int getNumColumns();

  /**
   * Gets the series containing the 4 corner points of the grid geoemtry.
   * 
   * @return the corner points series.
   */
  public abstract CornerPointsSeries getCornerPoints();

  /**
   * Returns a flag indicating if the grid corner points are rectangular.
   * 
   * @return <i>true</i> if the grid corner points are rectangular; <i>false</i> if not.
   */
  public abstract boolean isRectangular();

  /**
   * Transforms row,column coordinates to x,y coordinates.
   * 
   * @param row the row number (0 to nrows-1).
   * @param col the column number (0 to ncols-1).
   * @return the x,y coordinates (as an array of length=2).
   */
  public abstract double[] transformRowColToXY(final double row, final double col);

  /**
   * Transforms row,column coordinates to x,y coordinates.
   * 
   * @param row the row number (0 to nrows-1).
   * @param col the column number (0 to ncols-1).
   * @return the x,y coordinates (as an array of length=2).
   */
  public abstract double[] transformRowColToXY(final double row, final double col, final boolean checkBounds);

  /**
   * Transforms x,y coordinates to row,column coordinates.
   * 
   * @param x the x coordinate.
   * @param y the y coordinate.
   * @param round true to round off to the nearest integer row,column; otherwise false.
   * @return the row,column coordinates (as an array of length=2).
   */
  public abstract double[] transformXYToRowCol(final double x, final double y, final boolean round);

  /**
   * Returns a flag indicating if the geometry matches that of the specified grid geometry.
   * The geometries are considered a match if they contain the same corner points, number of rows and number of columns.
   *
   * @param geometry the geometry to compare.
   * @return <i>true</i> if the geometries match; <i>false</i> if not.
   */
  public abstract boolean matchesGeometry(final GridGeometry3d gridGeometry);

  /**
   * Checks if an object is "equal" to this one.
   * To be considered "equal", the object be an instance of the
   * <code>GridGeometry</code> class, and its corner points and
   * row,column definitions must be the same.
   * 
   * @param object the object to check for equality.
   * @return <i>true</i> if the object is "equal"; <i>false</i> if not.
   */
  public abstract boolean equals(final Object object);

  public abstract int hashCode();

  /**
   * Gets the distance between adjacent rows in the grid in real world x,y coordinates. 
   *  
   * @return the row spacing.
   */
  public abstract double getRowSpacing();

  /**
   * Gets the distance between adjacent columns in the grid in real world x,y coordinates. 
   * 
   * @return the column spacing.
   */
  public abstract double getColumnSpacing();

  /**
   * Gets the angle (in degrees) between the east-west axis and the inline direction.
   * 
   * @return the rotation angle.
   */
  public abstract double getRotation();

  /**
   * Computes whether the defining corner points are clockwise or counter-clockwise. 
   * 
   * Works by computing the normal to a plane through the first three points and 
   * then checking the sign of the distance from another point above the plane. 
   * 
   * @return the corner points orientation.
   */
  public abstract Orientation getClockwise();

  /**
   * Returns a flag indicating if the specified row,column is contained in the grid geometry.
   * 
   * @param row the row.
   * @param col the column.
   */
  public abstract boolean containsRowCol(final int row, final int col);

}