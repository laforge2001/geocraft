/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.action;


import org.eclipse.jface.action.Action;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.viewer.IViewer;


public class ZoomIn extends Action {

  private final IViewer _viewer;

  public ZoomIn(final IViewer viewer) {
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_ZOOM_IN));
    setToolTipText("Zoom In");
    _viewer = viewer;
  }

  @Override
  public void run() {
    _viewer.zoomIn();
  }
}
