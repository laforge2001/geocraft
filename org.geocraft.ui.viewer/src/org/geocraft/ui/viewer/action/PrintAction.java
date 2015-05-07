/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.action;


import org.eclipse.jface.action.Action;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.viewer.IViewer;


public class PrintAction extends Action {

  private final IViewer _viewer;

  public PrintAction(final IViewer viewer) {
    setToolTipText("Prints the view contents");
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_PRINT));
    _viewer = viewer;
  }

  @Override
  public void run() {
    _viewer.print();
  }
}
