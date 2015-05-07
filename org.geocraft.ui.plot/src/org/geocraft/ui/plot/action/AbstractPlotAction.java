/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.action;


import org.geocraft.ui.plot.IPlot;


public abstract class AbstractPlotAction {

  /** The plot associated with the action. */
  protected IPlot _plot;

  /**
   * The default constructor.
   * @param plot the plot associated with the action.
   */
  public AbstractPlotAction(final IPlot plot) {
    _plot = plot;
  }
}
