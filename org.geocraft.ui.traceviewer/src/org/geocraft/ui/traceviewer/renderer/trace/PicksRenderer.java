/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.traceviewer.renderer.trace;


import java.util.ArrayList;
import java.util.List;

import org.geocraft.core.model.Model;
import org.geocraft.core.model.event.DataSelection;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.object.IPlotPolyline;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.object.PlotPolyline;
import org.geocraft.ui.traceviewer.TraceViewRenderer;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.ReadoutInfo;


public class PicksRenderer extends TraceViewRenderer {

  private PicksData _picksData;

  public PicksRenderer() {
    super("Picks Renderer");
    _appendCursorInfo = true;
  }

  @Override
  public ReadoutInfo getReadoutInfo(final int traceNum, final float z) {
    if (traceNum >= 1 && traceNum <= _picksData.getNumPicks()) {
      List<String> keys = new ArrayList<String>();
      List<String> values = new ArrayList<String>();
      keys.add("Trace");
      values.add(Float.toString(_picksData.getPicks()[traceNum - 1]));
      return new ReadoutInfo(_name, keys, values);
    }
    return new ReadoutInfo(_name);
  }

  @Override
  protected void addPlotShapes() {
    IPlotPolyline polyline = new PlotPolyline();
    polyline.setLineColor(_picksData.getColor());
    polyline.setLineWidth(2);
    polyline.setLineStyle(LineStyle.SOLID);
    polyline.setPointColor(_picksData.getColor());
    polyline.setPointSize(1);
    polyline.setPointStyle(PointStyle.NONE);
    for (int i = 0; i < _picksData.getNumPicks(); i++) {
      double x = i + 1;
      double y = _picksData.getPicks()[i];
      PlotPoint point = new PlotPoint(x, y, 0);
      point.setPropertyInheritance(true);
      polyline.addPoint(point);
    }
    addShape(polyline);
  }

  @Override
  protected void addPopupMenuActions() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree(IViewer.GRID_FOLDER, autoUpdate);
  }

  @Override
  public DataSelection getDataSelection(final double x, final double y) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void redraw() {

  }

  @Override
  protected void setNameAndImage() {
    setName(_name);
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    _picksData = (PicksData) objects[0];
  }

  public Object[] getRenderedObjects() {
    return new Object[] { _picksData };
  }

  public Model getSettingsModel() {
    // TODO Auto-generated method stub
    return null;
  }

}
