/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.action;


import org.eclipse.jface.action.Action;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.viewer.IViewer;


public class ShowLayerModelTree extends Action {

  private final IViewer _viewer;

  public ShowLayerModelTree(final IViewer viewer) {
    super("Show layer model");
    setToolTipText("Layer model is shown");
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_PREFERENCES));
    _viewer = viewer;
  }

  @Override
  public void run() {
    _viewer.setLayerTreeVisible(true);
  }
}
