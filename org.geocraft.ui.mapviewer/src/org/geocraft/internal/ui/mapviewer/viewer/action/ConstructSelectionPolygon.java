/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.ui.mapviewer.viewer.action;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;
import org.geocraft.ui.mapviewer.IMapViewer;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.action.IPlotMouseAction;
import org.geocraft.ui.plot.action.PlotActionMask;
import org.geocraft.ui.plot.action.point.AddPointAction;
import org.geocraft.ui.plot.action.point.DeletePointAction;
import org.geocraft.ui.plot.defs.ActionMaskType;
import org.geocraft.ui.plot.defs.FillStyle;
import org.geocraft.ui.plot.defs.PointInsertionMode;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.layer.IPlotLayer;
import org.geocraft.ui.plot.layer.PlotLayer;
import org.geocraft.ui.plot.object.IPlotPolygon;
import org.geocraft.ui.plot.object.PlotPolygon;


/**
 * This action initiates the construction of a selection polygon in the plot.
 * Points are added to the selection polygon using MB1. Points are removed from
 * the selection polygon using SHIFT+MB2. To end the polygon construction, use MB2
 * without the SHIFT button.
 */
public class ConstructSelectionPolygon extends Action {

  /** The map viewer in which to construct the selection polygon. */
  private final IMapViewer _viewer;

  public ConstructSelectionPolygon(final IMapViewer viewer) {
    super();
    setToolTipText("Define AOI - MB1 to add point, SHIFT + MB2 to delete existing point, MB2 to end polygon editing and save AOI");
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_POLYGON));
    _viewer = viewer;
  }

  @Override
  public void run() {
    // Clear out the shape in the plot layer
    IPlotPolygon polygon = new PlotPolygon();
    polygon.clear();

    RGB lineColor = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY).getRGB();
    RGB fillColor = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY).getRGB();
    polygon.setSelectable(true);
    polygon.setName("AOI polygon");
    polygon.setLineColor(lineColor);
    polygon.setFillColor(fillColor);
    polygon.setFillStyle(FillStyle.NONE);
    polygon.setPointColor(new RGB(0, 0, 255));
    polygon.setPointStyle(PointStyle.FILLED_SQUARE);
    polygon.setPointSize(4);

    // Make sure the shape is selected
    polygon.select();

    IPlot plot = _viewer.getPlot();
    plot.getModelSpaceCanvas().setActiveShape(polygon);
    Cursor cursor = new Cursor(Display.getCurrent(), SWT.CURSOR_CROSS);

    // Set the mouse action for constructing a polygon shape in the plot
    plot.setMouseActions(getPolygonMouseActions(polygon), cursor);
    IPlotLayer layer = new PlotLayer("AOI Polygon");
    layer.addShape(polygon);
    polygon.rubberbandOn();
    plot.addLayer(layer);
  }

  private IPlotMouseAction[] getPolygonMouseActions(final IPlotPolygon polygon) {
    // Create an empty list of plot mouse actions
    List<IPlotMouseAction> actions = new ArrayList<IPlotMouseAction>();
    PlotActionMask mask;

    // Point add: mouse button 1 down
    mask = new PlotActionMask(ActionMaskType.MOUSE_DOWN, 1, 1, 0);
    actions.add(new AddPointAction(mask, PointInsertionMode.LAST));
    // Point delete: mouse button 2 down with shift
    mask = new PlotActionMask(ActionMaskType.MOUSE_DOWN, 2, 1, SWT.SHIFT);
    actions.add(new DeletePointAction(mask));
    actions.add(new EndPolygonDefinition(_viewer, polygon));
    return actions.toArray(new IPlotMouseAction[0]);
  }
}
