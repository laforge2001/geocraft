/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.action.shape;


import java.awt.geom.Point2D;

import org.geocraft.ui.plot.action.AbstractPlotMouseAction;
import org.geocraft.ui.plot.action.PlotActionMask;
import org.geocraft.ui.plot.action.PlotMouseEvent;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.object.IPlotMovableShape;
import org.geocraft.ui.plot.object.IPlotPoint;


public class EndShapeMotionAction extends AbstractPlotMouseAction {

  public EndShapeMotionAction(final PlotActionMask mask) {
    super(mask, "End Shape Motion", "End of motion for the selected shape.");
  }

  public void actionPerformed(final PlotMouseEvent event) {
    IModelSpaceCanvas canvas = event.getModelCanvas();
    if (canvas == null) {
      throw new RuntimeException("No model canvas.");
    }
    IPlotMovableShape shape = canvas.getShapeInMotion();
    IPlotPoint point = canvas.getPointInMotion();
    Point2D.Double modelCoord = event.getModelCoord();
    if (modelCoord != null) {
      if (shape != null && point != null) {
        shape.setInMotion(false);
        shape.motionEnd();
        point.setInMotion(false);
        point.motionEnd();
      }
    }
    canvas.setShapeInMotion(null);
    canvas.setPointInMotion(null);
  }

}
