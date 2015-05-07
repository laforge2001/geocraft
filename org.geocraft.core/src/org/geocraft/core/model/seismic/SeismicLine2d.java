/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.core.model.seismic;


import org.geocraft.core.common.util.HashCode;
import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.geometry.LineGeometry;


/**
 * This class describes a 2D seismic line value object.
 * The principal job of this geometry is to defines the relationship between
 * inline/xline coordinates and real-world map coordinates. The geometry is
 * contained within a 2D seismic survey and has no knowledge of the datasets
 * that might have reference to it.
 * <p>
 * This class is immutable and thus thread-safe.
 */
public final class SeismicLine2d extends LineGeometry {

  /** The CDP range (start,end,delta) of the 2D seismic line. */
  private final FloatRange _cdpRange;

  /** The shotpoint start value of the 2D seismic line. */
  private final float _shotpointStart;

  /** The shotpoint end value of the 2D seismic line. */
  private final float _shotpointEnd;

  /** The coordinate transform, used to convert between CDP, shotpoint and bin locations. */
  private final ISeismicLineCoordinateTransform _coordTransform;

  /**
   * Constructs a 2D seismic line.
   * 
   * @param name the name of the seismic line.
   * @param lineNumber the seismic line #.
   * @param cdpRange the CDP range (start,end,delta) of the seismic line.
   * @param shotpointStart the starting shotpoint of the seismic line.
   * @param shotpointEnd the ending shotpoint of the seismic line.
   * @param xyCoordinates the x,y coordinates of the seismic line bins.
   * @param coordTransform the coordinate transform of the seismic line.
   */
  public SeismicLine2d(final String name, final int lineNumber, final FloatRange cdpRange, final float shotpointStart, final float shotpointEnd, final CoordinateSeries xyCoordinates, final ISeismicLineCoordinateTransform coordTransform) {
    super(name, lineNumber, xyCoordinates);
    _cdpRange = cdpRange;
    _shotpointStart = shotpointStart;
    _shotpointEnd = shotpointEnd;
    _coordTransform = coordTransform;
  }

  /**
   * Returns the coordinate transform for the 2D seismic line.
   * <p>
   * This is used to convert between CDP, shotpoint and bin locations.
   * 
   * @return the coordinate transform.
   */
  public ISeismicLineCoordinateTransform getCoordinateTransform() {
    return _coordTransform;
  }

  /**
   * Returns the starting CDP of the 2D seismic line.
   * 
   * @return the starting CDP of the 2D seismic line.
   */
  public float getCDPStart() {
    return _cdpRange.getStart();
  }

  /**
   * Returns the ending CDP of the 2D seismic line.
   * 
   * @return the ending CDP of the 2D seismic line.
   */
  public float getCDPEnd() {
    return _cdpRange.getEnd();
  }

  /**
   * Returns the delta CDP of the 2D seismic line.
   * 
   * @return the delta CDP of the 2D seismic line.
   */
  public float getCDPDelta() {
    return _cdpRange.getDelta();
  }

  /**
   * Returns the starting shotpoint of the 2D seismic line.
   * 
   * @return the starting shotpoint of the 2D seismic line.
   */
  public float getShotpointStart() {
    return _shotpointStart;
  }

  /**
   * Returns the ending shotpoint of the 2D seismic line.
   * 
   * @return the ending shotpoint of the 2D seismic line.
   */
  public float getShotpointEnd() {
    return _shotpointEnd;
  }

  /**
   * Transforms a CDP coordinate to a bin coordinate.
   * <p>
   * The CDP coordinate must fall within the CDP range of the 2D seismic line.
   * Otherwise an <code>IndexOutOfBoundsException</code> will be thrown.
   * 
   * @param cdp the CDP coordinate.
   * @return the bin coordinate.
   * @throws IndexOutOfBoundsException thrown if the CDP is outside the range of the line.
   */
  public double transformCdpToBin(final float cdp) throws IndexOutOfBoundsException {
    int numPoints = getNumBins();
    float bin = (cdp - _cdpRange.getStart()) / _cdpRange.getDelta();
    if (bin < 0 || bin > numPoints - 1) {
      throw new IndexOutOfBoundsException("CDP " + cdp + " out of bounds (" + _cdpRange.getStart() + ","
          + _cdpRange.getEnd() + ").");
    }
    return Math.round(bin);
  }

  /**
   * Transforms a bin coordinate to a CDP coordinate.
   * <p>
   * The CDP coordinate returned will be the nearest one found
   * on the CDP stride (delta), even if the bin index is a
   * fractional number (e.g. 3.7).
   * The bin coordinate must fall within the number of points of the 2D seismic line.
   * Otherwise an <code>IndexOutOfBoundsException</code> will be thrown.
   * 
   * @param bin the fractional bin index.
   * @return the nearest CDP number.
   * @throws IndexOutOfBoundsException thrown if the bin is outside the range of the line.
   */
  public float transformBinToCdp(final double bin) throws IndexOutOfBoundsException {
    // Round to the bin coordinate to the nearest integer.
    int binIndex = roundToNearestBinIndex(bin);
    // Return the CDP at the bin coordinate.
    return _cdpRange.getValue(binIndex);
  }

  /**
   * Transforms a shotpoint coordinate to a bin coordinate.
   * <p>
   * The shotpoint coordinate must fall within the shotpoint range of the 2D seismic line.
   * Otherwise an <code>IndexOutOfBoundsException</code> will be thrown.
   * 
   * @param shotpoint the shotpoint coordinate.
   * @return the bin coordinate.
   * @throws IndexOutOfBoundsException thrown if the shotpoint is outside the range of the line.
   */
  public double transformShotpointToBin(final float shotpoint) throws IndexOutOfBoundsException {
    float cdp = _coordTransform.transformShotpointToCDP(shotpoint);
    return transformCdpToBin(cdp);
  }

  /**
   * Transforms a bin coordinate to a shotpoint coordinate.
   * <p>
   * The shotpoint coordinate returned will be the nearest one found
   * on the shotpoint stride (delta), even if the bin index is a
   * fractional number (e.g. 3.7).
   * The bin coordinate must fall within the number of points of the 2D seismic line.
   * Otherwise an <code>IndexOutOfBoundsException</code> will be thrown.
   * 
   * @param bin the fractional bin index.
   * @return the nearest shotpoint number.
   * @throws IndexOutOfBoundsException thrown if the bin is outside the range of the line.
   */
  public float transformBinToShotpoint(final double bin) throws IndexOutOfBoundsException {
    // Round to the bin coordinate to the nearest integer.
    int binIndex = roundToNearestBinIndex(bin);
    float cdp = transformBinToCdp(binIndex);
    return _coordTransform.transformCDPToShotpoint(cdp);
  }

  /**
   * Transforms a CDP coordinate to a shotpoint coordinate.
   * <p>
   * The CDP coordinate must fall within the CDP range of the 2D seismic line.
   * Otherwise an <code>IndexOutOfBoundsException</code> will be thrown.
   * 
   * @param cdp the CDP coordinate.
   * @return the shotpoint coordinate.
   * @throws IndexOutOfBoundsException thrown if the CDP is outside the range of the line.
   */
  public float transformCDPToShotpoint(final float cdp) throws IndexOutOfBoundsException {
    return _coordTransform.transformCDPToShotpoint(cdp);
  }

  /**
   * Transforms a shotpoint coordinate to a CDP coordinate.
   * <p>
   * The shotpoint coordinate must fall within the shotpoint range of the 2D seismic line.
   * Otherwise an <code>IndexOutOfBoundsException</code> will be thrown.
   * 
   * @param the shotpoint coordinate.
   * @return the CDP coordinate.
   * @throws IndexOutOfBoundsException thrown if the shotpoint is outside the range of the line.
   */
  public float transformShotpointToCDP(final float shotpoint) throws IndexOutOfBoundsException {
    return _coordTransform.transformShotpointToCDP(shotpoint);
  }

  /**
   * Transforms a CDP coordinate to x,y coordinates.
   * <p>
   * The CDP coordinate must fall within the CDP range of the 2D seismic line.
   * Otherwise an <code>IndexOutOfBoundsException</code> will be thrown.
   * 
   * @param cdp the CDP coordinate.
   * @return the x,y coordinates (as array of length=2).
   * @throws IndexOutOfBoundsException thrown if the CDP is outside the range of the line.
   */
  public double[] transformCDPToXY(final float cdp) throws IndexOutOfBoundsException {
    // Transform the CDP coordinate to a bin coordinate.
    double bin = transformCdpToBin(cdp);
    // Transform the bin to x,y coordinates.
    return transformBinToXY(bin);
  }

  /**
   * Transforms a shotpoint coordinate to x,y coordinates.
   * <p>
   * The shotpoint coordinate must fall within the shotpoint range of the 2D seismic line.
   * Otherwise an <code>IndexOutOfBoundsException</code> will be thrown.
   * 
   * @param shotpoint the shotpoint coordinate.
   * @return the x,y coordinates (as array of length=2).
   * @throws IndexOutOfBoundsException thrown if the shotpoint is outside the range of the line.
   */
  public double[] transformShotpointToXY(final float shotpoint) throws IndexOutOfBoundsException {
    // Transform the shotpoint coordinate to a bin coordinate.
    double bin = transformShotpointToBin(shotpoint);
    // Transform the bin to x,y coordinates.
    return transformBinToXY(bin);
  }

  /**
   * Transforms an array of CDP numbers to x,y coordinates.
   * 
   * @param cdps the array of CDP numbers.
   * @return the series containing the x,y coordinates.
   * @throws IllegalArgumentException throw if any of the CDP numbers falls outside the CDP range.
   */
  public CoordinateSeries transformCDPsToXYs(final float[] cdps) {
    Point3d[] points = new Point3d[cdps.length];
    for (int i = 0; i < cdps.length; i++) {
      int index = Math.round((cdps[i] - _cdpRange.getStart()) / _cdpRange.getDelta());
      if (index >= 0 && index < _cdpRange.getNumSteps()) {
        points[i] = getPoints().getPoint(index);
      } else {
        throw new IllegalArgumentException("Invalid cdp: " + cdps[i] + ". Must be in the range " + _cdpRange.getStart()
            + " to " + _cdpRange.getEnd());
      }
    }
    return CoordinateSeries.createDirect(points, getPoints().getCoordinateSystem());
  }

  /**
   * Transforms an array of shotpoint numbers to x,y coordinates.
   * 
   * @param shotpoints the array of shotpoint numbers.
   * @return the series containing the x,y coordinates.
   * @throws IllegalArgumentException throw if any of the shotpoint numbers falls outside the shotpoint range.
   */
  public CoordinateSeries transformShotpointsToXYs(final float[] shotpoints) {
    float[] cdps = _coordTransform.transformShotpointsToCDPs(shotpoints);
    return transformCDPsToXYs(cdps);
  }

  /**
   * Transforms an array of CDP numbers to shotpoint numbers.
   * 
   * @param cdps the array of CDP numbers.
   * @return the array of shotpoint numbers corresponding to the CDP numbers.
   * @throws IllegalArgumentException throw if any of the CDP numbers falls outside the CDP range.
   */
  public float[] transformCDPsToShotpoints(final float[] cdps) {
    return _coordTransform.transformCDPsToShotpoints(cdps);
  }

  /**
   * Transforms an array of shotpoint numbers to CDP numbers.
   * 
   * @param shotpoints the array of shotpoint numbers.
   * @return the array of CDP numbers corresponding to the shotpoint numbers.
   * @throws IllegalArgumentException throw if any of the shotpoint numbers falls outside the shotpoint range.
   */
  public float[] transformShotpointsToCDPs(final float[] shotpoints) {
    return _coordTransform.transformShotpointsToCDPs(shotpoints);
  }

  /**
   * Returns a flag indicating if the 2D seismic line matches another 2D seismic line.
   * The geometries are considered a match if they contain the same number of bins, bin locations, and CDP and shotpoint ranges.
   *
   * @param seismicLine the 2D seismic line to compare.
   * @return <i>true</i> if geometries match; <i>false</i> if not.
   */
  public boolean matchesGeometry(final SeismicLine2d seismicLine) {
    // Check the line numbers.
    if (getNumber() != seismicLine.getNumber()) {
      return false;
    }
    // Check that the CDP numbers are equal.
    if (getCDPStart() != seismicLine.getCDPStart()) {
      return false;
    }
    if (getCDPEnd() != seismicLine.getCDPEnd()) {
      return false;
    }
    if (getCDPDelta() != seismicLine.getCDPDelta()) {
      return false;
    }
    // Check that the shotpoint ranges are equal.
    if (getShotpointStart() != seismicLine.getShotpointStart()) {
      return false;
    }
    if (getShotpointEnd() != seismicLine.getShotpointEnd()) {
      return false;
    }
    // Check the grid geometry equality conditions.
    return super.matchesGeometry(seismicLine);
  }

  /**
   * Checks if an object is "equal" to this 2D seismic line.
   * <p>
   * To be considered "equal", the object be an instance of the
   * <code>SeismicLine2d</code> class, and its x,y coordinates
   * must be the same.
   * 
   * @param object the object to check for equality.
   * @return <i>true</i> if the object is "equal"; <i>false</i> if not.
   */
  @Override
  public boolean equals(final Object object) {
    // Check that it is an instance of a 2D seismic line.
    if (object instanceof SeismicLine2d) {
      SeismicLine2d seismicLine = (SeismicLine2d) object;
      // First compare the display names.
      if (!getDisplayName().equals(seismicLine.getDisplayName())) {
        return false;
      }
      // Then compare the geometries.
      if (!matchesGeometry(seismicLine)) {
        return false;
      }
      // Then check the equality conditions of the line.
      return super.equals(seismicLine);
    }
    return false;
  }

  @Override
  public int hashCode() {
    HashCode hashCode = new HashCode();
    hashCode.add(super.hashCode());
    hashCode.add(getNumber());
    hashCode.add(_cdpRange);
    hashCode.add(_shotpointStart);
    hashCode.add(_shotpointEnd);
    return hashCode.getHashCode();
  }

  @Override
  public String toString() {
    return getDisplayName();
  }

}
