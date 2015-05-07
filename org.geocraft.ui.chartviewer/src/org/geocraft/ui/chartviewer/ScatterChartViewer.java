/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.chartviewer;


import java.util.Map;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.ui.chartviewer.data.PolarChartData;
import org.geocraft.ui.chartviewer.data.ScatterData;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.PlotScrolling;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.layer.PlotLayer;
import org.geocraft.ui.plot.model.IModelSpace;


public class ScatterChartViewer extends AbstractChartViewer {

  public ScatterChartViewer(final Composite parent, final String title, final String xLabel, final String yLabel) {
    super(parent, title, false);
    IPlot plot = getPlot();
    plot.getActiveModelSpace().addLayer(new PlotLayer("Default"));
    plot.getActiveModelSpace().getAxisX().getLabel().setText(xLabel);
    plot.getActiveModelSpace().getAxisY().getLabel().setText(yLabel);
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
    return "ScatterChart";
  }

  public void addData(final ScatterData data) {
    addObjects(new Object[] { data }, true);
  }

  public ScatterData addData(final String name, final float[] xs, final float[] ys) {
    return addData(name, xs, ys, ChartUtil.createRandomRGB(), ChartUtil.createRandomPointStyle(), 4);
  }

  public ScatterData addData(final String name, final float[] xs, final float[] ys, final RGB pointColor,
      final PointStyle pointStyle, final int pointSize) {
    return addData(name, xs, ys, pointColor, pointStyle, pointSize, false);
  }

  public ScatterData addData(final String name, final float[] xs, final float[] ys, final RGB pointColor,
      final PointStyle pointStyle, final int pointSize, final boolean connectPoints) {
    ScatterData data = new ScatterData(name, xs, ys, pointColor, pointStyle, pointSize);
    data.setConnectPoints(connectPoints);
    addData(data);
    return data;
  }

  public PolarChartData addData(final String name, final float[][] xPnts, final float[][] yPnts, final float[][] data,
      final Grid3d grid) {
    PostStack3d volume = null;
    final PolarChartData dataObject = new PolarChartData(name, xPnts, yPnts, data, grid, volume);
    addObjects(new Object[] { dataObject });
    return dataObject;
  }

  public PolarChartData addData(final String name, final float[][] xPnts, final float[][] yPnts, final float[][] data,
      final PostStack3d volume) {
    Grid3d grid = null;
    final PolarChartData dataObject = new PolarChartData(name, xPnts, yPnts, data, grid, volume);
    addObjects(new Object[] { dataObject });
    return dataObject;
  }

  public ScatterData addData(final String name, final float[] xPnts, final float[] yPnts, final String[] labels,
      final RGB labelColor, final Font labelFont) {

    // Point style is none for labels
    PointStyle pointStyle = PointStyle.NONE;

    // Determine the label size from the label font
    int labelSize = labelFont.getFontData()[0].getHeight();

    ScatterData dataObject = new ScatterData(name, xPnts, yPnts, labelColor, pointStyle, labelSize);
    dataObject.addLabels(labels, labelFont);
    addObjects(new Object[] { dataObject });
    return dataObject;
  }

  public void setBounds(final float xStart, final float xEnd, final float yStart, final float yEnd) {
    IModelSpace modelSpace = getPlot().getActiveModelSpace();
    modelSpace.setDefaultBounds(xStart, xEnd, yStart, yEnd);
    modelSpace.setViewableBounds(xStart, xEnd, yStart, yEnd);
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
