/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.listener;


import org.geocraft.ui.plot.event.PlotLayerEvent;


/**
 * The listener interface for receiving plot layer events.
 */
public interface IPlotLayerListener {

  /**
   * Invoked when the plot layer is updated.
   * @param event the plot layer event.
   */
  void layerUpdated(PlotLayerEvent event);
}
