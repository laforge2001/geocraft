/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.action.point;


import java.awt.geom.Point2D;

import org.geocraft.ui.plot.action.AbstractPlotMouseAction;
import org.geocraft.ui.plot.action.PlotActionMask;
import org.geocraft.ui.plot.action.PlotMouseEvent;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.object.IPlotPoint;


public class EndPointMotionAction extends AbstractPlotMouseAction {

  public EndPointMotionAction(final PlotActionMask mask) {
    super(mask, "End Point Motion", "End of motion for the selected point.");
  }

  public void actionPerformed(final PlotMouseEvent event) {
    IModelSpaceCanvas canvas = event.getModelCanvas();
    if (canvas == null) {
      throw new RuntimeException("No model canvas.");
    }
    Point2D.Double modelCoord = event.getModelCoord();
    if (modelCoord != null) {
      IPlotPoint point = canvas.getPointInMotion();
      if (point != null) {
        point.setInMotion(false);
        point.motionEnd();
      }
    }
    canvas.setPointInMotion(null);
  }

}
