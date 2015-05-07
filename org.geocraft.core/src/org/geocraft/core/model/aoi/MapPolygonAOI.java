/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.core.model.aoi;


import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.SpatialExtent;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.InMemoryMapper;
import org.geocraft.core.model.seismic.SeismicSurvey2d;


/**
 * This class represents an area-of-interest (AOI) defined as a collection of polygons in x,y space.
 * <p>
 * Each polygon in the collection is flagged as being <i>inclusive</i> or <i>exclusive</i>.
 * This AOI can be used to define a subset of data on which to operate.
 */
public class MapPolygonAOI extends AreaOfInterest {

  /** The collection of polygonal areas flagged as <i>inclusive</i>. */
  private final List<MapPolygon> _polygons;

  /**
   * Constructs an in-memory AOI defined in x,y space as a collection of polygons.
   * 
   * @param name the name of the AOI.
   */
  public MapPolygonAOI(final String name) {
    this(name, new InMemoryMapper(MapPolygonAOI.class));
  }

  /**
   * Constructs an AOI defined in x,y space as a collection of polygons.
   * <p>
   * This resulting AOI will be lazy-loaded from its underlying datastore when needed.
   * 
   * @param name the name of the AOI.
   * @param mapper the mapper to the underlying datastore.
   */
  public MapPolygonAOI(final String name, final IMapper mapper) {
    super(name, mapper);
    _polygons = Collections.synchronizedList(new ArrayList<MapPolygon>());
    load();
  }

  /**
   * Adds an <i>inclusive</i> polygonal area to the AOI.
   *
   * @param xs the array of x coordinates of the polygonal area.
   * @param ys the array of y coordinates of the polygonal area.
   */
  public void addInclusionPolygon(final double[] xs, final double[] ys) {
    addPolygon(xs, ys, MapPolygon.Type.INCLUSIVE);
    setDirty(true);
  }

  /**
   * Adds an <i>exclusive</i> polygonal area to the AOI.
   *
   * @param xs the array of x coordinates of the polygonal area.
   * @param ys the array of y coordinates of the polygonal area.
   */
  public void addExclusionPolygon(final double[] xs, final double[] ys) {
    addPolygon(xs, ys, MapPolygon.Type.EXCLUSIVE);
    setDirty(true);
  }

  /**
   * Adds a polygonal area to the given path.
   * 
   * @param x the array of x coordinates of the polygonal area.
   * @param y the array of y coordinates of the polygonal area.
   * @param list the list of <i>inclusive</i> or <i>exclusive</i> areas.
   */
  private void addPolygon(final double[] xs, final double[] ys, final MapPolygon.Type type) {
    MapPolygon polygon = new MapPolygon(type, xs, ys);
    _polygons.add(polygon);
  }

  @Override
  public boolean contains(final double x, final double y) {
    load();

    boolean insideAOI = false;
    for (MapPolygon polygon : _polygons.toArray(new MapPolygon[0])) {
      if (polygon.contains(x, y)) {
        // If the x,y coordinate falls within ANY exclusion polygon, then it is not considered inside the AOI.
        if (polygon.isExclusive()) {
          return false;
        }
        insideAOI = true;
      }
    }
    return insideAOI;
  }

  @Override
  public boolean contains(final double x, final double y, final SeismicSurvey2d survey) {
    // Simply ignore the 2D seismic survey and check the x,y.
    return contains(x, y);
  }

  /**
   * Returns an array of the <i>inclusive</i> polygonal areas.
   * 
   * @return the <i>inclusive</i> polygonal areas.
   */
  public MapPolygon[] getPolygons() {
    load();
    return _polygons.toArray(new MapPolygon[0]);
  }

  @Override
  public SpatialExtent getExtent() {
    load();
    double xmin = Double.POSITIVE_INFINITY;
    double xmax = Double.NEGATIVE_INFINITY;
    double ymin = Double.POSITIVE_INFINITY;
    double ymax = Double.NEGATIVE_INFINITY;

    // Find the minimum and maximum x,y coordinates.
    // This includes both the inclusive and exclusive polygons.
    MapPolygon[] polygons = getPolygons();
    for (MapPolygon polygon : polygons) {
      Rectangle2D bounds = polygon.getPath().getBounds2D();
      xmin = Math.min(xmin, bounds.getMinX());
      xmax = Math.max(xmax, bounds.getMaxX());
      ymin = Math.min(ymin, bounds.getMinY());
      ymax = Math.max(ymax, bounds.getMaxY());
    }

    double[] xs = new double[] { xmin, xmax };
    double[] ys = new double[] { ymin, ymax };
    return new SpatialExtent(xs, ys, new double[0], Domain.TIME);
  }
}
