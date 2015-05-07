/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.core.model.culture;


import java.util.ArrayList;
import java.util.List;

import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.InMemoryMapper;


/**
 * A GIS feature comprised of one or more non-intersecting SimplePolygons. 
 * A SimplePolygon has one outer boundary and zero or more interior holes.
 */

public class PolygonFeature extends Feature {

  /** References to the SimplePolygons for this PolygonFeature. */
  // TODO set
  private final List<SimplePolygon> _polygons = new ArrayList<SimplePolygon>();

  public PolygonFeature(final String name, final Layer layer) {
    this(name, new InMemoryMapper(PolygonFeature.class), layer);
  }

  /**
   * TODO better comment parameterized constructor.
   *
   * @param name of the entity.
   * @param mapper 
   * @param layer this feature belongs in.
   */
  public PolygonFeature(final String name, final IMapper mapper, final Layer layer) {
    super(name, mapper, layer);
  }

  /**
   * The number of SimplePolygons for this PolygonFeature.
   *
   * @return numPolygons
   */
  public int getNumPolygons() {
    return _polygons.size();
  }

  /**
   * References to the SimplePolygons for this PolygonFeature.
   *
   * @return polygons
   */
  public SimplePolygon[] getPolygons() {
    SimplePolygon[] polygonsArray = new SimplePolygon[_polygons.size()];
    _polygons.toArray(polygonsArray);
    return polygonsArray;
  }

  /**
   * Adds an array of SimplePolygon, ignoring duplicates.
   *
   * @param polygons to add to this feature. 
   */
  public void addToPolygons(final SimplePolygon[] polygons) {
    for (SimplePolygon polygon : polygons) {
      if (!_polygons.contains(polygon)) {
        _polygons.add(polygon);
      }
    }
  }

  /**
   * Remove SimplePolygon elements from this feature.
   *
   * @param polygons to remove from this feature. 
   */
  public void removeFromPolygons(final SimplePolygon[] polygons) {
    for (SimplePolygon polygon : polygons) {
      _polygons.remove(polygon);
    }
  }

  /**
   * Removes all polygons.
   */
  public void removeAllPolygons() {
    _polygons.clear();
  }

}
