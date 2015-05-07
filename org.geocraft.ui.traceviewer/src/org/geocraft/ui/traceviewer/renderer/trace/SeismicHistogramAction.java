/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.traceviewer.renderer.trace;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.geocraft.ui.chartviewer.ChartViewerFactory;
import org.geocraft.ui.chartviewer.HistogramChartViewer;
import org.geocraft.ui.chartviewer.data.HistogramData;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.multiplot.MultiPlotFactory;
import org.geocraft.ui.multiplot.MultiPlotPart;


/**
 * This action displays a histogram of the data currently displayed by a seismic dataset renderer.
 */
public class SeismicHistogramAction extends Action {

  private final TraceDataRenderer _renderer;

  public SeismicHistogramAction(final TraceDataRenderer renderer) {
    _renderer = renderer;
    setText("Histogram...");
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_HISTOGRAM));
  }

  @Override
  public void run() {

    Display.getDefault().asyncExec(new Runnable() {

      public void run() {
        // Get the histogram data from the renderer.
        final HistogramData histogramData = _renderer.getHistogramData();
        // If null, then no data is currently display, so inform the user.
        if (histogramData == null) {
          MessageDialog.openError(Display.getDefault().getActiveShell(), "Histogram Error",
              "No data currently displayed");
          return;
        }

        // Create the view part in which to put the histogram.
        MultiPlotPart part;
        try {
          String title = "Seismic Histogram";
          part = MultiPlotFactory.createPart(1);
          part.setTitleAndImage(title, ImageRegistryUtil.getSharedImages().getImage(ISharedImages.IMG_HISTOGRAM));
        } catch (PartInitException ex) {
          return;
        }

        // Create a histogram chart and add it to the view part.
        HistogramChartViewer histogramChart = ChartViewerFactory.createHistogramChart(part.getViewerParent(),
            "Histogram", "Value", "Count");
        part.addViewer(histogramChart);
        histogramChart.addObjects(new Object[] { histogramData });
      }

    });
  }
}
