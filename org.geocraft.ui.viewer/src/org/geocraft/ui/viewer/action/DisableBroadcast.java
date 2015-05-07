/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.action;


import org.eclipse.jface.action.Action;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.viewer.IViewer;


/**
 * Disables the cursor broadcast status of a viewer.
 */
public class DisableBroadcast extends Action {

  /** The viewer whose broadcast status is to be disabled. */
  private final IViewer _viewer;

  /**
   * Constructs an action for disabling the broadcast status of a viewer.
   * 
   * @param viewer the viewer whose broadcast status is to be disabled.
   */
  public DisableBroadcast(final IViewer viewer) {
    super("Disable cursor broadcast");
    setToolTipText("Cursor broadcast is disabled");
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_SEND_CURSOR));
    _viewer = viewer;
  }

  @Override
  public void run() {
    _viewer.setCursorBroadcast(false);
  }
}
