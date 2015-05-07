/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.layer;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class LayeredModelEntry {

  private final IViewLayer _layer;

  private IViewLayer _parent;

  private final List<IViewLayer> _children;

  public LayeredModelEntry(final IViewLayer layer) {
    _layer = layer;
    _children = Collections.synchronizedList(new ArrayList<IViewLayer>());
  }

  public IViewLayer getLayer() {
    return _layer;
  }

  public IViewLayer getParent() {
    return _parent;
  }

  public void setParent(final IViewLayer parent) {
    _parent = parent;
  }

  public IViewLayer[] getChildren() {
    return _children.toArray(new IViewLayer[0]);
  }

  public void setChildren(final IViewLayer[] children) {
    _children.clear();
    for (IViewLayer child : children) {
      _children.add(child);
    }
  }

  public void addChild(final IViewLayer child) {
    if (!_children.contains(child)) {
      _children.add(child);
    }
  }

  public void removeChild(final IViewLayer child) {
    if (_children.contains(child)) {
      _children.remove(child);
    }
  }

  public void dispose() {
    _children.clear();
  }
}
