/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.internal.ui.volumeviewer.widget;


import org.geocraft.core.rendering.camera.Camera;
import org.geocraft.core.rendering.scene.GroupNode;
import org.geocraft.internal.ui.volumeviewer.canvas.ViewCanvasImplementor;


/**
 * Scene-graph widget that draws the XYZ focus axes.
 *
 * TODO: port from Ardor3D AxisRods. Currently stubbed as an empty GroupNode
 * so callers compile. When re-implemented, build three child LineGeometry
 * objects in-place.
 */
public class FocusRods extends GroupNode {

  public enum ShowMode {
    ALWAYS("alwaysOn", "Always on"),
    ON_INTERACT("onInteract", "On mouse interaction"),
    NEVER("alwaysOff", "Always off");

    private final String _id;
    private final String _name;

    private ShowMode(final String id, final String name) {
      _id = id;
      _name = name;
    }

    public String getId() { return _id; }
    public String getName() { return _name; }

    public static ShowMode getModeForId(final String id) {
      for (ShowMode mode : values()) {
        if (mode.getId().equals(id)) {
          return mode;
        }
      }
      return ON_INTERACT;
    }
  }

  private double _scaleFactor = 0.05;
  private float _lineWidth = 2.5f;
  private ShowMode _mode = ShowMode.ON_INTERACT;
  private final ViewCanvasImplementor _impl;
  private final boolean _cursor;
  private boolean _interacting;

  public FocusRods(final ViewCanvasImplementor impl, final boolean cursor) {
    super("focus");
    _impl = impl;
    _cursor = cursor;
  }

  public void update(final Camera camera, final double tpf, final boolean inFocus) {
    // TODO: port from Ardor3D FocusRods.update
  }

  public double getScaleFactor() { return _scaleFactor; }
  public void setScaleFactor(final double factor) { _scaleFactor = factor; }

  public ShowMode getShowMode() { return _mode; }
  public void setShowMode(final ShowMode mode) { _mode = mode; }

  public boolean isInteracting() { return _interacting; }
  public void setInteracting(final boolean interacting) { _interacting = interacting; }

  public double getLineWidth() { return _lineWidth; }
  public void setAxisLineWidth(final float lineWidth) { _lineWidth = lineWidth; }

  public void setAxisLinesVisible(final boolean visible) {
    // TODO: port from Ardor3D FocusRods.setAxisLinesVisible
  }
}
