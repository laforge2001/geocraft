/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.listener;


import org.geocraft.ui.plot.event.PlotEvent;


/**
 * Interface for a plot listener.
 */
public interface IPlotListener {

  /**
   * Invoked when a plot is updated.
   * 
   * @param plotEvent
   *            the plot event.
   */
  void plotUpdated(PlotEvent plotEvent);
}
