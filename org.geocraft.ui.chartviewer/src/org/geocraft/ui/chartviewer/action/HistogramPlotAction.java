/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.chartviewer.action;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.grid.Grid2d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.well.WellLogTrace;
import org.geocraft.ui.chartviewer.ChartViewerFactory;
import org.geocraft.ui.chartviewer.HistogramChartViewer;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.multiplot.MultiPlotFactory;
import org.geocraft.ui.multiplot.MultiPlotPart;
import org.geocraft.ui.repository.RepositoryViewData;


public class HistogramPlotAction implements IWorkbenchWindowActionDelegate {

  public void dispose() {
    // Nothing to do.
  }

  public void init(final IWorkbenchWindow window) {
    // Nothing to do.
  }

  public void run(final IAction action) {
    Display.getDefault().asyncExec(new Runnable() {

      public void run() {

        // Get the entities currently selected in the repository.
        Entity[] entities = RepositoryViewData.getSelectedEntities();

        // If no entities selected, simply return.
        if (entities.length == 0) {
          return;
        }

        // Create empty lists for grids, volumes and log traces.
        List<Grid2d> grid2ds = new ArrayList<Grid2d>();
        List<Grid3d> grid3ds = new ArrayList<Grid3d>();
        List<PostStack3d> poststack3ds = new ArrayList<PostStack3d>();
        List<WellLogTrace> logTraces = new ArrayList<WellLogTrace>();

        // Loop thru the selected entities, adding each one to the appropriate list.
        for (Entity entity : entities) {
          if (entity.getClass().equals(Grid2d.class)) {
            grid2ds.add((Grid2d) entity);
          } else if (entity.getClass().equals(Grid3d.class)) {
            grid3ds.add((Grid3d) entity);
          } else if (entity.getClass().equals(PostStack3d.class)) {
            poststack3ds.add((PostStack3d) entity);
          } else if (entity.getClass().equals(WellLogTrace.class)) {
            logTraces.add((WellLogTrace) entity);
          }
        }

        // Determine the number of columns (max is 5).
        int numColumns = grid3ds.size() + grid2ds.size() + poststack3ds.size() + logTraces.size();
        numColumns = Math.min(numColumns, 5);

        // If no supported entities found, simply return.
        if (numColumns == 0) {
          return;
        }

        HistogramChartViewer histogramChart = null;
        boolean singleChart = false;
        if (numColumns > 1) {
          Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
          singleChart = MessageDialog.openQuestion(shell, "Histogram Options",
              "Do you want to display all histograms in a single chart?");
          if (singleChart) {
            numColumns = 1;
          }
        }

        // Create a multi-plot part.
        MultiPlotPart part;
        try {
          String title = "Histogram";
          if (numColumns > 1) {
            title = "Histograms";
          }
          part = MultiPlotFactory.createPart(numColumns);
          part.setTitleAndImage(title, ImageRegistryUtil.getSharedImages().getImage(ISharedImages.IMG_HISTOGRAM));
        } catch (PartInitException ex) {
          return;
        }

        // If putting all the histograms into a single chart, then create the chart and add it to the multi-plot part.
        if (singleChart) {
          // Create a histogram chart and add it to the multi-chart part.
          histogramChart = ChartViewerFactory.createHistogramChart(part.getViewerParent(), "Histogram", "Value",
              "Percentage");
          part.addViewer(histogramChart);
        }

        // Add the histogram data for the 2d grids.
        for (Grid2d grid2d : grid2ds) {
          if (!singleChart) {
            // Create a histogram chart and add it to the multi-chart part.
            histogramChart = ChartViewerFactory.createHistogramChart(part.getViewerParent(), "Histogram: "
                + grid2d.getDisplayName(), "Value", "Percentage");
            part.addViewer(histogramChart);
          }
          histogramChart.addObjects(new Object[] { grid2d });
        }

        // Add the histogram data for the 3d grids.
        for (Grid3d grid3d : grid3ds) {
          if (!singleChart) {
            // Create a histogram chart and add it to the multi-chart part.
            histogramChart = ChartViewerFactory.createHistogramChart(part.getViewerParent(), "Histogram: "
                + grid3d.getDisplayName(), "Value", "Percentage");
            part.addViewer(histogramChart);
          }
          histogramChart.addObjects(new Object[] { grid3d });
        }

        // Add the histogram data for the 3d volumes.
        for (PostStack3d poststack3d : poststack3ds) {
          if (!singleChart) {
            // Create a histogram chart and add it to the multi-chart part.
            histogramChart = ChartViewerFactory.createHistogramChart(part.getViewerParent(), "Histogram: "
                + poststack3d.getDisplayName(), "Value", "Percentage");
            part.addViewer(histogramChart);
          }
          histogramChart.addObjects(new Object[] { poststack3d });
        }

        // Add the histogram data for the well log traces.
        for (WellLogTrace logTrace : logTraces) {
          if (!singleChart) {
            // Create a histogram chart and add it to the multi-chart part.
            histogramChart = ChartViewerFactory.createHistogramChart(part.getViewerParent(), "Histogram: "
                + logTrace.getDisplayName(), "Value", "Percentage");
            part.addViewer(histogramChart);
          }
          histogramChart.addObjects(new Object[] { logTrace });
        }
      }

    });
  }

  public void selectionChanged(final IAction action, final ISelection selection) {
    // does nothing for now
  }

}
