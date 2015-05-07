/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.mapviewer.renderer.seismic;


import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.core.model.seismic.TraceSection;
import org.geocraft.ui.mapviewer.IMapViewer;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.action.IPlotMouseAction;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.layer.IPlotLayer;
import org.geocraft.ui.plot.layer.PlotLayer;
import org.geocraft.ui.plot.object.IPlotLine;
import org.geocraft.ui.plot.object.PlotLine;
import org.geocraft.ui.plot.object.PlotPoint;


/**
 * Initiates the construction of an xline trace section in a map viewer, based on
 * a 3D seismic geometry. As the user moves the mouse over the map view, a line
 * will track the movement, indicating the seismic xline that will be selected.
 * The user can "select" the xline by pressing mouse button 2.
 */
public class ConstructXline3dTraceSection extends Action implements MouseMoveListener {

  /** The map viewer in which to construct the xline trace section. */
  private final IMapViewer _viewer;

  /** The seismic geometry on which to construct the xline trace section. */
  private final SeismicSurvey3d _geometry;

  /** The plot shape representation of the trace section. */
  private IPlotLine _plotLine;

  public ConstructXline3dTraceSection(final IMapViewer viewer, final SeismicSurvey3d geometry) {
    super("Select Xline Section");
    setToolTipText("Define Xline Trace Section - Mouse motion to change xline, MB1 to select xline");
    //setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_POLYGON));
    _viewer = viewer;
    _geometry = geometry;
  }

  @Override
  public void run() {
    // Clear out the shape in the plot layer
    _plotLine = new PlotLine();
    _plotLine.clear();

    RGB lineColor = new RGB(0, 255, 0);
    _plotLine.setSelectable(true);
    _plotLine.setName("Trace section inline");
    _plotLine.setLineColor(lineColor);
    _plotLine.setPointColor(lineColor);
    _plotLine.setPointStyle(PointStyle.NONE);
    _plotLine.setPointSize(0);

    // Make sure the shape is selected
    _plotLine.select();

    IPlot plot = _viewer.getPlot();
    plot.getModelSpaceCanvas().setActiveShape(_plotLine);
    Cursor cursor = new Cursor(Display.getCurrent(), SWT.CURSOR_CROSS);

    // Set the mouse action for constructing an xline shape in the plot
    plot.setMouseActions(getMouseActions(_plotLine), cursor);
    plot.getModelSpaceCanvas().addMouseMoveListener(this);
    IPlotLayer layer = new PlotLayer("Trace Section Xline");
    layer.addShape(_plotLine);
    plot.addLayer(layer);
  }

  private IPlotMouseAction[] getMouseActions(final IPlotLine inline) {
    // Create an empty list of plot mouse actions
    List<IPlotMouseAction> actions = new ArrayList<IPlotMouseAction>();

    actions.add(new EndSeismic3dTraceSection(TraceSection.SectionType.XLINE_SECTION, _viewer, _geometry, inline));
    return actions.toArray(new IPlotMouseAction[0]);
  }

  public void mouseMove(final MouseEvent event) {
    // Determine the x,y coordinate of the mouse motion.
    Point2D.Double coord = new Point2D.Double();

    IPlot plot = _viewer.getPlot();
    plot.getModelSpaceCanvas().transformPixelToModel(plot.getActiveModelSpace(), event.x, event.y, coord);

    // Compute the inline,xline coordinate.
    float[] results = _geometry.transformXYToInlineXline(coord.x, coord.y, true);
    float xlineMin = Math.min(_geometry.getXlineStart(), _geometry.getXlineEnd());
    float xlineMax = Math.max(_geometry.getXlineStart(), _geometry.getXlineEnd());
    float xline = results[1];
    xline = Math.max(xline, xlineMin);
    xline = Math.min(xline, xlineMax);

    // Compute the x,y coordinate of at the current xline and starting inline of the geometry.
    double[] xyStart = _geometry.transformInlineXlineToXY(_geometry.getInlineStart(), xline);

    // Compute the x,y coordinate of at the current xline and ending inline of the geometry.
    double[] xyEnd = _geometry.transformInlineXlineToXY(_geometry.getInlineEnd(), xline);

    // Move the points of the display line.
    _plotLine.blockUpdate();
    _plotLine.setPoints(new PlotPoint(xyStart[0], xyStart[1], 0), new PlotPoint(xyEnd[0], xyEnd[1], 0));
    _plotLine.unblockUpdate();
    _plotLine.updated();
  }

}
