/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.action.zoom;


import java.awt.geom.Point2D;

import org.geocraft.ui.plot.action.AbstractPlotMouseAction;
import org.geocraft.ui.plot.action.PlotActionMask;
import org.geocraft.ui.plot.action.PlotMouseEvent;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.object.IPlotPolygon;


public class ZoomMotionAction extends AbstractPlotMouseAction {

  /**
   * Constructs an instance of ZoomMotionAction.
   * @param mask the action mask (mouse buttons, clicks, modifiers).
   */
  public ZoomMotionAction(final PlotActionMask mask) {
    super(mask, "Zoom Window", "Update the zoom window cursor definition.");
  }

  public void actionPerformed(final PlotMouseEvent event) {
    IModelSpaceCanvas canvas = event.getModelCanvas();
    IPlotPolygon zoomRect = canvas.getZoomRectangle();
    if (zoomRect == null) {
      return;
    }
    Point2D.Double modelCoord = event.getModelCoord();
    if (modelCoord == null) {
      return;
    }
    double x = modelCoord.getX();
    double y = modelCoord.getY();
    zoomRect.blockUpdate();
    zoomRect.getPoint(1).setX(x);
    zoomRect.getPoint(2).setXY(x, y);
    zoomRect.getPoint(3).setY(y);
    zoomRect.unblockUpdate();
    zoomRect.updated();
  }

}
