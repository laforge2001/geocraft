package org.geocraft.ui.mapviewer.renderer.fault;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.PolylinePick;
import org.geocraft.core.model.event.DataSelection;
import org.geocraft.core.model.fault.FaultInterpretation;
import org.geocraft.core.model.fault.TriangleDefinition;
import org.geocraft.ui.mapviewer.MapViewRenderer;
import org.geocraft.ui.model.ModelUI;
import org.geocraft.ui.plot.defs.FillStyle;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPolygon;
import org.geocraft.ui.plot.object.IPlotPolyline;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.object.PlotPolygon;
import org.geocraft.ui.plot.object.PlotPolyline;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.ReadoutInfo;


public class FaultRenderer extends MapViewRenderer {

  /** The fault interpretation to render. */
  private FaultInterpretation _fault;

  /** The model of display settings. */
  private final FaultRendererModel _model;

  private final List<IPlotPolygon> _triangleShapes;

  private final List<IPlotPolyline> _segmentShapes;

  public FaultRenderer() {
    super("Fault Interpretation Renderer");
    _triangleShapes = new ArrayList<IPlotPolygon>();
    _segmentShapes = new ArrayList<IPlotPolyline>();
    _model = new FaultRendererModel();
  }

  @Override
  protected void addPlotShapes() {
    RGB displayColor = _fault.getDisplayColor();

    PolylinePick[] segments = _fault.getSegments();
    for (PolylinePick segment : segments) {
      IPlotPolyline segmentShape = new PlotPolyline();
      segmentShape.setName(segment.getDisplayName());
      int numPoints = segment.getNumPoints();
      Point3d[] points = segment.getPoints();

      // Loop thru the points in the segment, adding them to the plot shape.
      for (int j = 0; j < numPoints; j++) {
        double x = points[j].getX();
        double y = points[j].getY();
        double z = points[j].getZ();
        IPlotPoint point = new PlotPoint("", x, y, z);
        point.setPropertyInheritance(false);
        point.setPointSize(2);
        point.setPointColor(displayColor);
        point.setPointStyle(PointStyle.FILLED_CIRCLE);
        segmentShape.setLineColor(displayColor);
        segmentShape.setLineStyle(LineStyle.SOLID);
        segmentShape.setLineWidth(1);
        segmentShape.addPoint(point);
      }
      // Add the plot shape to the renderer.
      _segmentShapes.add(segmentShape);
      addShape(segmentShape);
    }

    // Create the plot shape representation of the triangles.
    if (!_fault.isTriangulated()) {
      return;
    }

    TriangleDefinition[] triangles = _fault.getTriangulatedSurface().getTriangles();
    Point3d[] vertices = _fault.getTriangulatedSurface().getVertices();
    for (int i = 0; i < _fault.getNumTriangles(); i++) {
      IPlotPolygon triangleShape = new PlotPolygon();
      triangleShape.setName("Triangle #" + (i + 1));
      Point3d[] points = new Point3d[3];
      points[0] = vertices[triangles[i].getVertex1() - 1];
      points[1] = vertices[triangles[i].getVertex2() - 1];
      points[2] = vertices[triangles[i].getVertex3() - 1];

      // Loop thru the points in the triangulation, adding them to the plot shape.
      for (int j = 0; j < 3; j++) {
        double x = points[j].getX();
        double y = points[j].getY();
        double z = points[j].getZ();
        IPlotPoint point = new PlotPoint("", x, y, z);
        point.setPropertyInheritance(false);
        point.setPointSize(2);
        point.setPointColor(displayColor);
        point.setPointStyle(PointStyle.NONE);
        triangleShape.setLineColor(displayColor);
        triangleShape.setLineStyle(LineStyle.SOLID);
        triangleShape.setLineWidth(1);
        triangleShape.setFillStyle(FillStyle.NONE);
        triangleShape.addPoint(point);
      }
      // Add the plot shape to the renderer.
      _triangleShapes.add(triangleShape);
      addShape(triangleShape);
    }

  }

  @Override
  public DataSelection getDataSelection(final double x, final double y) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ReadoutInfo getReadoutInfo(final double x, final double y) {
    return new ReadoutInfo(getClass() + "");
  }

  @Override
  public Object[] getRenderedObjects() {
    return new Object[] { _fault };
  }

  @Override
  public void redraw() {
    // No action required.
  }

  @Override
  protected void addPopupMenuActions() {
    Dialog dialog = new FaultRendererDialog(getShell(), _fault.getDisplayName(), this);
    addSettingsPopupMenuAction(dialog, 600, SWT.DEFAULT);
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    // Add the renderer to the point set folder in the map view tree.
    addToLayerTree(IViewer.FAULT_FOLDER, autoUpdate);
  }

  @Override
  protected void setNameAndImage() {
    // Set the renderer name and image to that of the point set.
    setName(_fault);
    setImage(ModelUI.getSharedImages().getImage(_fault));
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    // Store the reference to the fault interpretation entity.
    _fault = (FaultInterpretation) objects[0];
  }

  public FaultRendererModel getSettingsModel() {
    return _model;
  }

  /**
   * Updates the display settings.
   * This will trigger the renderer to redraw.
   */
  public void updateSettings(final FaultRendererModel model) {
    _model.updateFrom(model);

    for (int i = 0; i < _segmentShapes.size(); i++) {
      _segmentShapes.get(i).blockUpdate();
    }
    for (int i = 0; i < _triangleShapes.size(); i++) {
      _triangleShapes.get(i).blockUpdate();
    }
    for (int i = 0; i < _segmentShapes.size(); i++) {
      _segmentShapes.get(i).setVisible(_model.getSegmentsVisible());
    }
    for (int i = 0; i < _triangleShapes.size(); i++) {
      _triangleShapes.get(i).setVisible(_model.getTrianglesVisible());
    }
    for (int i = 0; i < _segmentShapes.size(); i++) {
      _segmentShapes.get(i).unblockUpdate();
    }
    for (int i = 0; i < _triangleShapes.size(); i++) {
      _triangleShapes.get(i).unblockUpdate();
    }
    updated();
  }
}
