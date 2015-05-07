/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.mapviewer.renderer.seismic;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.core.model.seismic.TraceSection;
import org.geocraft.ui.mapviewer.IMapViewer;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.action.IPlotMouseAction;
import org.geocraft.ui.plot.action.PlotActionMask;
import org.geocraft.ui.plot.action.point.AddPointAction;
import org.geocraft.ui.plot.action.point.DeletePointAction;
import org.geocraft.ui.plot.defs.ActionMaskType;
import org.geocraft.ui.plot.defs.PointInsertionMode;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.layer.IPlotLayer;
import org.geocraft.ui.plot.layer.PlotLayer;
import org.geocraft.ui.plot.object.IPlotPolyline;
import org.geocraft.ui.plot.object.PlotPolyline;


/**
 * Initiates the construction of an arbitrary trace section in a map viewer, based on
 * a 3D seismic geometry. The user move the mouse over the map view and adds points to
 * the section by pressing mouse button 1. By pressing mouse button 2, the user adds
 * the current point and "selects" the section.
 */
public class ConstructArbitrary3dTraceSection extends Action {

  /** The map viewer in which to construct the arbitrary trace section. */
  private final IMapViewer _viewer;

  /** The seismic geometry on which to construct the arbitrary trace section. */
  private final SeismicSurvey3d _survey;

  public ConstructArbitrary3dTraceSection(final IMapViewer viewer, final SeismicSurvey3d survey) {
    super("Select Arbitrary Section");
    setToolTipText("Define Arbitrary Trace Section - MB1 to add point, SHIFT + MB2 to delete existing point, MB2 to end editing and save trace section");
    //setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_POLYGON));
    _viewer = viewer;
    _survey = survey;
  }

  @Override
  public void run() {
    // Clear out the shape in the plot layer
    IPlotPolyline polyline = new PlotPolyline();
    polyline.clear();

    RGB lineColor = Display.getCurrent().getSystemColor(SWT.COLOR_GREEN).getRGB();
    RGB pointColor = new RGB(0, 0, 255);
    polyline.setSelectable(true);
    polyline.setName("Trace section polyline");
    polyline.setLineColor(lineColor);
    polyline.setPointColor(pointColor);
    polyline.setPointStyle(PointStyle.FILLED_SQUARE);
    polyline.setPointSize(4);

    // Make sure the shape is selected
    polyline.select();
    IPlot plot = _viewer.getPlot();
    plot.getModelSpaceCanvas().setActiveShape(polyline);
    Cursor cursor = new Cursor(Display.getCurrent(), SWT.CURSOR_CROSS);

    // Set the mouse action for constructing a polyline shape in the plot
    plot.setMouseActions(getMouseActions(polyline), cursor);
    IPlotLayer layer = new PlotLayer("Trace Section Polyline");
    layer.addShape(polyline);
    polyline.rubberbandOn();
    plot.addLayer(layer);
  }

  private IPlotMouseAction[] getMouseActions(final IPlotPolyline polyline) {
    // Create an empty list of plot mouse actions
    List<IPlotMouseAction> actions = new ArrayList<IPlotMouseAction>();
    PlotActionMask mask;

    // Point add: mouse button 1 down
    mask = new PlotActionMask(ActionMaskType.MOUSE_DOWN, 1, 1, 0);
    actions.add(new AddPointAction(mask, PointInsertionMode.LAST));
    // Point delete: mouse button 2 down with shift
    mask = new PlotActionMask(ActionMaskType.MOUSE_DOWN, 2, 1, SWT.SHIFT);
    actions.add(new DeletePointAction(mask));
    actions.add(new EndSeismic3dTraceSection(TraceSection.SectionType.IRREGULAR, _viewer, _survey, polyline));
    return actions.toArray(new IPlotMouseAction[0]);
  }
}
