/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.layer;


/**
 * Defines the  interface for a view layer listener.
 */
public interface ViewLayerListener {

  /**
   * Invoked when the layer is updated.
   * @param event the view layer event.
   */
  void viewLayerUpdated(ViewLayerEvent event);
}
