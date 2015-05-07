/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer;


import org.eclipse.jface.action.Action;
import org.eclipse.swt.graphics.Image;
import org.geocraft.core.model.datatypes.SpatialExtent;
import org.geocraft.ui.viewer.ReadoutInfo;
import org.geocraft.ui.viewer.layer.AbstractViewLayer;


public class RendererViewLayer extends AbstractViewLayer {

  private VolumeViewRenderer _renderer;

  public RendererViewLayer(final String name, final VolumeViewRenderer renderer) {
    super(name, renderer.getUniqueID(), false, false, true);
    _renderer = renderer;
    for (final Action action : _renderer.getActions()) {
      addAction(action);
    }
  }

  public SpatialExtent getExtent() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void remove() {
    super.remove();
    if (_renderer != null) {
      _renderer.clear();
      _renderer = null;
    }
  }

  @Override
  public Image getImage() {
    return _renderer.getImage();
  }

  @Override
  public void setVisible(final boolean visible) {
    super.setVisible(visible);
    _renderer.setVisible(visible);
  }

  public void redraw() {
    _renderer.redraw();
  }

  public void refresh() {
    _renderer.redraw();
  }

  public VolumeViewRenderer getRenderer() {
    return _renderer;
  }

  public boolean showReadoutInfo() {
    if (_renderer != null) {
      return _renderer.showReadoutInfo();
    }
    return false;
  }

  public void showReadoutInfo(final boolean show) {
    if (_renderer != null) {
      _renderer.showReadoutInfo(show);
    }
  }

  public ReadoutInfo getReadoutInfo(final double x, final double y) {
    if (_renderer != null) {
      return _renderer.getReadoutInfo(x, y);
    }
    return new ReadoutInfo(getName());
  }

  @Override
  public Object getUserObject() {
    final Object[] objects = _renderer.getRenderedObjects();
    if (objects == null) {
      return null;
    } else if (objects.length == 1) {
      return objects[0];
    }
    return objects;
  }

}
