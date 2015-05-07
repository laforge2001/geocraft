/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.action;


import org.eclipse.jface.action.Action;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.sectionviewer.ISectionViewer;


public class RedrawSection extends Action {

  private final ISectionViewer _viewer;

  public RedrawSection(final ISectionViewer viewer) {
    _viewer = viewer;
    setToolTipText("Redraws the section");
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_REFRESH));
  }

  @Override
  public void run() {
    _viewer.redrawAllRenderers();
  }
}
