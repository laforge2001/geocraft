/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.geometry;


import org.geocraft.core.common.math.MathUtil;
import org.geocraft.core.common.util.HashCode;
import org.geocraft.core.model.base.ValueObject;
import org.geocraft.core.model.datatypes.CoordinateSeries;


public class GridGeometry2d extends ValueObject {

  /** The array of line geometries in the grid. */
  private final LineGeometry[] _lineGeometries;

  public GridGeometry2d(final String name, final LineGeometry[] lines) {
    super(name);
    _lineGeometries = new LineGeometry[lines.length];
    System.arraycopy(lines, 0, _lineGeometries, 0, lines.length);
  }

  /**
   * Gets the line geometry with the given line name.
   * 
   * @param lineName the line name.
   * @return the line geometry.
   */
  public LineGeometry getLineByName(final String lineName) {
    for (LineGeometry lineGeometry : _lineGeometries) {
      if (lineGeometry.getDisplayName().equals(lineName)) {
        return lineGeometry;
      }
    }
    throw new IllegalArgumentException("Invalid line name: " + lineName);
  }

  /**
   * Gets the line geometry with the given line number.
   * 
   * @param lineNumber the line number.
   * @return the line geometry.
   */
  public LineGeometry getLineByNumber(final int lineNumber) {
    for (LineGeometry lineGeometry : _lineGeometries) {
      if (lineGeometry.getNumber() == lineNumber) {
        return lineGeometry;
      }
    }
    throw new IllegalArgumentException("Invalid line #: " + lineNumber);
  }

  /**
   * Returns the index-th line geometry contained in the grid geometry.
   * 
   * @param index the index of the line geometry to get.
   * @return the index-th line geometry.
   */
  public LineGeometry getLine(final int index) {
    return _lineGeometries[index];
  }

  /**
   * Returns an array of the line geometries in the grid geometry.
   */
  public LineGeometry[] getLines() {
    LineGeometry[] lines = new LineGeometry[_lineGeometries.length];
    System.arraycopy(_lineGeometries, 0, lines, 0, lines.length);
    return lines;
  }

  public int[] getLineNumbers() {
    int numLines = getNumLines();
    int[] lineNumbers = new int[numLines];
    for (int i = 0; i < numLines; i++) {
      lineNumbers[i] = _lineGeometries[i].getNumber();
    }
    return lineNumbers;
  }

  public String[] getLineNames() {
    int numLines = getNumLines();
    String[] lineNames = new String[numLines];
    for (int i = 0; i < numLines; i++) {
      lineNames[i] = _lineGeometries[i].getDisplayName();
    }
    return lineNames;
  }

  /**
   * Returns the # of lines in the grid.
   */
  public int getNumLines() {
    return _lineGeometries.length;
  }

  /**
   * Returns the # of bins in the index-th line geometry.
   * 
   * @param index the index of the line geometry.
   * @return the # of bins.
   */
  public int getNumBins(final int index) {
    return getLine(index).getNumBins();
  }

  /**
   * Returns the # of rows in the grid geometry.
   * This is the same as calling <code>getNumLines</code>.
   * 
   * @return the # of rows.
   */
  public int getNumRows() {
    return getNumLines();
  }

  /**
   * Returns the # of columns in the given row.
   * This is the same as calling <code>getNumBins</code>.
   * 
   * @param row the row.
   * @return the # of columns.
   */
  public int getNumColumns(final int row) {
    return getNumBins(row);
  }

  /**
   * Returns a flag indicating if the geometry matches that of the specified grid geometry.
   * The geometries are considered a match if they contain the same corner points, number of rows and number of columns.
   *
   * @param geometry the geometry to compare.
   * @return <i>true</i> if geometries match; <i>false</i> if not.
   */
  public boolean matchesGeometry(final GridGeometry2d gridGeometry) {
    // Check that the number of lines are equal.
    if (getNumLines() != gridGeometry.getNumLines()) {
      return false;
    }

    // Check each line.
    int numLines = getNumLines();
    for (int i = 0; i < numLines; i++) {
      // Check that the # of points in each line are equal.
      CoordinateSeries points1 = getLine(i).getPoints();
      CoordinateSeries points2 = gridGeometry.getLine(i).getPoints();
      if (points1.getNumPoints() != points2.getNumPoints()) {
        return false;
      }
      // Check that the point x,y coordinates are equal.
      for (int j = 0; j < points1.getNumPoints(); j++) {
        if (!MathUtil.isEqual(points1.getX(j), points2.getX(j)) || !MathUtil.isEqual(points1.getY(j), points2.getY(j))) {
          return false;
        }
      }
    }

    // The match conditions have been satisfied.
    return true;
  }

  /**
   * Checks if an object is "equal" to this one.
   * To be considered "equal", the object be an instance of the
   * <code>GridGeometry2d</code> class, and its corner points and
   * row,column definitions must be the same.
   * 
   * @param object the object to check for equality.
   * @return <i>true</i> if the object is "equal"; <i>false</i> if not.
   */
  @Override
  public boolean equals(final Object object) {
    // Check that it is an instance of a grid geometry.
    if (object != null && object instanceof GridGeometry2d) {
      GridGeometry2d grid = (GridGeometry2d) object;
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
    hashCode.add(getNumLines());
    for (LineGeometry line : getLines()) {
      hashCode.add(line);
    }
    return hashCode.getHashCode();
  }

  @Override
  public String toString() {
    return getDisplayName();
  }

}
