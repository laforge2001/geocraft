/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot.action;


import org.eclipse.jface.action.Action;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.plot.IPlot;


public class EditLayout extends Action {

  private final IPlot _plot;

  public EditLayout(final IPlot plot) {
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_MODIFY_LAYOUT));
    //setImageDescriptor(ImageRegistryUtil.getImageDescriptor("icons/oo/guides-16.png"));
    setToolTipText("Edit the crossplot layout");
    _plot = plot;
  }

  @Override
  public void run() {
    _plot.editCanvasLayout();
  }
}
