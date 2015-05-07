/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.fault;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geocraft.core.model.GeologicInterpretation;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.PolylinePick;
import org.geocraft.core.model.mapper.IMapper;


public class FaultInterpretation extends GeologicInterpretation {

  /** The type of fault (e.g. Normal, Thrust, etc). */
  private FaultType _faultType;

  /** The triangulated surface representation of the interpretation (optional). */
  private TriangulatedSurface _triangulatedSurface;

  /** The collection of fault pick segments, mapped by a pick key. */
  private final Map<String, PolylinePick> _pickSegments;

  public FaultInterpretation(final String displayName, final IMapper mapper) {
    super(displayName, mapper);
    _pickSegments = new HashMap<String, PolylinePick>();
  }

  /**
   * Returns the type of fault (e.g. Normal, Thrust, etc).
   */
  public FaultType getFaultType() {
    load();
    return _faultType;
  }

  /**
   * Sets the type of fault (e.g. Normal, Thrust, etc).
   * 
   * @param faultType the type of fault to set.
   */
  public void setFaultType(final FaultType faultType) {
    _faultType = faultType;
    setDirty(true);
  }

  /**
   * Returns the number of interpreted pick segments.
   */
  public int getNumPickSegments() {
    load();
    return _pickSegments.size();
  }

  /**
   * Adds a fault pick segment to the interpretation.
   * 
   * @param key the map key for the pick segment to add.
   * @param pick the pick segment to add.
   */
  public void addPickSegment(final String key, final PolylinePick pick) {
    _pickSegments.put(key, pick);
    setDirty(true);
  }

  /**
   * Removes a fault pick segment from the interpretation.
   * 
   * @param key the map key for the pick segment to remove.
   */
  public void removePickSegment(final String key) {
    _pickSegments.remove(key);
    setDirty(true);
  }

  /**
   * Returns an array of the fault segment picks.
   */
  public PolylinePick[] getSegments() {
    load();
    return _pickSegments.values().toArray(new PolylinePick[0]);
  }

  /**
   * Returns a flag indicating if the interpretation contains a triangulated surface representation.
   * 
   * @return <i>true</i> if a triangulated surface exists; <i>false</i> if not.
   */
  public boolean isTriangulated() {
    load();
    return _triangulatedSurface != null;
  }

  /**
   * Returns the triangulated surface representation.
   */
  public TriangulatedSurface getTriangulatedSurface() {
    load();
    return _triangulatedSurface;
  }

  /**
   * Sets the triangulated surface representation.
   * 
   * @param vertices the collection of triangle vertices.
   * @param triangles the collection of triangle connections.
   */
  public void setTriangulatedSurface(final List<Point3d> vertices, final List<TriangleDefinition> triangles) {
    _triangulatedSurface = new TriangulatedSurface(getDisplayName(), vertices, triangles);
    setDirty(true);
  }

  /**
   * Returns the number of vertices in the triangulated surface representation.
   */
  public int getNumVertices() {
    load();
    if (_triangulatedSurface == null) {
      return 0;
    }
    return _triangulatedSurface.getNumVertices();
  }

  /**
   * Returns the number of triangles in the triangulated surface representation.
   */
  public int getNumTriangles() {
    load();
    if (_triangulatedSurface == null) {
      return 0;
    }
    return _triangulatedSurface.getNumTriangles();
  }

}
