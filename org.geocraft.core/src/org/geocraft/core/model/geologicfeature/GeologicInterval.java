/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.core.model.geologicfeature;


import java.util.HashSet;
import java.util.Set;

import org.geocraft.core.model.mapper.IMapper;


/**
 * An interval bounded by two FeatureBoundaries.  An interval may have zero or more subIntervals,
 * and a subInterval has one or more layers.
 */

public class GeologicInterval extends GeologicFeature {

  /**
   * Feature bounded at the base of the interval.
   */
  private GeologicFeature _baseBoundary;

  /**
   * Attribute to define how the layers are infilled between the top and base feature
   * boundaries. The infillType is RVS controlled. TODO 360 what does this mean?
   */
  private String _infillType;

  /**
   * Number of layers in the interval, if this is a leaf interval.
   */
  private int _numLayers;

  /**
   * Parent of this interval, if this is a subinterval.
   * If Interval is null, the interval is the top-most interval.
   */
  private GeologicFeature _parent;

  /**
   * List of subintervals, if any.
   * The subintervals in the list are ordered from top to base.
   */
  private final Set<GeologicFeature> _subIntervals = new HashSet<GeologicFeature>();

  /**
   * Feature bounded at the top of the interval.
   */
  private GeologicFeature _topBoundary;

  /**
   * Parameterized constructor.
   * @param name Name of the entity.
   * @param mapper desired storage properties
   */
  public GeologicInterval(final String name, final IMapper mapper) {
    super(name, FeatureType.INTERVAL, mapper);
  }

  /**
   * Feature bounded at the base of the interval.
   * @return baseBoundary
   */
  public GeologicFeature getBaseBoundary() {
    return _baseBoundary;
  }

  /**
   * Attribute to define how the layers are infilled between the top and base feature
   * boundaries.
   *
   * @return infillType
   */
  public String getInfillType() {
    return _infillType;
  }

  /**
   * Number of layers in the interval, if this is a leaf interval.
   *
   * @return numLayers
   * 
   * TODO 360? 
   */
  public int getNumLayers() {
    return _numLayers;
  }

  /**
   * Parent of this interval, if this is a subinterval.
   * If Interval is null, the interval is the top-most interval.
   *
   * @return parent
   */
  public GeologicFeature getParent() {
    return _parent;
  }

  /**
   * List of subintervals, if any.
   *
   * @return subIntervals
   */
  public GeologicFeature[] getSubIntervals() {

    GeologicFeature[] subIntervalsArray = new GeologicFeature[_subIntervals.size()];

    _subIntervals.toArray(subIntervalsArray);
    return subIntervalsArray;
  }

  /**
   * Feature bounded at the top of the interval.
   *
   * @return topBoundary
   */
  public GeologicFeature getTopBoundary() {
    return _topBoundary;
  }

  /**
   * Feature bounded at the base of the interval.
   *
   * @param baseBoundary
   */
  public void setBaseBoundary(final GeologicFeature baseBoundary) {
    _baseBoundary = baseBoundary;
  }

  /**
   * Attribute to define how the layers are infilled between the top and base feature
   * boundaries.
   *
   * @param infillType
   */
  public void setInfillType(final String infillType) {
    _infillType = infillType;
  }

  /**
   * Number of layers in the interval, if this is a leaf interval.
   * @param numLayers
   */
  public void setNumLayers(final int numLayers) {
    _numLayers = numLayers;
  }

  /**
   * Parent of this interval, if this is a subinterval.
   * If Interval is null, the interval is the top-most interval.
   * @param parent
   */
  // TODO:  parent cannot be changed.
  public void setParent(final GeologicFeature parent) {
    _parent = parent;
  }

  /**
   * Add an array of GeologicFeature, ignoring duplicates.
   *
   * @param subIntervals
   */
  public void addToSubIntervals(final GeologicFeature[] subIntervals) {

    for (GeologicFeature subInterval : subIntervals) {
      _subIntervals.add(subInterval);
    }
  }

  /**
   * Remove elements specified in the parameter.
   *
   * @param subIntervals List of subintervals.
   */
  public void removeFromSubIntervals(final GeologicFeature[] subIntervals) {

    for (GeologicFeature subInterval : subIntervals) {
      _subIntervals.remove(subInterval);
    }
  }

  /**
   * Removes all subIntervals.
   */
  public void removeAllSubIntervals() {
    _subIntervals.clear();
  }

  /**
   * Feature bounded at the top of the interval.
   *
   * @param topBoundary
   */
  public void setTopBoundary(final GeologicFeature topBoundary) {
    _topBoundary = topBoundary;
  }
}
