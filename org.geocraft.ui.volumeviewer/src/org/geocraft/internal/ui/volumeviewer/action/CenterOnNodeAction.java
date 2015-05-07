/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.ui.volumeviewer.action;


import org.eclipse.jface.action.Action;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.volumeviewer.VolumeViewRenderer;
import org.geocraft.ui.volumeviewer.VolumeViewer;


public class CenterOnNodeAction extends Action {

  VolumeViewer _viewer;

  VolumeViewRenderer _renderer;

  public CenterOnNodeAction(final VolumeViewer viewer, final VolumeViewRenderer renderer) {
    super();
    setText("Center on Node");
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_ZOOM_OBJECT));
    _viewer = viewer;
    _renderer = renderer;
  }

  @Override
  public void run() {
    if (_viewer != null && _renderer != null) {
      _viewer.centerOnSpatial(_renderer.getSpatials(_viewer.getCurrentDomain()));
    }
  }
}
