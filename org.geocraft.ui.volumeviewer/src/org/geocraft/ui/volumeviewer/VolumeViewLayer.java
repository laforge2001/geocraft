/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer;


import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.Image;
import org.geocraft.core.model.base.IPropertiesProvider;
import org.geocraft.core.model.base.IPropertiesProviderContainer;
import org.geocraft.core.model.datatypes.SpatialExtent;
import org.geocraft.ui.viewer.ReadoutInfo;
import org.geocraft.ui.viewer.layer.AbstractViewLayer;


public class VolumeViewLayer extends AbstractViewLayer implements IPropertiesProviderContainer, IAdaptable {

  private final VolumeViewRenderer _renderer;

  public VolumeViewLayer(final VolumeViewRenderer renderer) {
    super(renderer.getName(), renderer.getUniqueID(), true, false, true);
    _renderer = renderer;
  }

  @Override
  public Image getImage() {
    return _renderer.getImage();
  }

  public SpatialExtent getExtent() {
    // TODO Auto-generated method stub
    return null;
  }

  public ReadoutInfo getReadoutInfo(final double x, final double y) {
    // TODO Auto-generated method stub
    return null;
  }

  public void redraw() {
    // TODO Auto-generated method stub

  }

  public void refresh() {
    // TODO Auto-generated method stub

  }

  public boolean showReadoutInfo() {
    return _renderer.showReadoutInfo();
  }

  public void showReadoutInfo(final boolean show) {
    _renderer.showReadoutInfo(show);
  }

  public IPropertiesProvider getPropertiesProvider() {
    return _renderer.getPropertiesProvider();
  }

}
