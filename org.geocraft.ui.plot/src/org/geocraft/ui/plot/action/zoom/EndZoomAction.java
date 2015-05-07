/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.action.zoom;


import java.awt.event.ActionEvent;

import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.action.AbstractPlotAction;


/**
 * The basic action for ending the plot zoom.
 */
public class EndZoomAction extends AbstractPlotAction {

  public EndZoomAction(final IPlot plot) {
    super(plot);
  }

  @SuppressWarnings("unused")
  public void actionPerformed(final ActionEvent e) {
    // TODO: implement this
    //    _plot.setActionMode(ActionMode.General);
    //    Cursor cursor = PlotCursorFactory.getDefaultCursor();
    //    _plot.getModelCanvas().getComponent().setCursor(cursor);
  }

}
