/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.internal.geomath.view;


import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.part.ViewPart;
import org.geocraft.algorithm.IStandaloneAlgorithmDescription;
import org.geocraft.algorithm.StandaloneAlgorithmRegistry;


public class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {

  private TreeParent _invisibleRoot;

  private final ViewPart _view;

  public ViewContentProvider(final ViewPart view) {
    _view = view;
  }

  @SuppressWarnings("unused")
  public void inputChanged(final Viewer v, final Object oldInput, final Object newInput) {
    // does nothing for now
  }

  public void dispose() {
    // does nothing for now
  }

  public Object[] getElements(final Object parent) {
    if (parent.equals(_view.getViewSite())) {
      if (_invisibleRoot == null) {
        initialize();
      }
      return getChildren(_invisibleRoot);
    }
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

  /*
   * We will set up a dummy model to initialize tree hierarchy. 
   * In a real code, you will connect to a real model and expose its hierarchy.
   */
  private void initialize() {
    _invisibleRoot = new TreeParent("");

    IStandaloneAlgorithmDescription[] tools = StandaloneAlgorithmRegistry.getInstance().getAlgorithmDescriptions();
    for (IStandaloneAlgorithmDescription tool : tools) {
      if (!tool.isVisible()) {
        continue;
      }
      String[] paths = tool.getFullPath().split("/");
      int level = 1;
      for (String path : paths) {
        TreeObject treeNode = locateNode(paths, path, level);
        if (treeNode == null) {
          if (level == 1) {
            _invisibleRoot.addChild(new TreeParent(path));
          } else {
            TreeParent parentNode = (TreeParent) locateNode(paths, paths[level - 2], level - 1);
            if (level < paths.length) {
              parentNode.addChild(new TreeParent(path));
            } else {
              parentNode.addChild(new TreeObject(path, tool));
            }
          }
        }
        level++;
      }
    }
  }

  private TreeObject locateNode(final String[] paths, final String nodeName, final int level) {
    TreeObject[] childNodes = _invisibleRoot.getChildren();
    int temp = 1;
    while (temp <= level) {
      boolean found = false;
      for (TreeObject childNode : childNodes) {
        if (temp == level && childNode.getName().equals(nodeName)) {
          return childNode;
        }
        if (childNode instanceof TreeParent && childNode.getName().equals(paths[temp - 1])) {
          TreeParent parentNode = (TreeParent) childNode;
          childNodes = parentNode.getChildren();
          found = true;
          break;
        }
      }
      if (!found) {
        return null;
      }
      temp++;
    }

    //    TreeObject node = null;
    //    List<TreeObject> prevLevelObjects = new ArrayList<TreeObject>();
    //    List<TreeObject> levelObjects = new ArrayList<TreeObject>();
    //    prevLevelObjects.add(_invisibleRoot);
    //    // build the existing levels
    //    for (int i = 0; i < level; i++) {
    //      for (TreeObject object : prevLevelObjects) {
    //        if (object instanceof TreeParent) {
    //          levelObjects.addAll(Arrays.asList(((TreeParent) object).getChildren()));
    //        }
    //      }
    //      if (i < level - 1) {
    //        prevLevelObjects.clear();
    //        prevLevelObjects.addAll(levelObjects);
    //        levelObjects.clear();
    //      }
    //    }
    //
    //    // locate the node
    //    for (int i = 0; i < levelObjects.size() && node == null; i++) {
    //      TreeObject currentNode = levelObjects.get(i);
    //      if (currentNode.getName().equals(nodeName)) {
    //        node = currentNode;
    //      }
    //    }
    return null;
  }

}
