/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.ui.volumeviewer.canvas;


import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Spatial;


public class PickRecord {

  public enum Type {
    Triangle, Bounding
  }

  private final Spatial _spatial;

  private final double _distance;

  private final Vector3 _location;

  private final Type _type;

  /**
   * 
   * @param spatial
   * @param distance
   * @param location
   */
  public PickRecord(final Spatial spatial, final double distance, final Vector3 location, final Type type) {
    _spatial = spatial;
    _distance = distance;
    _location = location;
    _type = type;
  }

  public Spatial getSpatial() {
    return _spatial;
  }

  public double getDistance() {
    return _distance;
  }

  public Vector3 getLocation() {
    return _location;
  }

  public Type getType() {
    return _type;
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof PickRecord && ((PickRecord) obj)._spatial == _spatial; // they point to the same exact object.
  }
}
