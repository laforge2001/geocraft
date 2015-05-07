/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.layer;


/**
 * The listener interface for the layered view model.
 */
public interface ILayeredModelListener {

  /**
   * Invoked when the model is updated (i.e. when layers are added or removed).
   */
  void layeredModelUpdated(ViewLayerEvent event);
}
