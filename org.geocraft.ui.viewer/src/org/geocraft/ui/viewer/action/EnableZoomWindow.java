/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.action;


import org.eclipse.jface.action.Action;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.viewer.IViewer;


public class EnableZoomWindow extends Action {

  private final IViewer _viewer;

  public EnableZoomWindow(final IViewer viewer) {
    super("Enable zoom window");
    setToolTipText("Zoom window is enabled");
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_ZOOM_OBJECT));
    _viewer = viewer;
  }

  @Override
  public void run() {
    _viewer.zoomWindow(true);
  }
}
