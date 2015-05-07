/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.mapviewer.renderer.seismic;


import org.eclipse.swt.SWT;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.TraceSectionSelection;
import org.geocraft.core.model.seismic.SeismicSurvey2d;
import org.geocraft.core.model.seismic.TraceSection.SectionType;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.message.Topic;
import org.geocraft.ui.mapviewer.IMapViewer;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.action.AbstractPlotMouseAction;
import org.geocraft.ui.plot.action.PlotActionMask;
import org.geocraft.ui.plot.action.PlotMouseActionList;
import org.geocraft.ui.plot.action.PlotMouseEvent;
import org.geocraft.ui.plot.defs.ActionMaskType;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotShape;


/**
 * Defines the plot mouse action to invoke when the user "selects" a trace
 * section from the map view renderer of a 2D seismic survey. This action
 * will reset the default map viewer mouse actions and then prompt the user
 * to name the defined trace section. Once named, this action will create a
 * list of all the x,y points in the section and publish them on the message
 * bus for any subscribers of the <code>TRACE_SECTION_SELECTED</code> topic.
 */
public class EndSeismic2dTraceSection extends AbstractPlotMouseAction {

  /** The map viewer in which the trace section was defined. */
  private final IMapViewer _viewer;

  /** The seismic survey on which the trace section was defined. */
  private final SeismicSurvey2d _survey;

  /** The plot shape representation of the trace section. */
  private final IPlotShape _shape;

  private static int _counter;

  public EndSeismic2dTraceSection(final IMapViewer viewer, final SeismicSurvey2d survey, final IPlotShape line) {
    super(new PlotActionMask(ActionMaskType.MOUSE_DOWN, 2, 1, 0), "Trace Section End",
        "End the trace section definition.");
    _viewer = viewer;
    _survey = survey;
    _shape = line;
  }

  public void actionPerformed(final PlotMouseEvent event) {
    // Compute the line,xline coordinate.
    IPlotPoint point0 = _shape.getFirstPoint();
    float[] results = _survey.transformXYToLineCDP(point0.getX(), point0.getY());
    int lineNumber = Math.round(results[0]);

    _shape.deselect();

    IPlot plot = _viewer.getPlot();
    plot.setMouseActions(PlotMouseActionList.getDefaultObjectActions().getActions(), SWT.CURSOR_ARROW);

    _counter++;

    plot.getActiveModelSpace().removeLayer(_shape.getLayer());

    // Create a list of all the control points.
    int numPoints = _shape.getPointCount();
    Point3d[] points = new Point3d[numPoints];
    for (int i = 0; i < numPoints; i++) {
      IPlotPoint point = _shape.getPoint(i);
      points[i] = new Point3d(point.getX(), point.getY(), point.getZ());
    }

    // Publish the trace section selection on the message bus.
    TraceSectionSelection selection = new TraceSectionSelection(SectionType.INLINE_SECTION, _survey, lineNumber);
    ServiceProvider.getMessageService().publish(Topic.TRACE_SECTION_SELECTED, selection);
  }
}
