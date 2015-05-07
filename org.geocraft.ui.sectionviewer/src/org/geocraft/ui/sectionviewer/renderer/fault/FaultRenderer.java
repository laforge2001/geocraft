/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.renderer.fault;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.event.DataSelection;
import org.geocraft.core.model.fault.FaultInterpretation;
import org.geocraft.core.model.fault.TriangleDefinition;
import org.geocraft.core.model.fault.TriangulatedSurface;
import org.geocraft.core.model.seismic.TraceSection;
import org.geocraft.core.model.seismic.TraceSection.SectionType;
import org.geocraft.internal.ui.sectionviewer.SectionViewRendererUtil;
import org.geocraft.ui.model.ModelUI;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.PlotEventType;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.defs.TextAnchor;
import org.geocraft.ui.plot.event.ModelSpaceEvent;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPolyline;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.object.PlotPolyline;
import org.geocraft.ui.sectionviewer.SectionViewRenderer;
import org.geocraft.ui.sectionviewer.renderer.well.ProjectionPlane;
import org.geocraft.ui.sectionviewer.renderer.well.TriangleIntersection;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.ReadoutInfo;


public class FaultRenderer extends SectionViewRenderer implements ControlListener {

  /** The fault bore to render. */
  private FaultInterpretation _faultSurface;

  private TraceSection _currentSection;

  private final FaultRendererModel _model;

  /** The model space canvas in which to render the image. */
  protected IModelSpaceCanvas _canvas;

  protected boolean _needsRedraw = true;

  protected final List<IPlotPolyline> _surfaceTriangles;

  protected final List<IPlotPolyline> _pickSegments;

  public FaultRenderer() {
    super("Fault Surface Renderer");
    _model = new FaultRendererModel();
    _surfaceTriangles = Collections.synchronizedList(new ArrayList<IPlotPolyline>());
    _pickSegments = Collections.synchronizedList(new ArrayList<IPlotPolyline>());
    showReadoutInfo(true);

    IPreferenceStore preferences = FaultRendererPreferencePage.PREFERENCE_STORE;
    IPropertyChangeListener preferencesListener = new IPropertyChangeListener() {

      @Override
      public void propertyChange(final org.eclipse.jface.util.PropertyChangeEvent event) {
        _needsRedraw = true;
        redraw();
      }
    };
    preferences.addPropertyChangeListener(preferencesListener);
  }

  @Override
  public Object[] getRenderedObjects() {
    return new Object[] { _faultSurface };
  }

  @Override
  public void modelSpaceUpdated(final ModelSpaceEvent event) {
    PlotEventType eventType = event.getEventType();
    if (eventType.equals(PlotEventType.AXIS_UPDATED) || eventType.equals(PlotEventType.VIEWABLE_BOUNDS_UPDATED)) {
      redraw();
    }
  }

  @Override
  public void redraw() {
    redrawInternal();
  }

  protected void redrawInternal() {

    TraceSection section = getViewer().getTraceSection();
    if (section == null) {
      _currentSection = section;
      removeAllShapes(false);
      _surfaceTriangles.clear();
      _pickSegments.clear();
      super.updated();
      return;
    }

    boolean showTriangles = _model.getTrianglesVisible();
    LineStyle trianglesLineStyle = _model.getTrianglesLineStyle();
    int trianglesLineWidth = _model.getTrianglesLineWidth();
    RGB trianglesLineColor = _faultSurface.getDisplayColor();

    ProjectionPlane plane = new ProjectionPlane();

    int numTraces = section.getNumTraces();
    if (!section.equals(_currentSection) || _needsRedraw) {
      removeAllShapes(false);
      _surfaceTriangles.clear();
      _pickSegments.clear();
      Domain domain = section.getDomain();
      int numPanels = section.getNumPanels();
      int[] panelIndices = section.getPanelIndices();
      SectionType sectionType = section.getSectionType();
      boolean canRenderFault = false;
      if (sectionType.equals(SectionType.INLINE_SECTION) || sectionType.equals(SectionType.XLINE_SECTION)) {
        canRenderFault = true;
      } else if (sectionType.equals(SectionType.IRREGULAR)) {
        canRenderFault = true;
      }
      int[][] traceIndices = new int[numPanels][2];
      if (panelIndices.length == 0) {
        traceIndices[0][0] = 0;
        traceIndices[0][1] = section.getNumTraces() - 1;
      } else {
        traceIndices = new int[numPanels][2];
        traceIndices[0][0] = 0;
        int prevPanelIndex = 0;
        for (int traceIndex = 0; traceIndex < section.getNumTraces(); traceIndex++) {
          int currentPanelIndex = panelIndices[traceIndex];
          if (currentPanelIndex == prevPanelIndex) {
            traceIndices[currentPanelIndex][1] = traceIndex;
          } else {
            traceIndices[currentPanelIndex][0] = traceIndex;
          }
          prevPanelIndex = currentPanelIndex;
        }
        traceIndices[numPanels - 1][1] = section.getNumTraces() - 1;
      }
      float panelSize = 1;
      if (canRenderFault) {
        Point3d[] xyPoints = section.getPointsXY();
        for (int i = 0; i < numPanels; i++) {

          int index0 = traceIndices[i][0];
          int index1 = traceIndices[i][1];
          panelSize = index1 - index0;

          plane.x0 = xyPoints[index0].getX();
          plane.y0 = xyPoints[index0].getY();
          plane.z0 = section.getStartZ();
          plane.x1 = xyPoints[index1].getX();
          plane.y1 = xyPoints[index1].getY();
          plane.z1 = section.getEndZ();
          plane.dx = plane.x1 - plane.x0;
          plane.dy = plane.y1 - plane.y0;

          // Check each triangle of the fault surface, checking its intersection with the plane.
          if (showTriangles && _faultSurface.isTriangulated()) {
            TriangulatedSurface triSurface = _faultSurface.getTriangulatedSurface();
            if (triSurface == null) {
              break;
            }
            int numTriangles = _faultSurface.getNumTriangles();
            TriangleDefinition[] triangles = triSurface.getTriangles();
            Point3d[] vertices = triSurface.getVertices();
            Point3d[] points = new Point3d[3];
            for (int k = 0; k < numTriangles; k++) {
              points[0] = vertices[triangles[k].getVertex1() - 1];
              points[1] = vertices[triangles[k].getVertex2() - 1];
              points[2] = vertices[triangles[k].getVertex3() - 1];
              TriangleIntersection intersection = SectionViewRendererUtil.computeTrianglePlaneIntersection(points,
                  plane);
              if (intersection.exists()) {
                double x0 = 1 + index0 + intersection.getTracePercentages()[0] * panelSize;
                double x1 = 1 + index0 + intersection.getTracePercentages()[1] * panelSize;
                double y0 = intersection.getZValues()[0];
                double y1 = intersection.getZValues()[1];
                IPlotPolyline shape = new PlotPolyline();
                shape.setTextColor(trianglesLineColor);
                shape.setTextAnchor(TextAnchor.EAST);
                shape.setPointStyle(PointStyle.NONE);
                shape.setPointSize(0);
                shape.setLineColor(trianglesLineColor);
                shape.setLineStyle(trianglesLineStyle);
                shape.setLineWidth(trianglesLineWidth);
                IPlotPoint point1 = new PlotPoint(x0, y0, 0);
                IPlotPoint point2 = new PlotPoint(x1, y1, 0);
                point1.setPropertyInheritance(true);
                point2.setPropertyInheritance(true);
                shape.addPoint(point1);
                shape.addPoint(point2);
                _surfaceTriangles.add(shape);
              }
            }
          }
        }
      }
    }
    _currentSection = section;
    for (IPlotPolyline shape : _surfaceTriangles) {
      addShape(shape);
    }
    for (IPlotPolyline shape : _pickSegments) {
      addShape(shape);
    }

    _needsRedraw = false;

    super.updated();
  }

  @Override
  public void controlMoved(final ControlEvent event) {
    // No action.
  }

  @Override
  public void controlResized(final ControlEvent event) {
    redrawInternal();
  }

  @Override
  protected void addPlotShapes() {
    for (IPlotPolyline shape : _surfaceTriangles) {
      addShape(shape);
    }
    for (IPlotPolyline shape : _pickSegments) {
      addShape(shape);
    }
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree(_faultSurface, _faultSurface.getDisplayName(), IViewer.FAULT_FOLDER, autoUpdate);
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    // Store a reference to the fault surface.
    _faultSurface = (FaultInterpretation) objects[0];
    _faultSurface.load();
    _canvas = getViewer().getModelSpaceCanvas();
  }

  @Override
  protected void setNameAndImage() {
    setName(_faultSurface);
    setImage(ModelUI.getSharedImages().getImage(_faultSurface));
  }

  @Override
  public DataSelection getDataSelection(final double x, final double y) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ReadoutInfo getReadoutInfo(final int trace, final float z) {
    return new ReadoutInfo("" + getClass());
  }

  @Override
  protected void addPopupMenuActions() {
    Dialog dialog = new FaultRendererDialog(getShell(), _faultSurface.getDisplayName(), this);
    addSettingsPopupMenuAction(dialog, SWT.DEFAULT, SWT.DEFAULT);
  }

  public FaultRendererModel getSettingsModel() {
    return _model;
  }

  /**
   * Updates the display settings (color) of the points.
   * This will trigger the renderer to redraw.
   */
  public void updateSettings(final FaultRendererModel model) {
    _model.updateFrom(model);
    _needsRedraw = true;
    redrawInternal();
  }
}
