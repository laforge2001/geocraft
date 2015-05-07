/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.listener;


import org.geocraft.ui.plot.action.PlotMouseEvent;


public interface IPlotMouseListener {

  void mouseDoubleClick(PlotMouseEvent event);

  void mouseDown(PlotMouseEvent event);

  void mouseUp(PlotMouseEvent event);

  void mouseMove(PlotMouseEvent event);

  void mouseEnter(PlotMouseEvent event);

  void mouseExit(PlotMouseEvent event);

  void mouseHover(PlotMouseEvent event);
}
