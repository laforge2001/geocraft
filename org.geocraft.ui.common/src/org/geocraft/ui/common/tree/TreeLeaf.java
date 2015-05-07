/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.ui.common.tree;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertySource;


public class TreeLeaf implements IAdaptable {

  private final String _name;

  private final String _description;

  private TreeBranch _parent;

  private Object _data;

  private final Set<ChangeListener> _listeners;

  public TreeLeaf(final String name, final String description) {
    _name = name;
    _description = description;
    _listeners = Collections.synchronizedSet(new HashSet<ChangeListener>());
  }

  public TreeLeaf(final String name, final String description, final ChangeListener listener) {
    this(name, description);
    addChangeListener(listener);
  }

  public String getName() {
    return _name;
  }

  public String getDescription() {
    return _description;
  }

  public void setParent(final TreeBranch parent) {
    _parent = parent;
    changed();
  }

  public TreeBranch getParent() {
    return _parent;
  }

  @Override
  public String toString() {
    return getName();
  }

  public void addChangeListener(final ChangeListener listener) {
    _listeners.add(listener);
  }

  public void removeChangeListener(final ChangeListener listener) {
    _listeners.remove(listener);
  }

  protected void changed() {
    ChangeEvent event = new ChangeEvent(this);
    ChangeListener[] listeners = _listeners.toArray(new ChangeListener[0]);
    for (ChangeListener listener : listeners) {
      listener.stateChanged(event);
    }
  }

  public Object getAdapter(final Class adapter) {
    if (adapter.equals(IPropertySource.class)) {
      return new TreePropertySource(_name, _description);
    }
    return null;
  }

  public Object getData() {
    return _data;
  }

  public void setData(Object data) {
    _data = data;
  }
}
