/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.action;


import org.eclipse.jface.action.Action;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.viewer.IViewer;


/**
 * Enables the cursor broadcast status of a viewer.
 */
public class EnableBroadcast extends Action {

  /** The viewer whose broadcast status is to be enabled. */
  private final IViewer _viewer;

  /**
   * Constructs an action for enabling the broadcast status of a viewer.
   * 
   * @param viewer the viewer whose broadcast status is to be enabled.
   */
  public EnableBroadcast(final IViewer viewer) {
    super("Enable cursor broadcast");
    setToolTipText("Cursor broadcast is enabled\nMouse move to broadcast the current cursor location\nMB3 to broadcast a select and toggle on/off the cursor move");
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_SEND_CURSOR));
    _viewer = viewer;
  }

  @Override
  public void run() {
    _viewer.setCursorBroadcast(true);
  }
}
