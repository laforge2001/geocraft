/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */
package org.geocraft.core.model.culture;


import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.InMemoryMapper;


/**
 * A GIS feature comprised of one or more disjoint polyline parts.
 */
public class PolylineFeature extends Feature {

  /**
   * An optional measure associated with each point. The measures array will have the same number of
   * elements, in the same order, as Points array. A measure will increase (or decrease)
   * monotonically along the polyline. If the points do not have measure values (IsMeasured is
   * false), the measures array will have zero elements.
   */
  private double[] _measures;

  /** The number of disjoint line parts. */
  private int _numParts;

  /**
   * An array representing the number of points in each line part. There will be numParts entries in
   * this array.
   */
  private int[] _numPointsPerPart;

  /**
   * An ordered array of points that define the Polyline. Points in the same part are contiguous.
   * NumPointsPerPoint can be used to find out how many points are in each part. The number of
   * points in this array will be equal to the sum of all the elements in NumPointsPerPart. If the
   * points do not have z-values (Is3D is false), the z-values will be 0.0 and should be ignored.
   */
  private CoordinateSeries _points;

  public PolylineFeature(final String name, final Layer layer) {
    this(name, new InMemoryMapper(PolylineFeature.class), layer);
  }

  /**
   * TODO 360 parameterized constructor
   * @param name of the entity.
   * @param mapper
   * @param layer this feature belongs in.
   */
  public PolylineFeature(final String name, final IMapper mapper, final Layer layer) {
    super(name, mapper, layer);
  }

  /**
   * An optional measure associated with each point. The measures array will have the same number of
   * elements, in the same order, as Points array. A measure will increase (or decrease)
   * monotonically along the polyline. If the points do not have measure values (IsMeasured is
   * false), the measures array will have zero elements.
   * @return measures
   */
  public double[] getMeasures() {
    return _measures;
  }

  /**
   * The number of disjoint line parts.
   * @return numParts
   */
  public int getNumParts() {
    return _numParts;
  }

  /**
   * An array representing the number of points in each line part. There will be numParts entries in
   * this array.
   * @return numPointsPerPart
   */
  public int[] getNumPointsPerPart() {
    return _numPointsPerPart;
  }

  /**
   * An ordered array of points that define the Polyline. Points in the same part are contiguous.
   * NumPointsPerPoint can be used to find out how many points are in each part. The number of
   * points in this array will be equal to the sum of all the elements in NumPointsPerPart. If the
   * points do not have z-values (Is3D is false), the z-values will be 0.0 and should be ignored.
   * @return points
   */
  public CoordinateSeries getPoints() {
    return _points;
  }

  /**
   * An optional measure associated with each point. The measures array will have the same number of
   * elements, in the same order, as Points array. A measure will increase (or decrease)
   * monotonically along the polyline. If the points do not have measure values (IsMeasured is
   * false), the measures array will have zero elements.
   * @param measures
   */
  public void setMeasures(final double[] measures) {
    _measures = measures;
    setDirty(true);
  }

  /**
   * The number of disjoint line parts.
   * @param numParts
   */
  public void setNumParts(final int numParts) {
    _numParts = numParts;
    setDirty(true);
  }

  /**
   * An array representing the number of points in each line part. There will be numParts entries in
   * this array.
   * @param numPointsPerPart
   */
  public void setNumPointsPerPart(final int[] numPointsPerPart) {
    _numPointsPerPart = numPointsPerPart;
    setDirty(true);
  }

  /**
   * An ordered array of points that define the Polyline. Points in the same part are contiguous.
   * NumPointsPerPoint can be used to find out how many points are in each part. The number of
   * points in this array will be equal to the sum of all the elements in NumPointsPerPart. If the
   * points do not have z-values (Is3D is false), the z-values will be 0.0 and should be ignored.
   * @param points
   */
  public void setPoints(final CoordinateSeries points) {
    _points = points;
    setDirty(true);
  }
}
