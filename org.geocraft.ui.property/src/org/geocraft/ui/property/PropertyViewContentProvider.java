/*
 * Copyright (C) ConocoPhillips 2009 All Rights Reserved.
 */
package org.geocraft.ui.property;


import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;


public class PropertyViewContentProvider implements ITreeContentProvider {

  //private final ViewPart _viewPart;

  public PropertyViewContentProvider() {
    //_viewPart = view;
  }

  @SuppressWarnings("unused")
  public void inputChanged(final Viewer v, final Object oldInput, final Object newInput) {
    // Nothing to do.
  }

  public void dispose() {
    // Nothing to do.
  }

  public Object[] getElements(final Object parent) {
    return getChildren(parent);
  }

  public Object getParent(final Object child) {
    if (child instanceof TreeObject) {
      return ((TreeObject) child).getParent();
    }
    return null;
  }

  public Object[] getChildren(final Object parent) {
    if (parent instanceof TreeParent) {
      return ((TreeParent) parent).getChildren();
    }
    return new Object[0];
  }

  public boolean hasChildren(final Object parent) {
    if (parent instanceof TreeParent) {
      return ((TreeParent) parent).hasChildren();
    }
    return false;
  }
}
