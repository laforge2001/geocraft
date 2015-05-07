/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.ui.model;


import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.geocraft.core.model.base.IPropertiesProvider;


public interface IModelSharedImages extends ISharedImages {

  /**
   * Returns the image associated with the specified properties provider.
   * 
   * @param entity the entity.
   * @return the associated image.
   */
  Image getImage(IPropertiesProvider propProvider);

}
