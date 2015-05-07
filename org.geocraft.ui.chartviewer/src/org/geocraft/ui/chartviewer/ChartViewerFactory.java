/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.chartviewer;


import org.eclipse.swt.widgets.Composite;


public class ChartViewerFactory {

  public static PieChartViewer createPieChart(final Composite parent, final String title) {
    return new PieChartViewer(parent, title);
  }

  public static ScatterChartViewer createScatterChart(final Composite parent, final String title, final String xLabel,
      final String yLabel) {
    return new ScatterChartViewer(parent, title, xLabel, yLabel);
  }

  public static GridImageChartViewer createGridImageChart(final Composite parent, final String title,
      final String xLabel, final String yLabel) {
    return new GridImageChartViewer(parent, title, xLabel, yLabel);
  }

  public static HistogramChartViewer createHistogramChart(final Composite parent, final String title,
      final String xLabel, final String yLabel) {
    return new HistogramChartViewer(parent, title, xLabel, yLabel);
  }
}
