/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.chartviewer.action;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.well.WellLogTrace;
import org.geocraft.ui.chartviewer.ChartViewerFactory;
import org.geocraft.ui.chartviewer.ScatterChartViewer;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.multiplot.MultiPlotFactory;
import org.geocraft.ui.multiplot.MultiPlotPart;
import org.geocraft.ui.repository.RepositoryViewData;


public class ScatterPlotAction implements IWorkbenchWindowActionDelegate {

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
        if (entities.length < 2) {
          return;
        }

        // Create empty lists for log traces.
        List<WellLogTrace> logTraces = new ArrayList<WellLogTrace>();

        // Loop thru the selected entities, adding each one to the appropriate list.
        for (Entity entity : entities) {
          if (entity.getClass().equals(WellLogTrace.class)) {
            logTraces.add((WellLogTrace) entity);
          }
        }

        // Determine the number of columns (max is 5).
        int numColumns = logTraces.size() - 1;
        numColumns = Math.min(numColumns, 5);

        // If no supported entities found, simply return.
        if (numColumns <= 0) {
          return;
        }

        ScatterChartViewer scatterChart = null;

        // Create a multi-plot part.
        MultiPlotPart part;
        try {
          String title = "Scatter";
          if (numColumns > 1) {
            title = "Scatters";
          }
          part = MultiPlotFactory.createPart(numColumns);
          part.setTitleAndImage(title, ImageRegistryUtil.getSharedImages().getImage(ISharedImages.IMG_HISTOGRAM));
        } catch (PartInitException ex) {
          return;
        }

        // Add the scatter data for the well log traces.
        for (int i = 0; i < logTraces.size() - 1; i++) {
          WellLogTrace logTrace1 = logTraces.get(i);
          for (int j = i + 1; j < logTraces.size(); j++) {
            WellLogTrace logTrace2 = logTraces.get(j);

            // Create a scatter chart and add it to the multi-chart part.
            scatterChart = ChartViewerFactory.createScatterChart(part.getViewerParent(), "Scatter: "
                + logTrace1.getDisplayName() + " vs " + logTrace2.getDisplayName(), logTrace1.getDisplayName(),
                logTrace2.getDisplayName());
            part.addViewer(scatterChart);
            scatterChart.addObjects(new Object[] { logTrace1, logTrace2 });
          }
        }
      }

    });
  }

  public void selectionChanged(final IAction action, final ISelection selection) {
    // does nothing for now
  }

}
