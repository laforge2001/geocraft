/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.action.shape;


import java.awt.geom.Point2D;
import java.util.List;

import org.geocraft.ui.plot.action.AbstractPlotMouseAction;
import org.geocraft.ui.plot.action.PlotActionMask;
import org.geocraft.ui.plot.action.PlotMouseEvent;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.object.IPlotShape;


public class DeselectShapeAction extends AbstractPlotMouseAction {

  public DeselectShapeAction(final PlotActionMask mask) {
    super(mask, "Deselect Shape", "Deselects the selected shape.");
  }

  public void actionPerformed(final PlotMouseEvent event) {
    IModelSpaceCanvas canvas = event.getModelCanvas();
    if (canvas == null) {
      throw new RuntimeException("No model canvas.");
    }
    Point2D.Double modelCoord = event.getModelCoord();
    if (modelCoord != null) {
      List<IPlotShape> shapes = canvas.getSelectedShapes();
      for (IPlotShape shape : shapes) {
        shape.deselect();
      }
    }
  }

}
