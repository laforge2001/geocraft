/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot.action;


import org.eclipse.jface.action.Action;
import org.eclipse.swt.graphics.Cursor;
import org.geocraft.abavo.crossplot.IABavoCrossplot;
import org.geocraft.abavo.crossplot.layer.PolygonLayer;
import org.geocraft.abavo.polygon.PolygonRegionsModel;
import org.geocraft.internal.abavo.ABavoCursor;
import org.geocraft.ui.plot.object.IPlotPolygon;


/**
 * This action initiates the construction of a selection polygon in the crossplot.
 * Points are added to the selection polygon using MB1. Points are removed from
 * the selection polygon using SHIFT+MB2. To end the polygon construction, use MB2
 * without the SHIFT button. Any points located inside the selection polygon will
 * be broadcast, but only if the broadcast button is toggled on in the crossplot view.
 */
public class ConstructSelectionPolygon extends Action {

  /** The crossplot in which to construct the selection polygon. */
  private final IABavoCrossplot _crossplot;

  /** The crossplot layer containing the polygon shape. */
  private final PolygonLayer _layer;

  public ConstructSelectionPolygon(final IABavoCrossplot crossplot, final PolygonLayer layer) {
    super("Construct the selection polygon");
    setToolTipText("Construct the selection polygon");
    _crossplot = crossplot;
    _layer = layer;
  }

  @Override
  public void run() {
    // Clear out the shape in the crossplot layer.
    IPlotPolygon polygon = _layer.getPolygon();
    polygon.clear();
    polygon.rubberbandOn();

    // Make sure the shape is selected.
    polygon.select();
    _crossplot.getModelSpaceCanvas().setActiveShape(polygon);

    // Update the cursor to a pencil.
    Cursor cursor = ABavoCursor.getPencil();

    // Set the mouse action for constructing a polygon shape in the crossplot.
    _crossplot.setMouseActions(PolygonRegionsModel.getPolygonMouseActions(_crossplot, -1, false), cursor);
  }
}
