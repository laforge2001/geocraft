/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.action;


import org.eclipse.jface.action.Action;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.viewer.IViewer;


/**
 * Disables the cursor reception status of a viewer.
 */
public class DisableReception extends Action {

  /** The viewer whose reception status is to be disabled. */
  private final IViewer _viewer;

  /**
   * Constructs an action for disabling the reception status of a viewer.
   * 
   * @param viewer the viewer whose reception status is to be disabled.
   */
  public DisableReception(final IViewer viewer) {
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_RECEIVE_CURSOR));
    setToolTipText("Cursor reception is disabled");
    _viewer = viewer;
  }

  @Override
  public void run() {
    _viewer.setCursorReception(false);
  }
}
