/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.core.model.culture;


import java.util.HashSet;
import java.util.Set;

import org.geocraft.core.model.datatypes.CoordinateSeries;


/**
 * A closed polygon with one outer boundary and zero or more non-intersecting interior holes. The
 * holes are themselves represented as a SimplePolygon, with no holes.
 */

public class SimplePolygon {

  /**
   * Reference to zero or more SimplePolygons which form holes (inner boundaries) in the interior of
   * the parent SimplePolygon. The holes must all be in the interior of the parent SimplePolyon and
   * must not intersect one another. This array will have zero elements if the parent SimplePolygon
   * has no holes.
   */
  private final Set<SimplePolygon> _holes = new HashSet<SimplePolygon>();

  /**
   * True if this SimplePolygon has z-values.
   */
  private boolean _is3D;

  /**
   * True if this SimplePolygon is a hole (an inner boundary).
   */
  private boolean _isHole;

  /**
   * Reference to the Layer that this SimplePolygon belongs to.
   */
  private Layer _layer;

  /**
   * A ordered set of points which define the boundary of this SimplePolygon. The points are ordered
   * in a clockwise direction for an outer boundary and counter clockwise for a hole (an inner
   * boundary). The last point of the boundary is identical to the first to close the polygon. If
   * the points do not have z-values (Is3D is false), the z-values will be 0.0 and should be
   * ignored.
   */
  private CoordinateSeries _points;

  /**
   * Reference to PolygonFeature this SimplePolygon is part of.
   */
  private PolygonFeature _polygonFeature;

  /**
   * Display name for this object.
   */
  private final String _displayName;

  /**
   * parameterized constructor
   * @param name Name of the entity.
   */
  public SimplePolygon(final String name) {
    _displayName = name;
  }

  public String getDisplayName() {
    return _displayName;
  }

  /**
   * Reference to zero or more SimplePolygons which form holes (inner boundaries) in the interior of
   * the parent SimplePolygon. The holes must all be in the interior of the parent SimplePolyon and
   * must not intersect one another. This array will have zero elements if the parent SimplePolygon
   * has no holes.
   * @return holes
   */
  public SimplePolygon[] getHoles() {
    SimplePolygon[] holesArray = new SimplePolygon[_holes.size()];
    _holes.toArray(holesArray);
    return holesArray;
  }

  /**
   * True if this SimplePolygon has z-values.
   * @return is3D
   */
  public boolean is3D() {
    return _is3D;
  }

  /**
   * True if this SimplePolygon is a hole (an inner boundary).
   * @return isHole
   */
  public boolean isHole() {
    return _isHole;
  }

  /**
   * Reference to the Layer that this SimplePolygon belongs to.
   * @return layer
   */
  public Layer getLayer() {
    return _layer;
  }

  /**
   * A ordered set of points which define the boundary of this SimplePolygon. The points are ordered
   * in a clockwise direction for an outer boundary and counter clockwise for a hole (an inner
   * boundary). The last point of the boundary is identical to the first to close the polygon. If
   * the points do not have z-values (Is3D is false), the z-values will be 0.0 and should be
   * ignored.
   * @return points
   */
  public CoordinateSeries getPoints() {
    return CoordinateSeries.create(_points.getPointsDirect(), _points.getCoordinateSystem());
  }

  /**
   * Reference to PolygonFeature this SimplePolygon is part of.
   * @return polygonFeature
   */
  public PolygonFeature getPolygonFeature() {
    return _polygonFeature;
  }

  /**
   * Add an array of SimplePolygon, ignoring duplicates.
   * 
   * @param holes of holes to add to the polygon.
   */
  public void addToHoles(final SimplePolygon[] holes) {
    for (SimplePolygon hole : holes) {
      _holes.add(hole);
    }
  }

  /**
   * Remove elements specified in the parameter.
   * 
   * @param holes of holes to remove from the polygon. 
   */
  public void removeFromHoles(final SimplePolygon[] holes) {
    for (SimplePolygon hole : holes) {
      _holes.remove(hole);
    }
  }

  /**
   * Removes all holes. Reference to zero or more SimplePolygons which form holes (inner boundaries)
   * in the interior of the parent SimplePolygon. The holes must all be in the interior of the
   * parent SimplePolyon and must not intersect one another. This array will have zero elements if
   * the parent SimplePolygon has no holes.
   */
  public void removeAllHoles() {
    _holes.clear();
  }

  /**
   * True if this SimplePolygon has z-values.
   * @param is3D
   */
  public void setIs3D(final boolean is3D) {
    _is3D = is3D;
  }

  /**
   * True if this SimplePolygon is a hole (an inner boundary).
   * @param isHole
   */
  public void setIsHole(final boolean isHole) {
    _isHole = isHole;
  }

  /**
   * Reference to the Layer that this SimplePolygon belongs to.
   * @param layer
   */
  public void setLayer(final Layer layer) {
    _layer = layer;
  }

  /**
   * A ordered set of points which define the boundary of this SimplePolygon. The points are ordered
   * in a clockwise direction for an outer boundary and counter clockwise for a hole (an inner
   * boundary). The last point of the boundary is identical to the first to close the polygon. If
   * the points do not have z-values (Is3D is false), the z-values will be 0.0 and should be
   * ignored.
   * @param points
   */
  public void setPoints(final CoordinateSeries points) {
    _points = points;
  }

  /**
   * Reference to PolygonFeature this SimplePolygon is part of.
   * @param polygonFeature
   */
  public void setPolygonFeature(final PolygonFeature polygonFeature) {
    _polygonFeature = polygonFeature;
  }
}
