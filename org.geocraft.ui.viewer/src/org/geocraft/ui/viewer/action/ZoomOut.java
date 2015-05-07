/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.action;


import org.eclipse.jface.action.Action;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.viewer.IViewer;


public class ZoomOut extends Action {

  private final IViewer _viewer;

  public ZoomOut(final IViewer viewer) {
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_ZOOM_OUT));
    setToolTipText("Zoom Out");
    _viewer = viewer;
  }

  @Override
  public void run() {
    _viewer.zoomOut();
  }
}
