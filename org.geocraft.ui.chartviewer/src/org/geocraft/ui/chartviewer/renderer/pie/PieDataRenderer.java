/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.chartviewer.renderer.pie;


import java.text.NumberFormat;

import org.eclipse.swt.graphics.RGB;
import org.geocraft.ui.chartviewer.AbstractChartViewer;
import org.geocraft.ui.chartviewer.PieChartViewer;
import org.geocraft.ui.chartviewer.data.IChartData;
import org.geocraft.ui.chartviewer.data.PieData;
import org.geocraft.ui.chartviewer.renderer.ChartViewRenderer;
import org.geocraft.ui.plot.defs.FillStyle;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPolygon;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.object.PlotPolygon;


public class PieDataRenderer extends ChartViewRenderer<PieChartViewer> {

  private PieData _pieData;

  private IPlotPolygon[] _pieWedges;

  private final NumberFormat _formatter;

  public PieDataRenderer() {
    super("PieData Renderer");
    _formatter = NumberFormat.getNumberInstance();
    _formatter.setGroupingUsed(false);
    _formatter.setMinimumIntegerDigits(1);
    _formatter.setMaximumIntegerDigits(3);
    _formatter.setMinimumFractionDigits(1);
    _formatter.setMaximumFractionDigits(2);
  }

  @Override
  public IChartData getRenderedChartData() {
    return _pieData;
  }

  @Override
  protected void addPlotShapes() {
    double startAngleInRadians = 0;
    double startAngleInDegrees = 0;
    String[] names = _pieData.getEntryNames();
    _pieWedges = new IPlotPolygon[names.length];
    for (int i = 0; i < names.length; i++) {
      float percent = _pieData.getWeight(names[i]);

      double endAngleInRadians = startAngleInRadians + percent * 2 * Math.PI;
      double endAngleInDegrees = endAngleInRadians * 180 / Math.PI;

      double sx = Math.cos(startAngleInRadians) * 100;
      double sy = Math.sin(startAngleInRadians) * 100;
      double ex = Math.cos(endAngleInRadians) * 100;
      double ey = Math.sin(endAngleInRadians) * 100;

      RGB color = _pieData.getRGB(names[i]);
      _pieWedges[i] = new PlotPolygon();
      _pieWedges[i].setName(names[i]);
      _pieWedges[i].setSelectable(false);
      _pieWedges[i].setEditable(false);
      _pieWedges[i].setLineColor(new RGB(0, 0, 0));
      _pieWedges[i].setLineStyle(LineStyle.SOLID);
      _pieWedges[i].setLineWidth(1);
      _pieWedges[i].setFillColor(color);
      _pieWedges[i].setFillStyle(FillStyle.SOLID);
      _pieWedges[i].setPointStyle(PointStyle.NONE);
      _pieWedges[i].addPoint(createPoint(0, 0));
      _pieWedges[i].addPoint(createPoint(sx, sy));
      int startAngleInDegreesInt = (int) Math.ceil(startAngleInDegrees);
      int endAngleInDegreesInt = (int) Math.floor(endAngleInDegrees);
      for (int j = startAngleInDegreesInt; j <= endAngleInDegreesInt; j++) {
        double angleInRadians = j * Math.PI / 180;
        double x = Math.cos(angleInRadians) * 100;
        double y = Math.sin(angleInRadians) * 100;
        _pieWedges[i].addPoint(createPoint(x, y));
      }
      _pieWedges[i].addPoint(createPoint(ex, ey));
      addShape(_pieWedges[i]);

      startAngleInRadians = endAngleInRadians;
      startAngleInDegrees = endAngleInDegrees;
    }
    getViewer().getPlot().getModelSpaceCanvas().checkAspectRatio();
  }

  private IPlotPoint createPoint(final double x, final double y) {
    IPlotPoint point = new PlotPoint(x, y, 0);
    point.setPropertyInheritance(true);
    return point;
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree(AbstractChartViewer.DEFAULT_FOLDER, autoUpdate);
  }

  @Override
  public Object[] getRenderedObjects() {
    return new Object[] { _pieData };
  }

  @Override
  protected void setRenderedObjects(Object[] objects) {
    _pieData = (PieData) objects[0];
  }

  @Override
  protected void setNameAndImage() {
    setName(_pieData.getDisplayName());
  }

  /* (non-Javadoc)
   * @see org.geocraft.ui.viewer.IRenderer#getSettingsModel()
   */
  @Override
  public PieDataRendererModel getSettingsModel() {
    // TODO Auto-generated method stub
    return null;
  }
}
