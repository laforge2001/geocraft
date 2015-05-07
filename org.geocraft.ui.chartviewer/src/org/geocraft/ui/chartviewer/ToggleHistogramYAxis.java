/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.chartviewer;


import org.eclipse.jface.action.Action;


public class ToggleHistogramYAxis extends Action {

  private HistogramChartViewer _viewer;

  public ToggleHistogramYAxis(HistogramChartViewer viewer) {
    setText("Toggle Y Axis");
    _viewer = viewer;
  }

  @Override
  public void run() {
    _viewer.toggleVerticalAxis();
  }
}
