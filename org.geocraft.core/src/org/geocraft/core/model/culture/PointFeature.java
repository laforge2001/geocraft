/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.core.model.culture;


import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.InMemoryMapper;


/**
 * A GIS feature comprised of one or more points.
 */

public class PointFeature extends Feature {

  /**
   * An optional measure associated with each point. 
   * 
   * The measures array will have the same number of elements, in the same order, as
   * the Points array. 
   * 
   * If the Points do not have measure values (IsMeasured is false), the measures 
   * array will have zero elements.
   */
  private double[] _measures;

  /**
   * The points in this Point feature. It is possible for there to be more the one 
   * point in a PointFeature. 
   * 
   * If the points do not have z-values (Is3D is false), the z-values will be 0.0 
   * and should be ignored.
   */
  private CoordinateSeries _points;

  public PointFeature(final String name, final Layer layer) {
    this(name, new InMemoryMapper(PointFeature.class), layer);
  }

  /**
   * 
   * @param name of the entity.
   * @param layer this feature belongs in.
   */
  public PointFeature(final String name, final IMapper mapper, final Layer layer) {
    super(name, mapper, layer);
  }

  /**
   * An optional measure associated with each point. The measures array will 
   * have the same number of elements, in the same order, as the Points array. 
   * 
   * @return measures
   */
  public double[] getMeasures() {
    return _measures;
  }

  /**
   * The points in this Point feature. It is possible for there to be more 
   * than one point in a PointFeature. 
   * 
   * @return points
   */
  public CoordinateSeries getPoints() {
    return _points;
  }

  /**
   * An optional measure associated with each point. 
   * The measures array will have the same number of elements, in the same order, as
   * the Points array. If the Points do not have measure values (IsMeasured is false), 
   * the measures array will have zero elements. 
   * 
   * @param measures
   */
  public void setMeasures(final double[] measures) {
    _measures = measures;
    setDirty(true);
  }

  /**
   * The points in this Point feature. It is possible for there to be more than
   * one point in a PointFeature. 
   * 
   * @param points
   */
  public void setPoints(final CoordinateSeries points) {
    _points = points;
    setDirty(true);
  }
}
