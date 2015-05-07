package org.geocraft.ui.sectionviewer.renderer.grid;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.event.DataSelection;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.seismic.TraceSection;
import org.geocraft.ui.model.ModelUI;
import org.geocraft.ui.plot.defs.PlotEventType;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.event.ModelSpaceEvent;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPolyline;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.object.PlotPolyline;
import org.geocraft.ui.sectionviewer.SectionViewRenderer;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.ReadoutInfo;


/**
 * Simple class for rendering a <code>Grid3d</code> entity in the section viewer.
 * The grid is rendered as a collection of polyline segments, with gaps between
 * the segments representing "null" values in the grid.
 */
public class Grid3dRenderer extends SectionViewRenderer implements IGridRendererConstants {

  /** The grid being rendered. */
  private Grid3d _grid;

  /** The current section displayed in the viewer. */
  private TraceSection _currentSection;

  /** The model of properties used to render the grid. */
  private final Grid3dRendererModel _model;

  /** The collection of polyline shapes used to render the grid. */
  private final List<IPlotPolyline> _plotPolylines;

  /** The model space canvas in which to render the image. */
  protected IModelSpaceCanvas _canvas;

  public Grid3dRenderer() {
    super("Grid Renderer");
    _model = new Grid3dRendererModel();
    _plotPolylines = Collections.synchronizedList(new ArrayList<IPlotPolyline>());
    showReadoutInfo(true);
  }

  @Override
  public ReadoutInfo getReadoutInfo(final int traceNum, final float z) {

    String[] keys = new String[] { "Row", "Column", "Amplitude" };

    // Check that the trace is within the bounds of the current section.
    if (traceNum > 1 && traceNum <= _currentSection.getNumTraces()) {
      // Get the x,y coordinates of the selected trace.
      Point3d[] xyPoints = _currentSection.getPointsXY();
      Point3d xyPoint = xyPoints[traceNum - 1];

      // Convert the x,y coordinates to row,col coordinates.
      double[] rowcol = _grid.getGeometry().transformXYToRowCol(xyPoint.getX(), xyPoint.getY(), true);
      int row = Math.round((float) rowcol[0]);
      int col = Math.round((float) rowcol[1]);

      // Check that the row,col coordinates are within bounds of the grid.
      if (row >= 0 && row < _grid.getNumRows() && col >= 0 && col < _grid.getNumColumns()) {
        // If so, then create and return the readout info.
        String zVal = "Null";
        if (!_grid.isNull(row, col)) {
          zVal = "" + _grid.getValueAtRowCol(row, col);
        }
        String[] values = new String[] { "" + row, "" + col, zVal };
        return new ReadoutInfo(_grid.toString(), keys, values);
      }
    }
    // Otherwise, return an empty readout info.
    return new ReadoutInfo(_grid.toString(), keys, new String[] { "", "", "" });
  }

  @Override
  public Object[] getRenderedObjects() {
    return new Object[] { _grid };
  }

  @Override
  public void modelSpaceUpdated(final ModelSpaceEvent event) {
    PlotEventType eventType = event.getEventType();
    if (eventType.equals(PlotEventType.AXIS_UPDATED) || eventType.equals(PlotEventType.VIEWABLE_BOUNDS_UPDATED)) {
      redraw(false);
    }
  }

  @Override
  public void redraw() {
    renderInternal(true);
  }

  public void redraw(final boolean reload) {
    renderInternal(reload);
  }

  @Override
  protected void addPlotShapes() {
    for (IPlotPolyline polyline : _plotPolylines) {
      addShape(polyline);
    }
  }

  @Override
  protected void addPopupMenuActions() {
    Dialog dialog = new Grid3dRendererDialog(getShell(), _grid.getDisplayName(), this);
    addSettingsPopupMenuAction(dialog, SWT.DEFAULT, SWT.DEFAULT);
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree(IViewer.GRID_FOLDER, autoUpdate);
  }

  @Override
  protected void setNameAndImage() {
    // Set the name and image for the renderer.
    setName(_grid);
    setImage(ModelUI.getSharedImages().getImage(_grid));
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    // Store a reference to the grid.
    _grid = (Grid3d) objects[0];
    _grid.load();
    _model.setLineColor(_grid.getDisplayColor());
    _canvas = getViewer().getModelSpaceCanvas();
  }

  @Override
  public DataSelection getDataSelection(final double trace, final double z) {
    int traceNo = (int) Math.round(trace);
    int traceIndex = traceNo - 1;
    // If the current section if null, or the trace is not within the section, then return null.
    if (_currentSection == null || traceNo < 1 || traceNo > _currentSection.getNumTraces()) {
      return null;
    }

    // Transform the trace coordinate to x,y coordinates.
    Point3d[] xyPoints = _currentSection.getPointsXY();
    Point3d xyPoint = xyPoints[traceIndex];
    double x = xyPoint.getX();
    double y = xyPoint.getY();
    float value = _grid.getValueAtXY(x, y);

    // If the grid value at the x,y coordinates is null, then return null.
    if (_grid.isNull(value)) {
      return null;
    }

    // If z value is not within a selection tolerance, then return null.
    double pixelDistance = getViewer().getPlot().getModelSpaceCanvas()
        .computePixelDistance(_modelSpace, trace, z, trace, value);
    double pixelTolerance = 4;
    if (Math.abs(pixelDistance) > pixelTolerance) {
      return null;
    }

    // Otherwise, return a valid data selection object.
    DataSelection dataSelection = new DataSelection(getClass().getSimpleName());
    dataSelection.setSelectedObjects(new Object[] { _grid });
    return dataSelection;
  }

  /**
   * Gets the model of properties used to render the grid.
   * 
   * @return the model of renderer properties.
   */
  public Grid3dRendererModel getSettingsModel() {
    return _model;
  }

  /**
   * Updates the display settings (size, style, color) of the points.
   * This will trigger the renderer to redraw.
   */
  public void updateSettings(final Grid3dRendererModel model) {
    _model.updateFrom(model);
    for (IPlotPolyline plotPolyline : _plotPolylines) {
      plotPolyline.blockUpdate();
      plotPolyline.setLineStyle(_model.getLineStyle());
      plotPolyline.setLineColor(_model.getLineColor());
      plotPolyline.setLineWidth(_model.getLineWidth());
      for (int i = 0; i < plotPolyline.getPointCount(); i++) {
        IPlotPoint point = plotPolyline.getPoint(i);
        point.setPointStyle(PointStyle.NONE);
        point.setPointSize(_model.getLineWidth());
        point.setPropertyInheritance(true);
      }
      plotPolyline.unblockUpdate();
      plotPolyline.updated();
    }
  }

  /**
   * Renders the grid.
   */
  protected void renderInternal(final boolean reload) {

    TraceSection section = getViewer().getTraceSection(_grid.getZDomain());
    if (section == null) {
      _currentSection = section;
      removeAllShapes(false);
      _plotPolylines.clear();
      super.updated();
      return;
    }

    int numTraces = section.getNumTraces();
    if (reload || !section.equals(_currentSection)) {
      removeAllShapes(false);
      _plotPolylines.clear();
      IPlotPolyline polyline = null;
      boolean isPrevNull = true;
      Point3d[] xyPoints = section.getPointsXY();
      for (int i = 0; i < numTraces; i++) {
        double x = xyPoints[i].getX();
        double y = xyPoints[i].getY();
        float z = _grid.getValueAtXY(x, y);
        if (!_grid.isNull(z)) {
          if (isPrevNull) {
            polyline = createPolyline(_model);
            _plotPolylines.add(polyline);
          }
          polyline.blockUpdate();
          IPlotPoint point = new PlotPoint(i + 1, z, 0);
          point.setPropertyInheritance(true);
          polyline.addPoint(point);
          polyline.unblockUpdate();
          isPrevNull = false;
        } else {
          isPrevNull = true;
        }
      }
    }
    _currentSection = section;
    for (IPlotPolyline polyline : _plotPolylines) {
      addShape(polyline);
    }

    super.updated();
  }

  /**
   * Creates a polyline shape that is used to render the grid.
   * 
   * @param model the model of rendering properties.
   * @return the polyline shape.
   */
  private IPlotPolyline createPolyline(final Grid3dRendererModel model) {
    IPlotPolyline polyline = new PlotPolyline();
    polyline.setPointStyle(PointStyle.NONE);
    polyline.setPointSize(0);
    polyline.setPointColor(model.getLineColor());
    polyline.setLineStyle(model.getLineStyle());
    polyline.setLineWidth(model.getLineWidth());
    polyline.setLineColor(model.getLineColor());
    return polyline;
  }
}
