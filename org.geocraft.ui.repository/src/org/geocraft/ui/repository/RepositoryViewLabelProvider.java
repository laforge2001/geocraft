/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.ui.repository;


import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.base.IPropertiesProvider;
import org.geocraft.core.model.base.ValueObject;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.common.tree.TreeBranch;
import org.geocraft.ui.model.IModelSharedImages;
import org.geocraft.ui.model.ModelUI;


/**
 * Defines the label provider for the object tree in the repository view.
 */
public class RepositoryViewLabelProvider extends LabelProvider {

  /** The shared model images. */
  private final IModelSharedImages _modelImages;

  /**
   * The default constructor. Obtains a references to the shared model images.
   */
  public RepositoryViewLabelProvider() {
    _modelImages = ModelUI.getSharedImages();
  }

  @Override
  public String getText(final Object obj) {
    // Check if the object is an entity tree object.
    if (obj instanceof PropertiesProviderTreeObject
        && ((PropertiesProviderTreeObject) obj).getPropertiesProvider() instanceof Entity) {
      // Get the entity from the tree object and lookup its key.
      Entity entity = (Entity) ((PropertiesProviderTreeObject) obj).getPropertiesProvider();
      IRepository repository = ServiceProvider.getRepository();
      StringBuffer label = new StringBuffer();
      //if (ServiceComponent.getSortByVars()) {
      // Return the text as a combination of the key then the entity name.
      String key = repository.lookupVariableName(entity);
      label.append(key + "=");
      label.append(entity.getDisplayName());
      //} else {
      //  // Return the text as a combination of entity name then the key.
      //  label.append(entity.getDisplayName());
      //  String key = repository.lookupVariableName(entity);
      //  label.append("=" + key);
      //}
      return label.toString();
    } else if (obj instanceof PropertiesProviderTreeObject
        && ((PropertiesProviderTreeObject) obj).getPropertiesProvider() instanceof ValueObject) {
      ValueObject valueObject = (ValueObject) ((PropertiesProviderTreeObject) obj).getPropertiesProvider();
      return valueObject.toString();
    }
    return obj.toString();
  }

  @Override
  public Image getImage(final Object obj) {
    String imageKey = ISharedImages.IMG_OBJS_WARN_TSK;
    // Check if the object is an entity tree object.
    if (obj instanceof PropertiesProviderTreeObject) {
      // Get the entity from the tree object and lookup its image.
      IPropertiesProvider propProvider = ((PropertiesProviderTreeObject) obj).getPropertiesProvider();
      return _modelImages.getImage(propProvider);
    } else if (obj instanceof TreeBranch) {
      imageKey = ISharedImages.IMG_OBJ_FOLDER;
    }
    return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
  }

}
