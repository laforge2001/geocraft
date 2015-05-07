/*
 * Copyright (C) ConocoPhillips 2009 All Rights Reserved.
 */
package org.geocraft.ui.property;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;


/**
 * Category node in the property tree
 * @author hansegj
 *
 */
public class TreeParent extends TreeObject {

  private final List<TreeObject> _children;

  public TreeParent(final String category, String property, Object value, Image propertyImage, Image valueImage) {
    super(category, property, value, propertyImage, valueImage);
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
    return _children.size() == 0;
  }
}
