/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.action;


import org.eclipse.jface.action.Action;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.viewer.IViewer;


public class HomeView extends Action {

  private final IViewer _viewer;

  public HomeView(final IViewer viewer) {
    setToolTipText("Home View");
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(org.geocraft.ui.common.image.ISharedImages.IMG_HOME));
    setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ETOOL_HOME_NAV));
    _viewer = viewer;
  }

  @Override
  public void run() {
    _viewer.home();
  }
}
