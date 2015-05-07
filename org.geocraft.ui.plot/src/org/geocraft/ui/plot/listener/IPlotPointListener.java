/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.listener;

import org.geocraft.ui.plot.event.PointEvent;


/**
 * The listener interface for receing plot point events.
 */
public interface IPlotPointListener {

  /**
   * Invoked when the plot point is updated.
   * @param event the plot point event.
   */
  void pointUpdated(PointEvent event);
}
