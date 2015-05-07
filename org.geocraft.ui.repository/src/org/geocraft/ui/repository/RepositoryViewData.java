/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.repository;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.base.IPropertiesProvider;


/**
 * The class to manage the selected data into the repository tree.
 */
public class RepositoryViewData {

  private RepositoryViewData() {
    // The private constructor.
  }

  /** The selected entities in the repository tree. */
  private static List<Entity> _selectedEntities = new CopyOnWriteArrayList<Entity>();

  /** The selected objects in the repository tree. */
  private static List<Object> _selectedTreeObjects = new CopyOnWriteArrayList<Object>();

  public static Entity[] getSelectedEntities() {
    return _selectedEntities.toArray(new Entity[0]);
  }

  public static void setSelectedEntities(final List<Entity> entities) {
    _selectedEntities.clear();
    _selectedEntities.addAll(entities);
  }

  public static Object[] getSelectedObjects() {
    return _selectedTreeObjects.toArray(new Object[0]);
  }

  public static void setSelectedObjects(final List<Object> objects) {
    _selectedTreeObjects.clear();
    _selectedTreeObjects.addAll(objects);
  }

  public static TreePath selectPropertiesProvider(final Tree tree, final TreeItem node,
      final IPropertiesProvider propProvider, TreePath selected) {
    if (selected != null) {
      return selected;
    }
    Object data = node.getData();
    if (data instanceof PropertiesProviderTreeObject
        && ((PropertiesProviderTreeObject) data).getPropertiesProvider().equals(propProvider)) {
      tree.deselectAll();
      List<Object> path = new ArrayList<Object>();
      TreeItem currentNode = node;
      while (currentNode != null) {
        path.add(0, currentNode.getData());
        currentNode = currentNode.getParentItem();
      }
      return new TreePath(path.toArray());
    }

    TreeItem[] children = node.getItems();
    for (int i = 0; i < children.length && selected == null;) {
      return selectPropertiesProvider(tree, children[i], propProvider, selected);
    }

    return selected;
  }

}
