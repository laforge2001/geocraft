/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.action;


import org.eclipse.jface.action.Action;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.viewer.IViewer;


/**
 * Enables the cursor reception status of a viewer.
 */
public class EnableReception extends Action {

  /** The viewer whose reception status is to be enabled. */
  private final IViewer _viewer;

  /**
   * Constructs an action for enabling the reception status of a viewer.
   * 
   * @param viewer the viewer whose reception status is to be enabled.
   */
  public EnableReception(final IViewer viewer) {
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_RECEIVE_CURSOR));
    setToolTipText("Cursor reception is enabled");
    _viewer = viewer;
  }

  @Override
  public void run() {
    _viewer.setCursorReception(true);
  }
}
