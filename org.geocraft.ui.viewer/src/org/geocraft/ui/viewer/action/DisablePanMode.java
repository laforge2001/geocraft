/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.action;


import org.eclipse.jface.action.Action;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.viewer.IViewer;


public class DisablePanMode extends Action {

  private final IViewer _viewer;

  public DisablePanMode(final IViewer viewer) {
    super("Disable pan mode");
    setToolTipText("Pan mode is disabled");
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_DRAG_MODE));
    _viewer = viewer;
  }

  @Override
  public void run() {
    _viewer.pan(false);
  }
}
