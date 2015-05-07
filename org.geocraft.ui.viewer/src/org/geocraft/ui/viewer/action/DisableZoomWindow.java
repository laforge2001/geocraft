/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.action;


import org.eclipse.jface.action.Action;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.viewer.IViewer;


public class DisableZoomWindow extends Action {

  private final IViewer _viewer;

  public DisableZoomWindow(final IViewer viewer) {
    super("Disable zoom window");
    setToolTipText("Hold down the SHIFT key to zoom in on a region of the plot.");
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_ZOOM_OBJECT));
    _viewer = viewer;
  }

  @Override
  public void run() {
    _viewer.zoomWindow(false);
  }
}
