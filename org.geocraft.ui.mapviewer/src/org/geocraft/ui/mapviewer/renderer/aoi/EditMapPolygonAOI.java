/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.mapviewer.renderer.aoi;


import org.eclipse.jface.action.Action;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;


public class EditMapPolygonAOI extends Action {

  private final MapPolygonAOIRenderer _renderer;

  public EditMapPolygonAOI(final MapPolygonAOIRenderer renderer) {
    _renderer = renderer;
    setText("Edit AOI polygons...");
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_EDIT));
  }

  @Override
  public void run() {
    _renderer.startEdit();
  }

}
