/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.layer;


import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;


public class LayeredModelLabelProvider extends LabelProvider {

  @Override
  public String getText(final Object object) {
    // Check if the object is a view layer.
    if (object instanceof IViewLayer) {
      // Return the layer name.
      IViewLayer layer = (IViewLayer) object;
      return layer.getName();
    }
    return object.toString();
  }

  @Override
  public Image getImage(final Object object) {
    // Check if the object is a view layer.
    if (object instanceof IViewLayer) {
      // Return the layer name.
      IViewLayer layer = (IViewLayer) object;
      return layer.getImage();
    }
    return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
  }
}
