/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.action.zoom;


import java.awt.geom.Point2D;

import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.action.AbstractPlotMouseAction;
import org.geocraft.ui.plot.action.PlotActionMask;
import org.geocraft.ui.plot.action.PlotMouseEvent;
import org.geocraft.ui.plot.axis.IAxis;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;


public class ZoomAction extends AbstractPlotMouseAction {

  protected double _zoom = 1;

  /**
   * Constructs an instance of ZoomAction.
   * @param mask the action mask(mouse buttons, clicks, modifiers).
   * @param zoom the zoom factor.
   * @param name the action name.
   * @param description the action description.
   */
  public ZoomAction(final PlotActionMask mask, final String name, final String description, final double zoom) {
    super(mask, name, description);
    _zoom = zoom;
  }

  public void actionPerformed(final PlotMouseEvent event) {
    IPlot plot = event.getPlot();
    IModelSpaceCanvas canvas = event.getModelCanvas();
    if (canvas == null) {
      throw new RuntimeException("No model canvas.");
    }
    int px = event.getPixelCoord().x;
    int py = event.getPixelCoord().y;
    for (IModelSpace modelSpace : event.getPlot().getModelSpaces()) {
      Point2D.Double modelCoord = new Point2D.Double(0, 0);
      canvas.transformPixelToModel(modelSpace, px, py, modelCoord);
      double x = modelCoord.getX();
      double y = modelCoord.getY();
      IAxis xAxis = modelSpace.getAxisX();
      IAxis yAxis = modelSpace.getAxisY();
      double xStart = xAxis.getViewableStart();
      double xEnd = xAxis.getViewableEnd();
      double yStart = yAxis.getViewableStart();
      double yEnd = yAxis.getViewableEnd();
      double dx = (xEnd - xStart) / _zoom;
      double dy = (yEnd - yStart) / _zoom;
      modelSpace.setViewableBounds(x - dx / 2, x + dx / 2, y - dy / 2, y + dy / 2);
      //      double[] xLabels1 = Labels.computeLabels(xStart, xEnd, 10);
      //      double[] yLabels1 = Labels.computeLabels(yStart, yEnd, 10);
      //      xAxis.setStep(Level.Primary, xLabels1[2]);
      //      yAxis.setStep(Level.Primary, yLabels1[2]);
      //      xAxis.setStep(Level.Secondary, xLabels1[2] / 2);
      //      yAxis.setStep(Level.Secondary, yLabels1[2] / 2);
      plot.updateAll();
    }
  }
}
