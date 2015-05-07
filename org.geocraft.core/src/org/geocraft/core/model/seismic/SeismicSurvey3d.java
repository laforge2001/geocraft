/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.core.model.seismic;


import java.io.Serializable;

import org.geocraft.core.common.util.HashCode;
import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.CornerPointsSeries;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.geometry.GridGeometry3d;


/**
 * This class defines a two-dimension seismic geometry.
 * The geometry is defined by supplying the 4 corner points and the ranges
 * of inlines and xlines. The ordering of the corner points is defined by
 * the following inline,xline pairs: (inlineStart, xlineStart), (inlineEnd, xlineStart),
 * (inlineEnd, xlineEnd), (inlineStart,xlineEnd).
 * This ordering is illustrated below:<br>
 * <br>
 *   3-----------------------2   nrows-1    xlineEnd<br>
 *   |...|...|...|...|...|...|<br>
 *   |---+---+---+-----------|     ^<br>
 *   |...|...|...|...|...|...|     |<br>
 *   |---+---+---+-----------|     |<br>
 *   |...|...|...|...|...|...|<br>
 *   0-----------------------1     0        xlineStart<br>
 *   <br>
 *   0            --->   ncols-1<br>
 *   inlineStart  --->   inlineEnd<br>
 *<br>
 * A seismic geometry is just an extension of a grid geometry, and thus there is
 * a direct correlation between columns and inlines, and between rows and xlines.
 * Once created, the seismic geometry is immutable and it attributes cannot be changed.
 * A seismic geometry is considered equal to another grid geometry is their corner points
 * and inline,xline range definitions are the same.
 */
public class SeismicSurvey3d extends GridGeometry3d implements Serializable {

  /** The inline range (start,end,delta) of the seismic geometry. */
  private final int _inlineCount;

  private final float _inlineStart;

  private final float _inlineEnd;

  private final float _inlineDelta;

  /** The xline range (start,end,delta) of the seismic geometry. */
  private final int _xlineCount;

  private final float _xlineStart;

  private final float _xlineEnd;

  private final float _xlineDelta;

  private final SurveyOrientation _orientation;

  private static int findNumRows(final FloatRange inlineRange, final FloatRange xlineRange,
      final SurveyOrientation orientation) {
    switch (orientation) {
      case ROW_IS_INLINE:
        return inlineRange.getNumSteps();
      case ROW_IS_XLINE:
        return xlineRange.getNumSteps();
    }
    throw new RuntimeException("Invalid survey orientation.");
  }

  private static int findNumCols(final FloatRange inlineRange, final FloatRange xlineRange,
      final SurveyOrientation orientation) {
    switch (orientation) {
      case ROW_IS_INLINE:
        return xlineRange.getNumSteps();
      case ROW_IS_XLINE:
        return inlineRange.getNumSteps();
    }
    throw new RuntimeException("Invalid survey orientation.");
  }

  /**
   * Constructs a seismic geometry by supplying 4 corner points and
   * the ranges of inlines and crosslines.
   * The ordering of points in the grid geometry is defined as follows:<br>
   * <br>
   *   3-----------------------2   nrows-1    xlineEnd<br>
   *   |...|...|...|...|...|...|<br>
   *   |---+---+---+-----------|     ^<br>
   *   |...|...|...|...|...|...|     |<br>
   *   |---+---+---+-----------|     |<br>
   *   |...|...|...|...|...|...|<br>
   *   0-----------------------1     0        xlineStart<br>
   *   <br>
   *   0            --->   ncols-1<br>
   *   inlineStart  --->   inlineEnd<br>
   *   <br>
   * @param name the name the grid geometry.
   * @param numRows the number of rows in the grid geometry.
   * @param numColumns the number of columns in the grid geometry.
   * @param cornerPoints the series containing the 4 corner points of the grid geometry.
   */
  public SeismicSurvey3d(final String name, final FloatRange inlineRange, final FloatRange xlineRange, final CornerPointsSeries cornerPoints, final SurveyOrientation orientation) {
    super(name, findNumRows(inlineRange, xlineRange, orientation), findNumCols(inlineRange, xlineRange, orientation),
        cornerPoints);
    _inlineCount = inlineRange.getNumSteps();
    _inlineStart = inlineRange.getStart();
    _inlineEnd = inlineRange.getEnd();
    _inlineDelta = inlineRange.getDelta();
    _xlineCount = xlineRange.getNumSteps();
    _xlineStart = xlineRange.getStart();
    _xlineEnd = xlineRange.getEnd();
    _xlineDelta = xlineRange.getDelta();
    _orientation = orientation;
  }

  /**
   * Returns the number of inlines in the seismic geometry.
   */
  public int getNumInlines() {
    return _inlineCount;
  }

  /**
   * Returns the number of xlines in the seismic geometry.
   */
  public int getNumXlines() {
    return _xlineCount;
  }

  /**
   * Returns the inline range (start,end,delta) of the seismic geometry.
   */
  public FloatRange getInlineRange() {
    return new FloatRange(_inlineStart, _inlineEnd, _inlineDelta);
  }

  /**
   * Returns the starting inline of the seismic geometry.
   */
  public float getInlineStart() {
    return _inlineStart;
  }

  /**
   * Returns the ending inline of the seismic geometry.
   */
  public float getInlineEnd() {
    return _inlineEnd;
  }

  /**
   * Returns the delta inline of the seismic geometry.
   */
  public float getInlineDelta() {
    return _inlineDelta;
  }

  /**
   * Returns the xline range (start,end,delta) of the seismic geometry.
   */
  public FloatRange getXlineRange() {
    return new FloatRange(_xlineStart, _xlineEnd, _xlineDelta);
  }

  /**
   * Returns the starting xline of the seismic geometry.
   */
  public float getXlineStart() {
    return _xlineStart;
  }

  /**
   * Returns the ending xline of the seismic geometry.
   */
  public float getXlineEnd() {
    return _xlineEnd;
  }

  /**
   * Returns the delta xline of the seismic geometry.
   */
  public float getXlineDelta() {
    return _xlineDelta;
  }

  /**
   * Transforms inline,xline coordinates to x,y coordinates.
   * The x,y coordinate is returned in the form of a double array
   * of length=2. The 1st element (index=0) is the x value, and the
   * 2nd element (index=1) is the y value.
   * 
   * @param inline the inline coordinate.
   * @param xline the xline coordinate.
   * @return the x,y coordinates (as an array of length=2).
   */
  public double[] transformInlineXlineToXY(final float inline, final float xline) {
    // First transform inline,xline to row,col.
    double[] rc = transformInlineXlineToRowCol(inline, xline);
    // Then transform row,col to x,y.
    return transformRowColToXY(rc[0], rc[1]);
  }

  /**
   * Transforms inline,xline coordinates to x,y coordinates.
   * The x,y coordinate is returned in the form of a double array
   * of length=2. The 1st element (index=0) is the x value, and the
   * 2nd element (index=1) is the y value.
   * 
   * @param inline the inline coordinate.
   * @param xline the xline coordinate.
   * @param checkBounds true if bounds checking should be applied in rc->xy transform
   * @return the x,y coordinates (as an array of length=2).
   */
  public double[] transformInlineXlineToXY(final float inline, final float xline, final boolean checkBounds) {
    // First transform inline,xline to row,col.
    double[] rc = transformInlineXlineToRowCol(inline, xline);
    // Then transform row,col to x,y.
    return transformRowColToXY(rc[0], rc[1], checkBounds);
  }

  /**
   * Transforms the specified x,y coordinate to an inline,xline coordinate.
   * The inline,xline coordinate is returned in the form of a double array
   * of length=2. The 1st element (index=0) is the inline value, and the
   * 2nd element (index=1) is the xline value.
   * 
   * @param x x coordinate.
   * @param y y coordinate.
   * @param round if true rounds to the nearest inline, xline.
   * @return the inline,xline coordinates (as an array of length=2).
   */
  public float[] transformXYToInlineXline(final double x, final double y, final boolean round) {
    // First transform x,y to row,col.
    double[] rc = transformXYToRowCol(x, y, round);
    // Then transform row,col to inline,xline.
    return transformRowColToInlineXline(rc[0], rc[1]);
  }

  /**
   * Transforms row,column coordinates to inline,xline coordinates.
   * The inline,xline coordinate is returned in the form of a float array
   * of length=2. The 1st element (index=0) is the inline value, and the
   * 2nd element (index=1) is the xline value.
   * 
   * @param row row coordinate.
   * @param col column coordinate.
   * @return the inline,xline coordinates (as an array of length=2).
   */
  public float[] transformRowColToInlineXline(final double row, final double col) {
    float inline = 0;
    float xline = 0;
    switch (_orientation) {
      case ROW_IS_INLINE:
        inline = (float) (_inlineStart + row * _inlineDelta);
        xline = (float) (_xlineStart + col * _xlineDelta);
        break;
      case ROW_IS_XLINE:
        inline = (float) (_inlineStart + col * _inlineDelta);
        xline = (float) (_xlineStart + row * _xlineDelta);
        break;
    }
    return new float[] { inline, xline };
  }

  /**
   * Transforms inline,xline coordinates to row,column coordinates.
   * The row,col coordinate is returned in the form of a double array
   * of length=2. The 1st element (index=0) is the row value, and the
   * 2nd element (index=1) is the col value.
   * 
   * @param row row coordinate.
   * @param col column coordinate.
   * @return the row,col coordinates (as an array of length=2).
   */
  public double[] transformInlineXlineToRowCol(final float inline, final float xline) {
    double row = Double.NaN;
    double col = Double.NaN;
    switch (_orientation) {
      case ROW_IS_INLINE:
        row = (inline - _inlineStart) / _inlineDelta;
        col = (xline - _xlineStart) / _xlineDelta;
        break;
      case ROW_IS_XLINE:
        col = (inline - _inlineStart) / _inlineDelta;
        row = (xline - _xlineStart) / _xlineDelta;
        break;
    }
    return new double[] { row, col };
  }

  /**
   * Transforms the specified inline,xline coordinates to a series
   * of x,y coordinates. Compute these locations using a bi-linear
   * interpolation of the corner points.
   * 
   * @param inlines the array of inline values.
   * @param xlines the array of xline values.
   * @return a coordinates of x,y coordinates.
   */
  public CoordinateSeries transformInlineXlineToXY(final float[] inlines, final float[] xlines) {

    // If the inline,xline array sizes do not match, throw an exception.
    if (inlines.length != xlines.length) {
      throw new IllegalArgumentException("The inline and xline arrays must be of same length.");
    }

    /*
     * Bilinear interpolation
     * 
     * 1--------------------2 
     * |                    | 
     * |                    | 
     * |                    | 
     * p1----------p--------p2 
     * |                    | 
     * |                    | 
     * |                    | 
     * |                    |
     * 0--------------------3
     * 
     */

    Point3d[] points = new Point3d[inlines.length];
    Point3d[] corners = getCornerPoints().getPointsDirect();
    double inlineDiff = _inlineEnd - _inlineStart;
    double xlineDiff = _xlineEnd - _xlineStart;
    double x;
    double y;
    double[] weights = new double[4];
    double x0 = corners[0].getX();
    double x1 = corners[1].getX();
    double x2 = corners[2].getX();
    double x3 = corners[3].getX();
    double y0 = corners[0].getY();
    double y1 = corners[1].getY();
    double y2 = corners[2].getY();
    double y3 = corners[3].getY();
    switch (_orientation) {
      case ROW_IS_INLINE:
        for (int i = 0; i < inlines.length; i++) {
          double xlinePercent = (xlines[i] - _xlineStart) / xlineDiff;
          weights[0] = 1 - xlinePercent;
          weights[1] = xlinePercent;
          weights[2] = xlinePercent;
          weights[3] = 1 - xlinePercent;
          double inlinePercent = (inlines[i] - _inlineStart) / inlineDiff;
          weights[0] *= 1 - inlinePercent;
          weights[1] *= 1 - inlinePercent;
          weights[2] *= inlinePercent;
          weights[3] *= inlinePercent;
          x = x0 * weights[0] + x1 * weights[1] + x2 * weights[2] + x3 * weights[3];
          y = y0 * weights[0] + y1 * weights[1] + y2 * weights[2] + y3 * weights[3];
          points[i] = new Point3d(x, y, 0);
        }
        break;
      case ROW_IS_XLINE:
        for (int i = 0; i < inlines.length; i++) {
          double inlinePercent = (inlines[i] - _inlineStart) / inlineDiff;
          weights[0] = 1 - inlinePercent;
          weights[1] = inlinePercent;
          weights[2] = inlinePercent;
          weights[3] = 1 - inlinePercent;
          double xlinePercent = (xlines[i] - _xlineStart) / xlineDiff;
          weights[0] *= 1 - xlinePercent;
          weights[1] *= 1 - xlinePercent;
          weights[2] *= xlinePercent;
          weights[3] *= xlinePercent;
          x = x0 * weights[0] + x1 * weights[1] + x2 * weights[2] + x3 * weights[3];
          y = y0 * weights[0] + y1 * weights[1] + y2 * weights[2] + y3 * weights[3];
          points[i] = new Point3d(x, y, 0);
        }
        break;
    }

    // Create and return the coordinate series of points.
    return CoordinateSeries.createDirect(points, getCornerPoints().getCoordinateSystem());
  }

  /**
   * Checks if an object is "equal" to this one.
   * To be considered "equal", the object be an instance of the
   * <code>SeismicSurvey3d</code> class, and its x,y coordinates
   * must be the same.
   * 
   * @param object the object to check for equality.
   * @return <i>true</i> if the object is "equal"; <i>false</i> if not.
   */
  @Override
  public boolean equals(final Object object) {
    // Check that it is an instance of a seismic geometry.
    if (object instanceof SeismicSurvey3d) {
      SeismicSurvey3d geometry = (SeismicSurvey3d) object;
      // First compare the display names.
      if (!getDisplayName().equals(geometry.getDisplayName())) {
        return false;
      }
      // Then compare the geometries.
      if (!matchesGeometry(geometry)) {
        return false;
      }
      // Then check the equality conditions of the grid geometry.
      return super.equals(geometry);
    }
    return false;
  }

  /**
   * Returns a flag indicating if the geometry matches that of the specified seismic geometry.
   * The geometries are considered a match if they contain the same corner points, number of rows, number of columns,
   * and inline and xline ranges.
   * 
   * @param geometry the geometry to compare.
   * @return <i>true</i> if geometries match; <i>false</i> if not.
   */
  public boolean matchesGeometry(final SeismicSurvey3d geometry) {
    // Check that the inline numbers are equal.
    if (getInlineStart() != geometry.getInlineStart()) {
      return false;
    }
    if (getInlineEnd() != geometry.getInlineEnd()) {
      return false;
    }
    if (getInlineDelta() != geometry.getInlineDelta()) {
      return false;
    }
    // Check that the xline ranges are equal.
    if (getXlineStart() != geometry.getXlineStart()) {
      return false;
    }
    if (getXlineEnd() != geometry.getXlineEnd()) {
      return false;
    }
    if (getXlineDelta() != geometry.getXlineDelta()) {
      return false;
    }
    // Check the grid geometry equality conditions.
    return super.matchesGeometry(geometry);
  }

  @Override
  public int hashCode() {
    HashCode hashCode = new HashCode();
    hashCode.add(super.hashCode());
    hashCode.add(_inlineStart);
    hashCode.add(_inlineEnd);
    hashCode.add(_inlineDelta);
    hashCode.add(_xlineStart);
    hashCode.add(_xlineEnd);
    hashCode.add(_xlineDelta);
    return hashCode.getHashCode();
  }

  @Override
  public String toString() {
    return getDisplayName();
  }

  public SurveyOrientation getOrientation() {
    return _orientation;
  }
}
