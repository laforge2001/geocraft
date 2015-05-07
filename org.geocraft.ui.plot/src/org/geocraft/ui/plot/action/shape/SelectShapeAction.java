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
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotShape;


public class SelectShapeAction extends AbstractPlotMouseAction {

  private final boolean _allowPointAddDelete;

  public SelectShapeAction(final PlotActionMask mask, final boolean allowPointAddDelete) {
    super(mask, "Select Shape", "Selects the nearest shape (within a tolerance).");
    _allowPointAddDelete = allowPointAddDelete;
  }

  public void actionPerformed(final PlotMouseEvent event) {
    IModelSpaceCanvas canvas = event.getModelCanvas();
    if (canvas == null) {
      throw new RuntimeException("No model canvas.");
    }
    Point pixelCoord = event.getPixelCoord();
    Point2D.Double modelCoord = event.getModelCoord();
    if (modelCoord != null) {
      int px = pixelCoord.x;
      int py = pixelCoord.y;
      IPlotPoint point = canvas.getNearestSelectablePoint(px, py);
      if (point != null) {
        Point2D.Double p = new Point2D.Double();
        canvas.transformModelToPixel(point.getModelSpace(), point.getX(), point.getY(), p);
        double dx = px - p.x;
        double dy = py - p.y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist <= canvas.getSelectionTolerance()) {
          IPlotShape shape = point.getShape();
          shape.select();
          if (_allowPointAddDelete) {
            canvas.setActiveShape(shape);
          }
        }
      }
    }
  }

}
