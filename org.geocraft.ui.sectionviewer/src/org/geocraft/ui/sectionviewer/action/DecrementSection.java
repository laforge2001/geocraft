/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.action;


import org.eclipse.jface.action.Action;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.sectionviewer.ISectionViewer;


public class DecrementSection extends Action {

  private final ISectionViewer _viewer;

  public DecrementSection(final ISectionViewer viewer) {
    _viewer = viewer;
    setToolTipText("Decrement the inline/xline section");
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_PREVIOUS));
  }

  @Override
  public void run() {
    _viewer.decrementSection();
  }
}
