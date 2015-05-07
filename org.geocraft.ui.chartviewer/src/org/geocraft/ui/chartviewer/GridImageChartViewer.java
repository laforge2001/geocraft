/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.chartviewer;


import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.geocraft.core.model.Model;
import org.geocraft.ui.chartviewer.data.GridImageData;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.PlotScrolling;
import org.geocraft.ui.plot.layer.PlotLayer;
import org.geocraft.ui.plot.model.IModelSpace;


public class GridImageChartViewer extends AbstractChartViewer {

  public GridImageChartViewer(final Composite parent, final String title, final String xLabel, final String yLabel) {
    super(parent, title, false);
    IPlot plot = getPlot();
    plot.getActiveModelSpace().addLayer(new PlotLayer("Default"));
    setLayerTreeVisible(false);
  }

  @Override
  protected IModelSpace initializeModelSpace() {
    return AbstractChartViewer.createModelSpace("", "");
  }

  @Override
  protected PlotScrolling getPlotScrolling() {
    return PlotScrolling.NONE;
  }

  @Override
  public String getChartType() {
    return "GridImageChart";
  }

  public GridImageData addData(final String name, final float xStart, final float xEnd, final float yStart,
      final float yEnd, final float[][] data) {
    final GridImageData dataObject = new GridImageData(name, xStart, xEnd, yStart, yEnd, data);
    addObjects(new Object[] { dataObject });
    return dataObject;
  }

  public Model getViewerModel() {
    // TODO Auto-generated method stub
    return null;
  }

  public void updateFromModel() {
    // TODO Auto-generated method stub

  }

  public void addRenderer(String klass, Map<String, String> props, String uniqueId) {
    // TODO Auto-generated method stub

  }

}
