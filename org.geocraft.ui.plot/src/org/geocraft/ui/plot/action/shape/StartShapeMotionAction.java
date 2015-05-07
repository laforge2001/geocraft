/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.action.shape;


import java.awt.geom.Point2D;

import org.eclipse.swt.graphics.Point;
import org.geocraft.ui.plot.action.AbstractPlotMouseAction;
import org.geocraft.ui.plot.action.PlotActionMask;
import org.geocraft.ui.plot.action.PlotMouseEvent;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.object.IPlotMovableShape;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotShape;


public class StartShapeMotionAction extends AbstractPlotMouseAction {

  public StartShapeMotionAction(final PlotActionMask mask) {
    super(mask, "Start Shape Motion", "Start of motion for the selected shape.");
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
      IPlotPoint point = canvas.getNearestSelectablePoint(px, py);
      if (point != null) {
        IPlotShape shape = point.getShape();
        if (shape != null && shape.isSelectable() && shape.isMovable()) {
          Point2D.Double p = new Point2D.Double();
          canvas.transformModelToPixel(point.getModelSpace(), point.getX(), point.getY(), p);
          double dx = px - p.x;
          double dy = py - p.y;
          double dist = Math.sqrt(dx * dx + dy * dy);
          if (dist <= canvas.getSelectionTolerance()) {
            if (!shape.isSelected()) {
              shape.select();
            }
            canvas.setShapeInMotion((IPlotMovableShape) shape);
            canvas.setPointInMotion(point);
            shape.setInMotion(true);
            shape.motionStart();
            point.motionStart();
          }
        }
      }
    }
  }

}
