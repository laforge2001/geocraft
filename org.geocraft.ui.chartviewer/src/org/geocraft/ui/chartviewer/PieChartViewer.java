/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.chartviewer;


import java.util.Map;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.geocraft.core.model.Model;
import org.geocraft.ui.chartviewer.data.PieData;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.PlotScrolling;
import org.geocraft.ui.plot.layout.CanvasLayoutModel;
import org.geocraft.ui.plot.model.IModelSpace;


public class PieChartViewer extends AbstractChartViewer {

  public PieChartViewer(final Composite parent, final String title) {
    super(parent, title, false);
    IPlot plot = getPlot();
    CanvasLayoutModel canvasModel = plot.getCanvasLayoutModel();
    canvasModel.setTopAxisHeight(0);
    canvasModel.setLeftAxisWidth(0);
    canvasModel.setRightAxisWidth(0);
    canvasModel.setBottomAxisHeight(0);
    canvasModel.setTopLabelHeight(0);
    canvasModel.setLeftLabelWidth(0);
    canvasModel.setRightLabelWidth(0);
    canvasModel.setBottomLabelHeight(0);
    plot.setHorizontalAxisGridLineDensity(0);
    plot.setVerticalAxisGridLineDensity(0);
    plot.setHorizontalAxisAnnotationDensity(0);
    plot.setVerticalAxisAnnotationDensity(0);
    plot.setCanvasLayoutModel(canvasModel);
  }

  @Override
  protected IModelSpace initializeModelSpace() {
    return AbstractChartViewer.createModelSpace("", "", 1f);
  }

  @Override
  protected PlotScrolling getPlotScrolling() {
    return PlotScrolling.NONE;
  }

  @Override
  public String getChartType() {
    return "PieChart";
  }

  public PieData addData(final String name, final String[] entryNames, final float[] entryValues, final RGB[] rgbs) {
    PieData data = new PieData(name, entryNames, entryValues, rgbs);
    addObjects(new Object[] { data });
    return data;
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
