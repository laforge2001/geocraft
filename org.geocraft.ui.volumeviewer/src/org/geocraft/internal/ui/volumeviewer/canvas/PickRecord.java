/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.internal.ui.volumeviewer.canvas;


import org.geocraft.core.rendering.scene.SceneNode;
import org.joml.Vector3f;


public class PickRecord {

  public enum Type {
    Triangle, Bounding
  }

  private final SceneNode _spatial;

  private final double _distance;

  private final Vector3f _location;

  private final Type _type;

  /**
   *
   * @param spatial
   * @param distance
   * @param location
   */
  public PickRecord(final SceneNode spatial, final double distance, final Vector3f location, final Type type) {
    _spatial = spatial;
    _distance = distance;
    _location = location;
    _type = type;
  }

  public SceneNode getSpatial() {
    return _spatial;
  }

  public double getDistance() {
    return _distance;
  }

  public Vector3f getLocation() {
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
