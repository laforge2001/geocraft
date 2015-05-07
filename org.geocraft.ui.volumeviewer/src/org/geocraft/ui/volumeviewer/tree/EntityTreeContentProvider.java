/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer.tree;


import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.widgets.Display;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.seismic.SeismicDataset;
import org.geocraft.ui.repository.PropertiesProviderTreeObject;
import org.geocraft.ui.repository.RepositoryViewContentProvider;
import org.geocraft.ui.viewer.tree.EntityTree;


/**
 * A content provider for the entity tree.
 */
public class EntityTreeContentProvider extends RepositoryViewContentProvider {

  /** A mapping between an entity and its overlay entities set. */
  private final Map<Entity, Set<Entity>> _entityOverlays = new LinkedHashMap<Entity, Set<Entity>>();

  /** A mapping between an entity and its tree object. */
  private final Map<Entity, PropertiesProviderTreeObject> _entityTreeObject = new LinkedHashMap<Entity, PropertiesProviderTreeObject>();

  /** The entity tree. */
  private EntityTree _tree;

  public EntityTreeContentProvider() {
    super(false);
  }

  public void setTree(final EntityTree tree) {
    _tree = tree;
  }

  @Override
  protected void addSeismic3dOverlays(final PropertiesProviderTreeObject volumeObject, final SeismicDataset volume) {
    Set<Entity> overlays = _entityOverlays.get(volume);
    if (overlays != null) {
      for (Entity entity : overlays) {
        PropertiesProviderTreeObject treeObject = new PropertiesProviderTreeObject(entity);
        treeObject.setParentProvider(volume);
        _entityTreeObject.put(entity, treeObject);
        volumeObject.addChild(treeObject);
      }
    }
  }

  /**
   * Add an entity as an overlay to another entity.
   * Overlays are added as child nodes of the other entity. 
   * @param entity the entity
   * @param overlayEntity the overlay entity
   */
  public void addEntityOverlay(final Entity entity, final Entity overlayEntity) {
    if (_entityOverlays.get(entity) == null) {
      _entityOverlays.put(entity, new HashSet<Entity>());
    }
    if (overlayEntity != null) {
      _entityOverlays.get(entity).add(overlayEntity);
    }
  }

  /**
   * Remove an overlay from an entity.
   * @param entity the entity
   * @param overlayEntity the overlay entity
   */
  public void removeEntityOverlay(final Entity entity, final Entity overlayEntity) {
    if (_entityOverlays.get(entity) == null) {
      return;
    }
    _entityOverlays.get(entity).remove(overlayEntity);
  }

  /**
   * Update the check state for a tree node of an entity.
   * @param entity the entity
   * @param checked the new checked state
   */
  public void updateCheckedState(final Entity entity, final boolean checked) {
    Display.getDefault().asyncExec(new Runnable() {

      public void run() {
        _tree.expandAll();
        _tree.setChecked(_entityTreeObject.get(entity), checked);
      }
    });
  }

  @Override
  public void dispose() {
    _entityOverlays.clear();
    _entityTreeObject.clear();
    super.dispose();
  }

}
