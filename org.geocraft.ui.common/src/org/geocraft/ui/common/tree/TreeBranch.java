/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.ui.common.tree;


import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeListener;


public class TreeBranch extends TreeLeaf {

  private List<TreeLeaf> _children;

  private Class[] _loadOptions;

  public TreeBranch(String name, String description) {
    super(name, description);
    _children = new ArrayList<TreeLeaf>();
    _loadOptions = new Class[0];
  }

  public TreeBranch(String name, String description, ChangeListener listener) {
    super(name, description, listener);
    _children = new ArrayList<TreeLeaf>();
    _loadOptions = new Class[0];
  }

  public TreeBranch(String name, String description, ChangeListener listener, Class... loadOptions) {
    super(name, description, listener);
    _children = new ArrayList<TreeLeaf>();
    _loadOptions = new Class[loadOptions.length];
    System.arraycopy(loadOptions, 0, _loadOptions, 0, loadOptions.length);
  }

  public Class[] getLoadOptions() {
    return _loadOptions;
  }

  public void addChild(TreeLeaf child) {
    _children.add(child);
    child.setParent(this);
    changed();
  }

  public void removeChild(TreeLeaf child) {
    _children.remove(child);
    child.setParent(null);
    changed();
  }

  public TreeLeaf[] getChildren() {
    return _children.toArray(new TreeLeaf[_children.size()]);
  }

  public boolean hasChildren() {
    return _children.size() > 0;
  }
}
