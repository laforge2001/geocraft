/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.action.zoom;


import java.awt.geom.Point2D;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.action.AbstractPlotMouseAction;
import org.geocraft.ui.plot.action.PlotActionMask;
import org.geocraft.ui.plot.action.PlotMouseEvent;
import org.geocraft.ui.plot.axis.IAxis;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;


public class PanAction extends AbstractPlotMouseAction {

  protected static int _xOld;

  protected static int _yOld;

  public PanAction(final PlotActionMask mask) {
    super(mask, "Pan", "Pans the plot using the mouse.");
  }

  @Override
  public void actionPerformed(final PlotMouseEvent event) {
    IPlot plot = event.getPlot();
    MouseEvent mouseEvent = event.getMouseEvent();
    if ((mouseEvent.stateMask & SWT.BUTTON_MASK) == 0 && mouseEvent.button == 0) {
      _xOld = mouseEvent.x;
      _yOld = mouseEvent.y;
      return;
    }
    int xNew = mouseEvent.x;
    int yNew = mouseEvent.y;
    IModelSpace model = plot.getActiveModelSpace();
    IModelSpaceCanvas canvas = plot.getModelSpaceCanvas();
    Point2D.Double coordNew = new Point2D.Double(0, 0);
    canvas.transformPixelToModel(model, xNew, yNew, coordNew);
    Point2D.Double coordOld = new Point2D.Double(0, 0);
    canvas.transformPixelToModel(model, _xOld, _yOld, coordOld);
    double deltaX = coordNew.getX() - coordOld.getX();
    double deltaY = coordNew.getY() - coordOld.getY();
    IAxis xAxis = model.getAxisX();
    IAxis yAxis = model.getAxisY();
    double xStart = xAxis.getViewableStart();
    double xEnd = xAxis.getViewableEnd();
    double yStart = yAxis.getViewableStart();
    double yEnd = yAxis.getViewableEnd();
    xStart -= deltaX;
    xEnd -= deltaX;
    yStart -= deltaY;
    yEnd -= deltaY;
    _xOld = xNew;
    _yOld = yNew;
    model.setViewableBounds(xStart, xEnd, yStart, yEnd);
    if (mouseEvent.button == 0) {
      plot.updateAll();
    } else {
      model.updated();
    }
  }
}
