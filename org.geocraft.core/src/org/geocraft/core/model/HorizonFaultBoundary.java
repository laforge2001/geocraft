/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.core.model;


import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.geologicfeature.GeologicFault;
import org.geocraft.core.model.mapper.IMapper;


/**
 * A set of connected points representing the intersection between a
 * horizon representation and a geologic fault.
 * <p>
 * If the fault dies out within the extent of the horizon representation
 * then this will form a closed polygon.
 * <p>
 * It is possible an intersection between a fault and horizon representation
 * may be represented by multiple horizon fault boundary polygons.
 */

public class HorizonFaultBoundary extends Entity {

  /** Identifies the associated geologic fault. */
  private GeologicFault _geologicFault;

  /** The associated horizon fault boundary set. */
  private HorizonFaultBoundarySet _horizonFaultBoundarySet;

  /** The number of points contained in the boundary. */
  private int _numPoints;

  /**
   * The set of ordered points that when connected form the horizon/fault boundary
   * (fault polygon). The X and Y fields will always be populated; however, the
   * Z field is not guaranteed to be populated.
   */
  private CoordinateSeries _points;

  /**
   * Indicates whether the the Point's Z field can be used.
   * If true the points have a three-dimensional spatial representation (e.g. the
   * X, Y, and Z fields are populated). The points can be used in a 3d viewer
   * type application.
   * If false the points has a two-dimensional spatial representation (e.g. only
   * the X and Y fields are populated). The points can be used in a 2d viewer
   * type application (e.g. basemap).
   */
  private boolean _hasZ;

  /**
   * Create a HorizonFaultBoundary and add it to its parent HorizonFaultBoundarySet.
   *
   * @param name Name of the entity.
   * @param mapper the entity mapper
   * @param geologicFault Identifies the associated geologic fault.
   * @param horizonFaultBoundarySet The parent horizon fault boundary set that this HorizonFaultBoundary belongs to.
   * @param points that are connected to form the horizon/fault boundary (fault polygon).
   * @param hasZ indicates whether the the Point's Z field can be used.
   */
  public HorizonFaultBoundary(String name, IMapper mapper, GeologicFault geologicFault, HorizonFaultBoundarySet horizonFaultBoundarySet, CoordinateSeries points, boolean hasZ) {

    super(name, mapper);
    _geologicFault = geologicFault;
    _horizonFaultBoundarySet = horizonFaultBoundarySet;
    _points = points;

    HorizonFaultBoundary[] boundaries = { this };

    horizonFaultBoundarySet.addToHorizonFaultBoundaries(boundaries);
    _hasZ = hasZ;
  }

  /**
   * Identifies the associated geologic fault.
   *
   * @return geologicFault
   */
  public GeologicFault getGeologicFault() {
    return _geologicFault;
  }

  /**
   * The associated horizon fault boundary set.
   *
   * @return horizonFaultBoundarySet
   */
  public HorizonFaultBoundarySet getHorizonFaultBoundarySet() {
    return _horizonFaultBoundarySet;
  }

  /**
   * The number of points contained in the boundary.
   *
   * @return numPoints
   */
  public int getNumPoints() {
    return _numPoints;
  }

  /**
   * The set of ordered points that when connected form the horizon/fault boundary
   * (fault polygon). The X and Y fields will always be populated; however, the
   * Z field is not guaranteed to be populated. Refer to the hasZ
   * attribute description.
   *
   * @return points
   */
  public CoordinateSeries getPoints() {
    return _points;
  }

  /**
   * Indicates whether the Point's Z field can be used.
   * If true the points have a three-dimensional spatial representation (e.g. the
   * X, Y, and Z fields are populated). The points can be used in a 3d viewer
   * type application.
   * <p>
   * If false the points has a two-dimensional spatial representation (e.g. only
   * the X and Y fields are populated). The points can be used in a 2d viewer
   * type application (e.g. basemap).
   *
   * @return true if the data has z values.
   */
  public boolean hasZ() {
    return _hasZ;
  }

  /**
   * Identifies the associated geologic fault.
   *
   * @param geologicFault
   */
  public void setGeologicFault(GeologicFault geologicFault) {
    _geologicFault = geologicFault;
    setDirty(true);
  }

  /**
   * The set of ordered points.
   *
   * TODO 360 - should this method take a hasZ argument?
   *
   * @param points
   */
  public void setPoints(CoordinateSeries points) {
    _points = points;
    setDirty(true);
  }
}
