/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.action.zoom;


import java.awt.geom.Point2D;

import org.eclipse.swt.graphics.RGB;
import org.geocraft.ui.plot.action.AbstractPlotMouseAction;
import org.geocraft.ui.plot.action.PlotActionMask;
import org.geocraft.ui.plot.action.PlotMouseEvent;
import org.geocraft.ui.plot.defs.FillStyle;
import org.geocraft.ui.plot.layer.IPlotLayer;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPolygon;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.object.PlotPolygon;


public class StartZoomMotionAction extends AbstractPlotMouseAction {

  /**
   * Constructs an instance of StartZoomMotionAction.
   * @param mask the action mask (mouse buttons, clicks, modifiers).
   */
  public StartZoomMotionAction(final PlotActionMask mask) {
    super(mask, "Start Zoom Window", "Start the zoom window cursor definition.");
  }

  @Override
  public void actionPerformed(final PlotMouseEvent event) {
    IModelSpaceCanvas canvas = event.getModelCanvas();
    IModelSpace modelSpace = event.getPlot().getActiveModelSpace();
    if (modelSpace == null) {
      return;
    }
    IPlotLayer layer = modelSpace.getActiveLayer();
    if (layer == null) {
      return;
    }
    IPlotPolygon zoomRect = canvas.getZoomRectangle();
    if (zoomRect == null) {
      zoomRect = new PlotPolygon();
      zoomRect.setName("Zoom");
    }
    zoomRect.clear();
    RGB lineColor = new RGB(255, 0, 0);
    zoomRect.setLineColor(lineColor);
    zoomRect.setLineWidth(2);
    zoomRect.setFillStyle(FillStyle.NONE);
    zoomRect.select();
    Point2D.Double modelCoord = event.getModelCoord();
    double x = modelCoord.getX();
    double y = modelCoord.getY();
    for (int i = 0; i < 4; i++) {
      IPlotPoint point = new PlotPoint(x, y, 0);
      zoomRect.addPoint(point);
    }
    layer.addShape(zoomRect, true);
    canvas.setZoomRectangle(zoomRect);
  }
}
