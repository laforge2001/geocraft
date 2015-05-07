/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.polygon;


/**
 * Defines the event that is broadcast upon the update of the polygon regions model in the AB crossplot.
 */
public class PolygonRegionsModelEvent {

  /** Enumeration for the polygon regions model event type. */
  public static enum Type {
    PolygonsUpdated, RegionSymmetryUpdated, PolygonCreated, PolygonDeleted
  }

  /** The event type. */
  private final Type _type;

  /** The polygon regions model. */
  private final PolygonRegionsModel _model;

  /** The indices of the polygons updated. */
  private final int[] _polygonIndices;

  /**
   * The default constructor.
   * @param polygonModel the polygon regions model.
   */
  public PolygonRegionsModelEvent(final Type type, final PolygonRegionsModel polygonModel, final int[] polygonIndices) {
    _type = type;
    _model = polygonModel;
    _polygonIndices = new int[polygonIndices.length];
    System.arraycopy(polygonIndices, 0, _polygonIndices, 0, polygonIndices.length);
  }

  /**
   * Gets the event type (e.g. PolygonsUpdated, RegionSymmetryUpdated, etc).
   * @return the event type.
   */
  public Type getType() {
    return _type;
  }

  /**
   * Gets the polygon regions model.
   * @return the polygon regions model.
   */
  public PolygonRegionsModel getPolygonRegionsModel() {
    return _model;
  }

  /**
   * Gets the indices of the updated polygons.
   * @return the indices of the updated polygons.
   */
  public int[] getPolygonIndices() {
    int[] polygonIndices = new int[_polygonIndices.length];
    System.arraycopy(_polygonIndices, 0, polygonIndices, 0, _polygonIndices.length);
    return polygonIndices;
  }
}
