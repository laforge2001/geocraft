/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.internal.geomath.view;


import org.eclipse.core.runtime.IAdaptable;
import org.geocraft.algorithm.IStandaloneAlgorithmDescription;


/*
 * The content provider class is responsible for providing objects to the
 * view. It can wrap existing objects in adapters or simply return objects
 * as-is. These objects may be sensitive to the current input of the view, or
 * ignore it and always show the same content (like Task List, for example).
 */
public class TreeObject implements IAdaptable {

  private final String _name;

  private IStandaloneAlgorithmDescription _algorithm;

  private TreeParent _parent;

  public TreeObject(final String name) {
    _name = name;
  }

  public TreeObject(final String name, final IStandaloneAlgorithmDescription algorithm) {
    this(name);
    _algorithm = algorithm;
  }

  public String getName() {
    return _name;
  }

  public IStandaloneAlgorithmDescription getStandaloneAlgorithm() {
    return _algorithm;
  }

  public void setParent(final TreeParent parent) {
    _parent = parent;
  }

  public TreeParent getParent() {
    return _parent;
  }

  @Override
  public String toString() {
    return getName();
  }

  @SuppressWarnings("unused")
  public Object getAdapter(final Class key) {
    return null;
  }

  public boolean isLeaf() {
    return true;
  }
}
