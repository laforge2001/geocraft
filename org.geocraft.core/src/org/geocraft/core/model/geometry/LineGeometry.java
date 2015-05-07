/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.geometry;


import org.geocraft.core.common.util.HashCode;
import org.geocraft.core.model.base.ValueObject;
import org.geocraft.core.model.datatypes.CoordinateSeries;


/**
 * This class defines a one-dimension line geometry.
 * The geometry is defined by supplying the x,y coordinates of the bin locations.
 *
 * Once created, the line geometry is immutable and it attributes cannot be changed.
 * A line geometry is considered equal to another line geometry if their x,y coordinates
 * are the same.
 */
public class LineGeometry extends ValueObject {

  /** The line ID number. */
  private final int _lineNumber;

  /** The x,y coordinates of the line geometry. */
  private final CoordinateSeries _xyCoordinates;

  /**
   * Constructs a line geometry by the x,y coordinates at each point.
   *   
   * @param name the name the line geometry.
   * @param xyCoordinates the series containing x,y coordinates.
   */
  public LineGeometry(final String name, final int lineNumber, final CoordinateSeries xyCoordinates) {
    super(name);
    _lineNumber = lineNumber;
    if (xyCoordinates.getNumPoints() < 2) {
      throw new IllegalArgumentException("A line geometry must contain at least 2 points.");
    }
    _xyCoordinates = CoordinateSeries.create(xyCoordinates.getPointsDirect(), xyCoordinates.getCoordinateSystem());
  }

  /**
   * Returns the line ID number.
   */
  public int getNumber() {
    return _lineNumber;
  }

  /**
   * Returns the number of bins (points) in the line geometry.
   * 
   * @return the number of bins.
   */
  public int getNumBins() {
    return _xyCoordinates.getNumPoints();
  }

  /**
   * Returns the series containing the x,y coordinates of the line geometry.
   * 
   * @return the x,y coordinate series.
   */
  public CoordinateSeries getPoints() {
    return _xyCoordinates;
  }

  /**
   * Given the row,column coordinates to x,y coordinates.
   * This is computed using a bilinear interpolation between the 4 corner points.
   * 
   * @param bin the fractional bin index (0 to npoints-1).
   * @return the x,y coordinates (as an array of length=2).
   */
  public double[] transformBinToXY(final double bin) throws IndexOutOfBoundsException {
    int[] binBounds = getBoundsingBinIndices(bin);
    if (binBounds[0] == binBounds[1]) {
      double x = _xyCoordinates.getX(binBounds[0]);
      double y = _xyCoordinates.getY(binBounds[0]);
      return new double[] { x, y };
    }
    // Interpolate between the bounding bins.
    double x0 = _xyCoordinates.getX(binBounds[0]);
    double y0 = _xyCoordinates.getY(binBounds[0]);
    double x1 = _xyCoordinates.getX(binBounds[1]);
    double y1 = _xyCoordinates.getY(binBounds[1]);
    double percent1 = (bin - binBounds[0]) / (binBounds[1] - binBounds[0]);
    double percent0 = 1 - percent1;
    double x = x0 * percent0 + x1 * percent1;
    double y = y0 * percent0 + y1 * percent1;
    return new double[] { x, y };
  }

  /**
   * Checks if an object is "equal" to this one.
   * To be considered "equal", the object be an instance of the
   * <code>LineGeometry</code> class, and its x,y coordinates
   * must be the same.
   * 
   * @param object the object to check for equality.
   * @return <i>true</i> if the object is "equal"; <i>false</i> if not.
   */
  @Override
  public boolean equals(final Object object) {
    // Check that it is an instance of a line geometry.
    if (object instanceof LineGeometry) {
      LineGeometry lineGeometry = (LineGeometry) object;
      // Compare the display names.
      if (!getDisplayName().equals(lineGeometry.getDisplayName())) {
        return false;
      }
      if (getNumber() != lineGeometry.getNumber()) {
        return false;
      }
      // If the display names match, then compare the geometries.
      return matchesGeometry(lineGeometry);
    }
    return false;
  }

  @Override
  public int hashCode() {
    HashCode hashCode = new HashCode();
    hashCode.add(getDisplayName());
    hashCode.add(_lineNumber);
    hashCode.add(_xyCoordinates);
    return hashCode.getHashCode();
  }

  /**
   * Returns the bin indices bounding the specified bin index.
   * For example, a bin index = 3.7 would be bounded by bin indices 3 and 4.
   * Note that bin indices range from 0 to numPoints-1. Thus, in cases where
   * the bin index is beyond either the starting or ending limits, then an
   * <code>IndexOutOfBoundsException</code> is thrown.
   * @param bin the fractional bin index.
   * @return the bounding bin indices.
   */
  protected int[] getBoundsingBinIndices(final double bin) throws IndexOutOfBoundsException {
    int numPoints = getNumBins();
    int binIndex0 = (int) Math.floor(bin);
    int binIndex1 = (int) Math.ceil(bin);
    if (binIndex0 < 0 || binIndex1 >= numPoints) {
      throw new IndexOutOfBoundsException("Bin index " + bin + " out of bounds (0," + (numPoints - 1) + ").");
    }
    return new int[] { binIndex0, binIndex1 };
  }

  /**
   * Rounds the fractional bin index to the nearest integer bin index.
   * For example, a bin index = 3.7 would be bounded by bin index 4.
   * Note that bin indices range from 0 to numPoints-1. Thus, in cases where
   * the bin index is beyond either the starting or ending limits, then an
   * <code>IndexOutOfBoundsException</code> is thrown.
   * @param bin the fractional bin index.
   * @return the nearest integer bin index.
   */
  protected int roundToNearestBinIndex(final double bin) throws IndexOutOfBoundsException {
    int numPoints = getNumBins();
    int binIndex = (int) Math.round(bin);
    if (binIndex < 0 || binIndex >= numPoints) {
      throw new IndexOutOfBoundsException("Bin index " + binIndex + " out of bounds (0," + (numPoints - 1) + ").");
    }
    return binIndex;
  }

  /**
   * Returns a flag indicating if the line geometry matches that of another line geometry.
   * The geometries are considered a match if they contain the same number of bins and bin locations.
   * 
   * @param lineGeometry the line geometry to compare.
   * @return <i>true</i> if geometries match; <i>false</i> if not.
   */
  public boolean matchesGeometry(final LineGeometry lineGeometry) {
    // Check that the number of bins are equal.
    if (getNumBins() != lineGeometry.getNumBins()) {
      return false;
    }
    CoordinateSeries points1 = getPoints();
    CoordinateSeries points2 = lineGeometry.getPoints();
    if (points1.getNumPoints() != points2.getNumPoints()) {
      return false;
    }
    // Check that the point x,y coordinates are equal.
    for (int i = 0; i < points1.getNumPoints(); i++) {
      if (points1.getX(i) != points2.getX(i) || points1.getY(i) != points2.getY(i)) {
        return false;
      }
    }
    // The match conditions have been satisfied.
    return true;
  }
}
