/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.action.zoom;


import java.awt.event.ActionEvent;

import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.action.AbstractPlotAction;


/**
 * The basic action for initializing the plot zoom.
 */
public class InitZoomAction extends AbstractPlotAction {

  /** The plot with which the action is associated. */
  //protected ZoomMode _zoomMode;
  /**
   * Constructs a zoom initialization action.
   * @param plot the plot.
   */
  public InitZoomAction(final IPlot plot) {//, final ZoomMode zoomMode) {
    super(plot);
    //_zoomMode = zoomMode;
  }

  /**
   * Invoked when when the zoom initialization action is triggered.
   * @param event the action event.
   */
  public void actionPerformed(final ActionEvent event) {
    // TODO: implement this
    //    _plot.setActionMode(ActionMode.ZoomByFactor);
    //    _plot.setZoomMode(_zoomMode);
    //    Cursor cursor = PlotCursorFactory.getZoomCursor();
    //    _plot.getModelCanvas().getComponent().setCursor(cursor);
  }

}
