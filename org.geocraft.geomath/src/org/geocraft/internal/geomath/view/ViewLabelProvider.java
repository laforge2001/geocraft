/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.internal.geomath.view;


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.geocraft.algorithm.IStandaloneAlgorithmDescription;
import org.geocraft.ui.common.image.ImageRegistryUtil;


public class ViewLabelProvider extends LabelProvider {

  private final ResourceManager _resourceManager = new LocalResourceManager(JFaceResources.getResources());

  @Override
  public String getText(final Object obj) {
    return obj.toString();
  }

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
        return _resourceManager.createImage(imageDesc);
      }
    }
    return ImageRegistryUtil.getSharedImages().getImage(org.geocraft.ui.common.image.ISharedImages.IMG_TOOL_24);
  }

  @Override
  public void dispose() {
    _resourceManager.dispose();
    super.dispose();
  }
}
