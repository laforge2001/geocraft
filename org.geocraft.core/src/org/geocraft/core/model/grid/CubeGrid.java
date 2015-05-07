/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.grid;


import java.util.ArrayList;
import java.util.List;

import org.geocraft.core.model.GeologicInterpretation;
import org.geocraft.core.model.mapper.IMapper;


public class CubeGrid extends GeologicInterpretation {

  //  private final Set<AbstractCubeGridProperty> _properties = new HashSet<AbstractCubeGridProperty>();

  //  private String[] _eventNames;

  private CubeGridGeometry _geometry;

  private CubeGridAreaOfInterest _aoi;

  private final List<TimeEvent> _events = new ArrayList<TimeEvent>();

  /**
   * Constructs a <code>CubeGrid</code> entity.
   * 
   * @param name the grid name.
   * @param mapper the grid mapper to the underlying datastore
   */
  public CubeGrid(final String name, final IMapper mapper) {
    super(name, mapper);
  }

  /**
   * Constructs a <code>CubeGrid</code> entity.
   * 
   * @param name the grid name.
   * @param mapper the grid mapper to the underlying datastore
   */
  public CubeGrid(final String name, final IMapper mapper, final CubeGridGeometry geometry) {
    super(name, mapper);
    _geometry = geometry;
  }

  public void setGeometry(final CubeGridGeometry geometry) {
    _geometry = geometry;
  }

  public CubeGridGeometry getGeometry() {
    if (_geometry == null) {
      load();
    }
    return _geometry;
  }

  public void setAoi(final CubeGridAreaOfInterest aoi) {
    _aoi = aoi;
  }

  public CubeGridAreaOfInterest getAoi() {
    if (_aoi == null) {
      load();
    }
    return _aoi;
  }

  public void updateTimeEvent(final TimeEvent event) {
    _events.add(event);
  }

  /**
   * retrieves the time event
   * @param eventId the id of the event (not the index into the list)
   * @return Time event associated with this event id
   */
  public TimeEvent getTimeEvent(final int eventId) {
    for (TimeEvent e : _events) {
      if (e.getId() == eventId) {
        return e;
      }
    }
    return null;
  }

  public TimeEvent getTimeEvent(final String eventName) {
    for (TimeEvent e : _events) {
      if (e.getName().equals(eventName))
        return e;
    }
    return null;
  }
}
