/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.action;


import org.eclipse.jface.action.Action;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.viewer.IViewer;


public class EnablePanMode extends Action {

  private final IViewer _viewer;

  public EnablePanMode(final IViewer viewer) {
    super("Enable pan mode");
    setToolTipText("Pan mode is enabled");
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_DRAG_MODE));
    _viewer = viewer;
  }

  @Override
  public void run() {
    _viewer.pan(true);
  }
}
