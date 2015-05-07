/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.internal.ui.model;


import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.geocraft.core.model.base.IPropertiesProvider;
import org.geocraft.ui.model.IModelSharedImages;
import org.geocraft.ui.model.ModelUI;


/**
 * Lazily loads the shared images this bundle provides.
 * 
 * We still use the convention that icons for entities are stored
 * in png files with the exact same name. 
 * 
 * So foo.class corresponds to the foo.png image.
 */
public class ModelSharedImages implements IModelSharedImages {

  /** The registry used to store the images. */
  private final ImageRegistry _imageRegistry;

  public ModelSharedImages() {
    _imageRegistry = new ImageRegistry();
  }

  /**
   * Convenience method that looks up the icon for an entity or value object type. 
   */
  public Image getImage(final IPropertiesProvider propProvider) {
    return getImage(propProvider.getClass().getSimpleName());
  }

  /**
   * Access an image directly from it's symbolic name. 
   * 
   * For entities this is done according to the 
   * entity/image naming convention. 
   */
  public Image getImage(final String symbolicName) {
    Image image = _imageRegistry.get(symbolicName);
    if (image == null) {
      loadImageDescriptor(symbolicName);
      image = _imageRegistry.get(symbolicName);
    }
    return image;
  }

  /**
   * Access an image descriptor directly from it's symbolic name. 
   * 
   * For entities this is done according to the entity/image naming convention. 
   */
  public ImageDescriptor getImageDescriptor(final String symbolicName) {
    ImageDescriptor imageDescriptor = _imageRegistry.getDescriptor(symbolicName);
    if (imageDescriptor == null) {
      imageDescriptor = loadImageDescriptor(symbolicName);
    }
    return imageDescriptor;
  }

  /**
   * Load an image that follows our entity/image naming convention. 
   * 
   * If it can't locate a png file a warning triangle image will 
   * be used instead. 
   * 
   * @param name 
   * @return image found at icons/[name].png or a warning triangle image 
   *               if [name].png does not exist.  
   */
  private ImageDescriptor loadImageDescriptor(final String name) {
    ImageDescriptor imageDescriptor = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
        ISharedImages.IMG_OBJS_WARN_TSK);
    String path = "icons/" + name + ".png";
    ImageDescriptor image = _imageRegistry.getDescriptor(path);
    if (image == null) {
      URL url = FileLocator.find(Platform.getBundle(ModelUI.PLUGIN_ID), new Path(path), null);
      if (url == null) {
        String emptyPath = "icons/Empty.png";
        url = FileLocator.find(Platform.getBundle(ModelUI.PLUGIN_ID), new Path(emptyPath), null);
      }
      imageDescriptor = ImageDescriptor.createFromURL(url);
      _imageRegistry.put(path, imageDescriptor);
    }
    _imageRegistry.put(name, imageDescriptor);
    return imageDescriptor;
  }

}