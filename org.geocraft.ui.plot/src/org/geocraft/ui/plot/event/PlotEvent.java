/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.event;


import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.defs.PlotEventType;


/**
 * The event fired to listeners when a plot is updated.
 */
public class PlotEvent extends AbstractPlotEvent {

  /** The plot that was updated. */
  private final IPlot _plot;

  /**
   * The default constructor.
   * 
   * @param eventType
   *            the event type.
   * @param plot
   *            the plot that was updated.
   */
  public PlotEvent(final PlotEventType eventType, final IPlot plot) {
    super(eventType);
    _plot = plot;
  }

  /**
   * Gets the plot that was updated.
   * 
   * @return the plot that was updated.
   */
  public IPlot getPlot() {
    return _plot;
  }
}
