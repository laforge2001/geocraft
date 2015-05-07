/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.action.zoom;


import java.awt.geom.Point2D;

import org.geocraft.ui.plot.action.AbstractPlotMouseAction;
import org.geocraft.ui.plot.action.PlotActionMask;
import org.geocraft.ui.plot.action.PlotMouseEvent;
import org.geocraft.ui.plot.axis.IAxis;
import org.geocraft.ui.plot.defs.UpdateLevel;
import org.geocraft.ui.plot.layer.IPlotLayer;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.object.IPlotPolygon;


public class EndZoomMotionAction extends AbstractPlotMouseAction {

  /**
   * Constructs an instance of PlotZoomEndMotionAction.
   * @param mask the action mask (mouse buttons, clicks, modifiers).
   */
  public EndZoomMotionAction(final PlotActionMask mask) {
    super(mask, "Start Zoom Window", "Start the zoom window cursor definition.");
  }

  public void actionPerformed(final PlotMouseEvent event) {
    IModelSpaceCanvas canvas = event.getModelCanvas();
    if (canvas == null) {
      throw new RuntimeException("No model canvas.");
    }
    IPlotPolygon zoomRect = canvas.getZoomRectangle();
    if (zoomRect == null) {
      return;
    }
    IPlotLayer layer = zoomRect.getLayer();
    if (layer == null) {
      return;
    }
    IModelSpace model = layer.getModelSpace();
    if (model == null) {
      return;
    }
    layer.removeShape(zoomRect);
    double xStart = zoomRect.getPoint(0).getX();
    double xEnd = zoomRect.getPoint(2).getX();
    double yStart = zoomRect.getPoint(0).getY();
    double yEnd = zoomRect.getPoint(2).getY();

    Point2D.Double pixelStart = new Point2D.Double(0, 0);
    canvas.transformModelToPixel(model, xStart, yStart, pixelStart);
    Point2D.Double pixelEnd = new Point2D.Double(0, 0);
    canvas.transformModelToPixel(model, xEnd, yEnd, pixelEnd);

    for (IModelSpace modelSpace : event.getPlot().getModelSpaces()) {
      Point2D.Double modelStart = new Point2D.Double(0, 0);
      canvas.transformPixelToModel(modelSpace, (int) pixelStart.getX(), (int) pixelStart.getY(), modelStart);
      Point2D.Double modelEnd = new Point2D.Double(0, 0);
      canvas.transformPixelToModel(modelSpace, (int) pixelEnd.getX(), (int) pixelEnd.getY(), modelEnd);
      xStart = modelStart.getX();
      yStart = modelStart.getY();
      xEnd = modelEnd.getX();
      yEnd = modelEnd.getY();
      double xMin = Math.min(xStart, xEnd);
      double xMax = Math.max(xStart, xEnd);
      IAxis xAxis = modelSpace.getAxisX();
      double viewableStartX = xAxis.getViewableStart();
      double viewableEndX = xAxis.getViewableEnd();
      double xRangeStart = xMin;
      double xRangeEnd = xMax;
      if (viewableStartX > viewableEndX) {
        xRangeStart = xMax;
        xRangeEnd = xMin;
      }

      double yMin = Math.min(yStart, yEnd);
      double yMax = Math.max(yStart, yEnd);
      IAxis yAxis = modelSpace.getAxisX();
      double viewableStartY = yAxis.getViewableStart();
      double viewableEndY = yAxis.getViewableEnd();
      double yRangeStart = yMin;
      double yRangeEnd = yMax;
      if (viewableStartY > viewableEndY) {
        yRangeStart = yMax;
        yRangeEnd = yMin;
      }

      modelSpace.setViewableBounds(xRangeStart, xRangeEnd, yRangeStart, yRangeEnd);
    }
    canvas.checkAspectRatio();
    canvas.update(UpdateLevel.RESIZE);

    canvas.setZoomRectangle(null);
  }
}
