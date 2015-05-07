/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.tree;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.geocraft.core.model.base.AbstractPropertiesProvider;
import org.geocraft.core.model.base.IPropertiesProvider;
import org.geocraft.ui.common.tree.TreeBranch;
import org.geocraft.ui.common.tree.TreeLeaf;
import org.geocraft.ui.common.tree.TreeRoot;
import org.geocraft.ui.repository.PropertiesProviderTreeObject;
import org.geocraft.ui.repository.RepositoryViewContentProvider;
import org.geocraft.ui.repository.RepositoryViewLabelProvider;
import org.geocraft.ui.repository.RepositoryViewSorter;


/**
 * A checkbox tree viewer for entities and value objects.
 */
public class EntityTree extends CheckboxTreeViewer {

  /** The tree viewer content provider. */
  private final RepositoryViewContentProvider _contentProvider;

  /** The tree viewer root. */
  private final TreeRoot _root;

  private RepositoryViewSorter _sorter;

  /** A list of the entities that are unchecked. */
  private final List<AbstractPropertiesProvider> _uncheckedEntities = new ArrayList<AbstractPropertiesProvider>();

  /**
   * The constructor.
   * @param parent the parent composite
   * @param contentProvider the tree viewer content provider
   * @param style the SWT style
   */
  public EntityTree(final Composite parent, final RepositoryViewContentProvider contentProvider, final int style) {
    super(parent, style);
    _contentProvider = contentProvider;
    setContentProvider(_contentProvider);
    setLabelProvider(new DecoratingLabelProvider(new RepositoryViewLabelProvider(), PlatformUI.getWorkbench()
        .getDecoratorManager().getLabelDecorator()));
    _sorter = new RepositoryViewSorter();
    setSorter(_sorter);
    _root = new TreeRoot();
    setInput(_root);
  }

  public void addEntities(final Map<Object, Object> entities) {
    _contentProvider.refreshContent(entities.keySet());
    expandAll();

    for (TreeLeaf node : _root.getChildren()) {
      setEnabledChildren((TreeBranch) node, entities);
    }
  }

  private void setEnabledChildren(final TreeBranch node, final Map<Object, Object> entities) {
    if (node instanceof PropertiesProviderTreeObject) {
      PropertiesProviderTreeObject treeObject = (PropertiesProviderTreeObject) node;
      boolean exists = entities.keySet().contains(treeObject.getPropertiesProvider());
      if (exists) {
        setChecked(treeObject, !_uncheckedEntities.contains(treeObject.getPropertiesProvider()));
      }
      if (!exists || entities.get(treeObject.getPropertiesProvider()) == null) {
        setGrayed(node, true);
        setChecked(node, true);
      }
    } else {
      setGrayed(node, true);
      setChecked(node, true);
    }
    TreeLeaf[] children = node.getChildren();
    for (TreeLeaf childNode : children) {
      if (childNode instanceof TreeBranch) {
        setEnabledChildren((TreeBranch) childNode, entities);
      }
    }
  }

  public void addUncheckedPropertiesProvider(final AbstractPropertiesProvider prop) {
    _uncheckedEntities.add(prop);
  }

  public void removeUncheckedPropertiesProvider(final IPropertiesProvider prop) {
    _uncheckedEntities.remove(prop);
  }

  public void dispose() {
    getTree().dispose();
    _contentProvider.dispose();
  }
}
