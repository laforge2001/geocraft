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
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.seismic.SeismicLine2d;
import org.geocraft.core.model.seismic.SeismicSurvey2d;
import org.geocraft.ui.mapviewer.IMapViewer;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.action.IPlotMouseAction;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.layer.IPlotLayer;
import org.geocraft.ui.plot.layer.PlotLayer;
import org.geocraft.ui.plot.object.IPlotPolyline;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.object.PlotPolyline;


/**
 * Initiates the construction of an line trace section in a map viewer, based on
 * a 2D seismic geometry. As the user moves the mouse over the map view, a line
 * will track the movement, indicating the seismic line that will be selected.
 * The user can "select" the line by pressing mouse button 2.
 */
public class ConstructLine2dTraceSection extends Action implements MouseMoveListener {

  /** The map viewer in which to construct the line trace section. */
  private final IMapViewer _viewer;

  /** The seismic geometry on which to construct the line trace section. */
  private final SeismicSurvey2d _survey;

  /** The plot shape representation of the trace section. */
  private IPlotPolyline _selectionShape;

  public ConstructLine2dTraceSection(final IMapViewer viewer, final SeismicSurvey2d survey) {
    super("Select Line Section");
    setToolTipText("Define Line Trace Section - Mouse motion to change line, MB1 to select line");
    //setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_POLYGON));
    _viewer = viewer;
    _survey = survey;
  }

  @Override
  public void run() {
    // Clear out the shape in the plot layer
    _selectionShape = new PlotPolyline();
    _selectionShape.clear();

    RGB lineColor = new RGB(0, 255, 0);
    _selectionShape.setSelectable(true);
    _selectionShape.setName("Trace section line");
    _selectionShape.setLineColor(lineColor);
    _selectionShape.setPointColor(lineColor);
    _selectionShape.setPointStyle(PointStyle.NONE);
    _selectionShape.setPointSize(0);

    // Make sure the shape is selected
    _selectionShape.select();

    IPlot plot = _viewer.getPlot();
    plot.getModelSpaceCanvas().setActiveShape(_selectionShape);
    Cursor cursor = new Cursor(Display.getCurrent(), SWT.CURSOR_CROSS);

    // Set the mouse action for constructing an line shape in the plot
    plot.setMouseActions(getMouseActions(_selectionShape), cursor);
    plot.getModelSpaceCanvas().addMouseMoveListener(this);
    IPlotLayer layer = new PlotLayer("Trace Section Line");
    layer.addShape(_selectionShape);
    plot.addLayer(layer);
  }

  private IPlotMouseAction[] getMouseActions(final IPlotPolyline shape) {
    // Create an empty list of plot mouse actions
    List<IPlotMouseAction> actions = new ArrayList<IPlotMouseAction>();

    actions.add(new EndSeismic2dTraceSection(_viewer, _survey, shape));
    return actions.toArray(new IPlotMouseAction[0]);
  }

  public void mouseMove(final MouseEvent event) {
    // Determine the x,y coordinate of the mouse motion.
    Point2D.Double coord = new Point2D.Double();

    IPlot plot = _viewer.getPlot();
    plot.getModelSpaceCanvas().transformPixelToModel(plot.getActiveModelSpace(), event.x, event.y, coord);

    // Compute the line,xline coordinate.
    float[] results = _survey.transformXYToLineCDP(coord.x, coord.y);
    int lineNumber = Math.round(results[0]);
    SeismicLine2d seismicLine = _survey.getLineByNumber(lineNumber);

    // Move the points of the display line.
    _selectionShape.blockUpdate();
    _selectionShape.clear();
    for (Point3d point : seismicLine.getPoints().getPointsDirect()) {
      _selectionShape.addPoint(new PlotPoint(point.getX(), point.getY(), 0));
    }
    _selectionShape.unblockUpdate();
    _selectionShape.updated();
  }

}
