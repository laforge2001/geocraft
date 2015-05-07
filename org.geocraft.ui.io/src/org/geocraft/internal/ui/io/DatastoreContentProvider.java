/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.ui.io;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Tree;
import org.geocraft.core.io.IDatastoreAccessor;
import org.geocraft.core.io.IDatastoreAccessorService;
import org.geocraft.core.model.mapper.IOMode;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.common.tree.TreeBranch;
import org.geocraft.ui.common.tree.TreeLeaf;
import org.geocraft.ui.common.tree.TreeRoot;


public class DatastoreContentProvider implements IStructuredContentProvider, ITreeContentProvider {

  private final Tree _tree;

  private final Set<String> _entityClassNames;

  private final IOMode _ioMode;

  public DatastoreContentProvider(final Tree tree, final Set<String> entityClassNames, final IOMode ioMode) {
    _tree = tree;
    _entityClassNames = entityClassNames;
    _ioMode = ioMode;
  }

  public Object[] getElements(final Object inputElement) {
    List<String> folderKeys = new ArrayList<String>();
    Map<String, TreeBranch> folders = new HashMap<String, TreeBranch>();
    IDatastoreAccessor[] datastoreAccessors = getDatastoreAccessors();
    for (IDatastoreAccessor datastoreAccessor : datastoreAccessors) {
      String category = datastoreAccessor.getCategory();
      TreeLeaf child = new TreeLeaf(datastoreAccessor.getName(), "");
      if (!folderKeys.contains(category)) {
        folderKeys.add(category);
        TreeBranch folder = new TreeBranch(category, "");
        folders.put(category, folder);
        folder.addChild(child);
      } else {
        TreeBranch folder = folders.get(category);
        folder.addChild(child);
      }
    }
    String[] keys = folderKeys.toArray(new String[0]);
    Arrays.sort(keys);
    TreeBranch[] results = new TreeBranch[keys.length];
    for (int i = 0; i < keys.length; i++) {
      results[i] = folders.get(keys[i]);
    }
    return results;
  }

  public void dispose() {
    // No action.
  }

  @SuppressWarnings("unused")
  public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
    // No action.
  }

  public Object[] getChildren(final Object parentElement) {
    if (parentElement instanceof TreeRoot) {
      return ((TreeRoot) parentElement).getChildren();
    } else if (parentElement instanceof TreeBranch) {
      return ((TreeBranch) parentElement).getChildren();
    }
    return new Object[0];
  }

  public Object getParent(final Object element) {
    TreeLeaf leaf = (TreeLeaf) element;
    return leaf.getParent();
  }

  public boolean hasChildren(final Object element) {
    if (element.getClass().equals(TreeLeaf.class)) {
      return false;
    }
    return true;
  }

  private IDatastoreAccessor[] getDatastoreAccessors() {
    IDatastoreAccessorService datastoreAccessorService = ServiceProvider.getDatastoreAccessorService();
    if (datastoreAccessorService == null) {
      ServiceProvider.getLoggingService().getLogger(getClass()).warn("No datastore accessor service found.");
      return new IDatastoreAccessor[0];
    }
    List<IDatastoreAccessor> list = new ArrayList<IDatastoreAccessor>();
    for (IDatastoreAccessor datastoreAccessor : datastoreAccessorService.getDatastoreAccessors()) {
      String[] supportedEntityClasses = datastoreAccessor.getSupportedEntityClassNames();
      for (String supportedEntityClass : supportedEntityClasses) {
        boolean canDoAction = true;
        if (_ioMode.equals(IOMode.INPUT)) {
          canDoAction = datastoreAccessor.canInput();
        } else if (_ioMode.equals(IOMode.OUTPUT)) {
          canDoAction = datastoreAccessor.canOutput();
        }
        if (canDoAction && (_entityClassNames.size() == 0 || _entityClassNames.contains(supportedEntityClass))) {
          list.add(datastoreAccessor);
          break;
        }
      }
    }
    return list.toArray(new IDatastoreAccessor[0]);
  }
}
