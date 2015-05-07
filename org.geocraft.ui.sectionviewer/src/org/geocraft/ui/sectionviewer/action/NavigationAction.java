/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.action;


import org.eclipse.jface.action.Action;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.sectionviewer.ISectionViewer;


public class NavigationAction extends Action {

  private final ISectionViewer _viewer;

  public NavigationAction(final ISectionViewer viewer) {
    _viewer = viewer;
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_NAVIGATOR));
    setToolTipText("Open the section navigator");
  }

  @Override
  public void run() {
    Action action = _viewer.getNavigationAction();
    if (action != null) {
      action.run();
    }
  }
}
