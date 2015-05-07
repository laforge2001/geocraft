/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.layer;


import org.geocraft.core.model.datatypes.SpatialExtent;
import org.geocraft.ui.viewer.ReadoutInfo;


/**
 * Defines a container view layer that acts like a directory in a file system.
 * It is not rendered in the viewer, but contains a collection of child layers
 * that are displayed. This allows, for example, the grouping of all the wells
 * in the layered model under a single view group layer so that the user can
 * show/hide all the wells with a single mouse click.
 */
public class ViewGroupLayer extends AbstractViewLayer {

  /**
   * Constructs a view group layer.
   * <p>
   * By default, this allows children, renaming and deletion.
   */
  public ViewGroupLayer(final String name, final String uniqueID) {
    this(name, uniqueID, true, true);
  }

  /**
   * Constructs a view group layer.
   * <p>
   * By default, this allows children.
   * 
   * @param allowsRename <i>true</i> if the view layer allows renaming; <i>false</i> if not.
   * @param allowsDelete <i>true</i> if the view layer allows deletion; <i>false</i> if not.
   */
  public ViewGroupLayer(final String name, final String uniqueID, final boolean allowsRename, final boolean allowsDelete) {
    super(name, uniqueID, true, allowsRename, allowsDelete);

    // Turn off readout info.
    showReadoutInfo(false);
  }

  public SpatialExtent getExtent() {
    return null;
  }

  public void refresh() {
    // No action required.
  }

  public void redraw() {
    // No action required.
  }

  public boolean showReadoutInfo() {
    return false;
  }

  public void showReadoutInfo(final boolean show) {
    if (show) {
      throw new UnsupportedOperationException("Cannot append cursor info for a view group layer.");
    }
  }

  public ReadoutInfo getReadoutInfo(double x, double y) {
    return new ReadoutInfo(getName());
  }

  @Override
  public Object getUserObject() {
    return getName();
  }

}
