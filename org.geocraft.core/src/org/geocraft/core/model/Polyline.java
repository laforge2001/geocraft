/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */
package org.geocraft.core.model;


import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.InMemoryMapper;
import org.geocraft.core.model.seismic.SeismicDataset;
import org.geocraft.core.model.seismic.SeismicLine2d;
import org.geocraft.core.model.seismic.SeismicSurvey3d;


/**
 * Represents a single polyline (e.g. a fault segment or a seismic horizon picked on a single 2d line).
 */
public class Polyline extends Entity {

  /**
   * The parent of the polyline.
   */
  private final PolylineSet _polylineSet;

  // TODO just store a reference to the abstract SeismicDataset
  /**
   * Optionally identifies the SeismicSurvey3d (3d survey) that this polyline was picked on
   */
  private SeismicSurvey3d _seismicSurvey3d;

  /** The 2D seismic line that this polyline was picked on (optional). */
  private SeismicLine2d _seismicLine2d;

  /**
   * Optionally identifies the SeismicDataset (2d or 3d) that this polyline was picked on
   */
  private SeismicDataset _seismicDataset;

  /**
   * The points defining the polyline
   */
  private CoordinateSeries _polyline;

  public Polyline(final String name, final PolylineSet polylineSet, final CoordinateSeries polyline) {
    this(name, new InMemoryMapper(Polyline.class), polylineSet, polyline);
  }

  /**
   * parameterized constructor
   * 
   * @param name Name of the entity.
   * @param polylineSet The parent of the polyline.
   * @param polyline The points defining the polyline
   */
  public Polyline(final String name, final IMapper mapper, final PolylineSet polylineSet, final CoordinateSeries polyline) {
    super(name, mapper);
    _polylineSet = polylineSet;
    _polyline = polyline;
    _mapper = mapper;
    // TODO: need to add self to PolylineSet
  }

  /**
   * The parent of the fault polyline.
   * 
   * @return faultPolylineSet
   */
  public PolylineSet getPolylineSet() {
    return _polylineSet;
  }

  /**
   * The SeismicSurvey3d (3d survey) that this polyline was picked on. If it is not associated with a 3d survey null is returned
   * 
   * @return seismicGeometry3d
   */
  // TODO: missing method to set the SeismicSurvey3d
  public SeismicSurvey3d getSeismicSurvey3d() {
    return _seismicSurvey3d;
  }

  /**
   * Returns the 2D seismic line that this polyline was picked on.
   * <p>
   * If it is not associated with a 2d seismic line, then <i>null</i> is returned.
   * 
   * @return the 2D seismic line this polyline was picked on; otherwise <i>null</i>.
   */
  public SeismicLine2d getSeismicLine2D() {
    return _seismicLine2d;
  }

  /**
   * The SeismicDataset (2d or 3d) that this polyline was picked on. If it is not associated with a dataset null is returned
   * 
   * @return seismicDataset
   */
  // TODO: missing method to set the SeismicDataset
  public SeismicDataset getSeismicDataset() {
    return _seismicDataset;
  }

  /**
   * Get the points defining the polyline
   * 
   * @return polyline
   */
  public CoordinateSeries getPolyline() {
    return _polyline;
  }

  /**
   * Get the points defining the polyline
   * 
   * @param polyline
   */
  public void setPolyline(final CoordinateSeries polyline) {
    _polyline = polyline;
    setDirty(true);
  }
}
