/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.action.point;


import java.awt.geom.Point2D;
import java.util.List;

import org.eclipse.swt.graphics.Point;
import org.geocraft.ui.plot.action.AbstractPlotMouseAction;
import org.geocraft.ui.plot.action.PlotActionMask;
import org.geocraft.ui.plot.action.PlotMouseEvent;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotShape;


public class StartPointMotionAction extends AbstractPlotMouseAction {

  public StartPointMotionAction(final PlotActionMask mask) {
    super(mask, "Start Point Motion", "Start of motion for the selected point.");
  }

  public void actionPerformed(final PlotMouseEvent event) {
    IModelSpaceCanvas canvas = event.getModelCanvas();
    if (canvas == null) {
      throw new RuntimeException("No model canvas.");
    }
    Point pixelCoord = event.getPixelCoord();
    if (pixelCoord != null) {
      int px = pixelCoord.x;
      int py = pixelCoord.y;
      List<IPlotShape> shapes = canvas.getSelectedShapes();
      for (IPlotShape shape : shapes) {
        IPlotPoint point = canvas.getNearestSelectablePoint(shape, px, py);
        if (point != null) {
          Point2D.Double p = new Point2D.Double();
          canvas.transformModelToPixel(point.getModelSpace(), point.getX(), point.getY(), p);
          double dx = px - p.x;
          double dy = py - p.y;
          double dist = Math.sqrt(dx * dx + dy * dy);
          if (dist <= canvas.getSelectionTolerance()) {
            canvas.setPointInMotion(point);
            point.setInMotion(true);
            point.motionStart();
          }
        }
      }
    }
  }

}
