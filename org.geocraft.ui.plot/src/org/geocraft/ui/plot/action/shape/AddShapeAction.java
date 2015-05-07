/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.action.shape;


import java.awt.geom.Point2D;

import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.action.AbstractPlotMouseAction;
import org.geocraft.ui.plot.action.PlotActionMask;
import org.geocraft.ui.plot.action.PlotMouseEvent;
import org.geocraft.ui.plot.defs.ShapeType;
import org.geocraft.ui.plot.layer.IPlotLayer;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.object.IPlotPointGroup;
import org.geocraft.ui.plot.object.IPlotShape;
import org.geocraft.ui.plot.object.PlotLine;
import org.geocraft.ui.plot.object.PlotPointGroup;
import org.geocraft.ui.plot.object.PlotPolygon;
import org.geocraft.ui.plot.object.PlotPolyline;
import org.geocraft.ui.plot.object.PlotRectangle;


/**
 * Ths class defines the mouse action for adding a shape
 * to the plot. 
 */
public class AddShapeAction extends AbstractPlotMouseAction {

  /** The type of shape to add. */
  protected ShapeType _shapeType;

  /** Flag indicating if the shape should be selected upon addition. */
  protected boolean _autoSelect;

  public AddShapeAction(final PlotActionMask mask, final ShapeType shapeType, final boolean autoSelect) {
    super(mask, "Add Shape", "Add a shape to the active layer.");
    _shapeType = shapeType;
    _autoSelect = autoSelect;
  }

  public void actionPerformed(final PlotMouseEvent event) {
    IModelSpaceCanvas canvas = event.getModelCanvas();
    if (canvas == null) {
      throw new RuntimeException("No model canvas.");
    }
    IPlot plot = event.getPlot();

    // Check that the active model space is not null.
    IModelSpace modelSpace = plot.getActiveModelSpace();
    if (modelSpace == null) {
      return;
    }

    // Check that the active layer is not null.
    IPlotLayer layer = modelSpace.getActiveLayer();
    if (layer == null) {
      return;
    }

    // Check that the model space coordinates are not null.
    Point2D.Double modelCoord = event.getModelCoord();
    if (modelCoord != null) {
      IPlotShape shape = null;
      // Create a new shape of the desired type.
      if (_shapeType.equals(ShapeType.POINT_GROUP)) {
        shape = new PlotPointGroup();
      } else if (_shapeType.equals(ShapeType.POLYLINE)) {
        shape = new PlotPolyline();
      } else if (_shapeType.equals(ShapeType.POLYGON)) {
        shape = new PlotPolygon();
      } else if (_shapeType.equals(ShapeType.LINE)) {
        shape = new PlotLine();
      } else if (_shapeType.equals(ShapeType.RECTANGLE)) {
        shape = new PlotRectangle();
      } else {
        throw new RuntimeException("Invalid shape type: " + _shapeType);
      }
      // Add the shape to the active layer.
      layer.addShape(shape);
      // If desired, select the shape.
      if (_autoSelect && shape != null) {
        shape.select();
        if (shape instanceof IPlotPointGroup && canvas.getRubberband()) {
          ((IPlotPointGroup) shape).rubberbandOn();
        }
      }
    }
  }
}
