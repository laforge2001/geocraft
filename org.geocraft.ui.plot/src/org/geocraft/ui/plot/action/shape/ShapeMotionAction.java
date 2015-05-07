/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.action.shape;


import java.awt.geom.Point2D;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.geocraft.ui.plot.action.AbstractPlotMouseAction;
import org.geocraft.ui.plot.action.PlotActionMask;
import org.geocraft.ui.plot.action.PlotMouseEvent;
import org.geocraft.ui.plot.defs.UpdateLevel;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.object.IPlotMovableShape;
import org.geocraft.ui.plot.object.IPlotPoint;


public class ShapeMotionAction extends AbstractPlotMouseAction {

  public ShapeMotionAction(final PlotActionMask mask) {
    super(mask, "Shape Motion", "Motion for the selected shape.");
  }

  public void actionPerformed(final PlotMouseEvent event) {
    IModelSpaceCanvas canvas = event.getModelCanvas();
    if (canvas == null) {
      throw new RuntimeException("No model canvas.");
    }
    IPlotMovableShape shape = canvas.getShapeInMotion();
    IPlotPoint point = canvas.getPointInMotion();
    Point pixelCoord = event.getPixelCoord();
    if (shape != null && point != null && pixelCoord != null) {
      Point2D.Double modelCoord = new Point2D.Double();
      canvas.transformPixelToModel(shape.getModelSpace(), pixelCoord.x, pixelCoord.y, modelCoord);

      double dx = modelCoord.getX() - point.getX();
      double dy = modelCoord.getY() - point.getY();

      Rectangle mask0 = shape.getRectangle(canvas);
      int xmin0 = mask0.x;
      int xmax0 = mask0.x + mask0.width - 1;
      int ymin0 = mask0.y;
      int ymax0 = mask0.y + mask0.height - 1;

      shape.blockUpdate();
      shape.moveBy(dx, dy);
      shape.unblockUpdate();

      Rectangle mask1 = shape.getRectangle(canvas);
      int xmin1 = mask1.x;
      int xmax1 = mask1.x + mask1.width - 1;
      int ymin1 = mask1.y;
      int ymax1 = mask1.y + mask1.height - 1;
      int xmin = Math.min(xmin0, xmin1);
      int xmax = Math.max(xmax0, xmax1);
      int ymin = Math.min(ymin0, ymin1);
      int ymax = Math.max(ymax0, ymax1);
      int width = xmax - xmin + 1;
      int height = ymax - ymin + 1;
      Rectangle mask = new Rectangle(xmin, ymin, width, height);

      shape.motion();

      canvas.update(UpdateLevel.REFRESH, mask);
    }
  }

}
