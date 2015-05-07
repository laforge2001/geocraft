/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.action;


import org.eclipse.jface.action.Action;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.sectionviewer.ISectionViewer;


public class IncrementSection extends Action {

  private final ISectionViewer _viewer;

  public IncrementSection(final ISectionViewer viewer) {
    _viewer = viewer;
    setToolTipText("Increments the inline/xline section");
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_NEXT));
  }

  @Override
  public void run() {
    _viewer.incrementSection();
  }
}
