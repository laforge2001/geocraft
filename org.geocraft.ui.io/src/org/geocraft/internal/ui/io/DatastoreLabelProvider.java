/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.ui.io;


import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.geocraft.core.io.IDatastoreAccessor;
import org.geocraft.core.io.IDatastoreAccessorService;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.common.tree.TreeBranch;
import org.geocraft.ui.common.tree.TreeLeaf;
import org.geocraft.ui.model.IModelSharedImages;
import org.geocraft.ui.model.ModelUI;


public class DatastoreLabelProvider extends LabelProvider {

  @Override
  public String getText(final Object object) {
    // Check if the object is an entity tree leaf object.
    if (object instanceof TreeLeaf) {
      return ((TreeLeaf) object).getName();
    }
    return object.toString();
  }

  @Override
  public Image getImage(final Object object) {
    if (object instanceof TreeBranch) {
      return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
    }
    TreeLeaf treeLeaf = (TreeLeaf) object;
    IDatastoreAccessor accessor = lookupDatastoreAccessor(treeLeaf.getName());
    if (accessor != null) {
      String imageKey = accessor.getSupportedEntityClassNames()[0];
      for (String entityClassName : accessor.getSupportedEntityClassNames()) {
        if (entityClassName.equals("PostStack3d") || entityClassName.equals("Grid3d")) {
          imageKey = entityClassName;
        }
      }
      IModelSharedImages modelImages = ModelUI.getSharedImages();
      return modelImages.getImage(imageKey);
    }
    return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
  }

  private IDatastoreAccessor lookupDatastoreAccessor(final String name) {
    IDatastoreAccessorService datastoreAccessorService = ServiceProvider.getDatastoreAccessorService();
    if (datastoreAccessorService == null) {
      ServiceProvider.getLoggingService().getLogger(getClass()).warn("No datastore accessor service found.");
      return null;
    }
    for (IDatastoreAccessor datastoreAccessor : datastoreAccessorService.getDatastoreAccessors()) {
      if (datastoreAccessor.getName().equalsIgnoreCase(name)) {
        return datastoreAccessor;
      }
    }
    return null;
  }
}
