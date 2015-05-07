/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.action;


import org.eclipse.jface.action.Action;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.viewer.IViewer;


public class HideLayerModelTree extends Action {

  private final IViewer _viewer;

  public HideLayerModelTree(final IViewer viewer) {
    super("Hide layer model");
    setToolTipText("Layer model is hidden");
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_PREFERENCES));
    _viewer = viewer;
  }

  @Override
  public void run() {
    _viewer.setLayerTreeVisible(false);
  }
}
