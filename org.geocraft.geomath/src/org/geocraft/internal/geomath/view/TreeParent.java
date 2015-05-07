/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.internal.geomath.view;


import java.util.ArrayList;
import java.util.List;


public class TreeParent extends TreeObject {

  private final List<TreeObject> _children;

  public TreeParent(final String name) {
    super(name);
    _children = new ArrayList<TreeObject>();
  }

  public void addChild(final TreeObject child) {
    _children.add(child);
    child.setParent(this);
  }

  public void removeChild(final TreeObject child) {
    _children.remove(child);
    child.setParent(null);
  }

  public TreeObject[] getChildren() {
    return _children.toArray(new TreeObject[_children.size()]);
  }

  public boolean hasChildren() {
    return _children.size() > 0;
  }

  @Override
  public boolean isLeaf() {
    return false;
  }
}
