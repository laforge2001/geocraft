/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.internal.geomath.view;


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.geocraft.algorithm.IStandaloneAlgorithmDescription;
import org.geocraft.ui.common.image.ImageRegistryUtil;


public class ViewLabelProvider extends LabelProvider {

  @Override
  public String getText(final Object obj) {
    return obj.toString();
  }

  //  public Image getImage(Object obj) {
  //    String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
  //    if (obj instanceof TreeParent) {
  //      imageKey = ISharedImages.IMG_OBJ_FOLDER;
  //    }
  //    return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
  //  }

  @Override
  public Image getImage(final Object object) {
    if (object instanceof TreeParent) {
      return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
    }
    TreeObject treeObject = (TreeObject) object;
    IStandaloneAlgorithmDescription toolDesc = treeObject.getStandaloneAlgorithm();
    if (toolDesc != null) {
      ImageDescriptor imageDesc = treeObject.getStandaloneAlgorithm().getIcon();
      if (imageDesc != null) {
        return imageDesc.createImage();
      }
    }
    return ImageRegistryUtil.getSharedImages().getImage(org.geocraft.ui.common.image.ISharedImages.IMG_TOOL_24);
  }
}
