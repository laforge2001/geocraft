/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */
package org.geocraft.core.model;


import java.util.HashMap;
import java.util.Map;

import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.FloatMeasurement;
import org.geocraft.core.model.geologicfeature.GeologicFeature;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.InMemoryMapper;


/**
 * A collection of polylines representing a fault or horizon (as interpreted on 2d seismic lines).
 */
public class PolylineSet extends Entity {

  /** Collection of related polylines. */
  private final Map<String, Polyline> _polylines = new HashMap<String, Polyline>();

  public PolylineSet(final String name, final GeologicFeature feature, final Domain primaryZDomain, final FloatMeasurement datumElevation) {
    this(name, new InMemoryMapper(PolylineSet.class), feature, primaryZDomain, datumElevation);
  }

  /**
   * parameterized constructor TODO
   * 
   * @param name of the entity.
   * @param mapper the entity mapper
   * @param feature associated with this PolylineSet.
   * @param primaryZDomain
   * @param datumElevation
   */
  public PolylineSet(final String name, final IMapper mapper, final GeologicFeature feature, final Domain primaryZDomain, final FloatMeasurement datumElevation) {
    super(name, mapper);//, feature, primaryZDomain, datumElevation, null);
    _mapper = mapper;
  }

  /**
   * Sequence of polylines that comprise the polyline set.
   * 
   * @return faultPolylines
   */
  public Polyline[] getPolylines() {
    Polyline[] faultPolylinesArray = new Polyline[_polylines.size()];
    _polylines.values().toArray(faultPolylinesArray);
    return faultPolylinesArray;
  }

  /**
   * The number of polylines contained in the set.
   * 
   * @return numPolylines
   */
  public int getNumPolylines() {
    return _polylines.size();
  }

  /**
   * Add an array of org.geocraft.core.model.Polyline, ignoring duplicates.
   * 
   * @param polylines Sequence of polylines that comprise the polyline set.
   */
  public void addPolylines(final Polyline[] polylines) {
    for (Polyline polyline : polylines) {
      addPolyline(polyline);
    }
  }

  /**
   * Add a org.geocraft.core.model.Polyline, ignoring duplicates.
   * 
   * @param polyline polyline to add to the the polyline set.
   */
  public void addPolyline(final Polyline polyline) {
    if (!_polylines.containsKey(polyline.getUniqueID())) {
      _polylines.put(polyline.getUniqueID(), polyline);
      setDirty(true);
    }
  }

  /**
   * Remove elements specified in the parameter.
   * 
   * @param polylines Sequence of polylines that comprise the polyline set.
   */
  public void removeFromPolylines(final Polyline[] polylines) {
    for (Polyline polyline : polylines) {
      _polylines.remove(polyline.getUniqueID());
    }
    setDirty(true);
  }

  /**
   * Removes all faultPolylines. Sequence of fault polylines that comprise the fault polyline set.
   */
  public void removeAllPolylines() {
    _polylines.clear();
    setDirty(true);
  }
}
