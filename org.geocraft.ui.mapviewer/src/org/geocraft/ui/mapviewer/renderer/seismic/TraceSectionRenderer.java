package org.geocraft.ui.mapviewer.renderer.seismic;


import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.event.DataSelection;
import org.geocraft.core.model.seismic.TraceSection;
import org.geocraft.ui.mapviewer.IMapViewer;
import org.geocraft.ui.mapviewer.MapViewRenderer;
import org.geocraft.ui.model.ModelUI;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPolyline;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.object.PlotPolyline;
import org.geocraft.ui.viewer.ReadoutInfo;


public class TraceSectionRenderer extends MapViewRenderer {

  /** The trace section. */
  private TraceSection _traceSection;

  /** The plot shape representation of the trace section. */
  private IPlotPolyline _plotShape;

  public TraceSectionRenderer() {
    super("Trace Section Renderer");
  }

  @Override
  public Object[] getRenderedObjects() {
    return new Object[] { _traceSection };
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    _traceSection = (TraceSection) objects[0];
  }

  @Override
  protected void addPopupMenuActions() {
    //Dialog dialog = new TraceSectionSettingsDialog(_shell, this);
    //addSettingsPopupMenuAction(dialog, SWT.DEFAULT, SWT.DEFAULT);
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree(IMapViewer.AOI_ROI_FOLDER, autoUpdate);
  }

  @Override
  protected void setNameAndImage() {
    setName(_traceSection.getDisplayName());
    setImage(ModelUI.getSharedImages().getImage("AreaOfInterest"));
  }

  @Override
  public DataSelection getDataSelection(final double x, final double y) {
    return null;
  }

  @Override
  public ReadoutInfo getReadoutInfo(final double x, final double y) {
    return new ReadoutInfo("" + getClass());
  }

  @Override
  public void redraw() {
    addShape(_plotShape);
  }

  @Override
  protected void addPlotShapes() {
    RGB color = new RGB(255, 0, 0);
    if (_traceSection.hasControlPoints()) {
      _plotShape = new PlotPolyline();
      _plotShape.setName(_traceSection.getDisplayName());
      _plotShape.setSelectable(false);
      _plotShape.setLineStyle(LineStyle.SOLID);
      _plotShape.setLineWidth(1);
      _plotShape.setLineColor(color);
      _plotShape.setPointStyle(PointStyle.NONE);
      _plotShape.setPointSize(0);
      for (Point3d point3d : _traceSection.getControlPoints()) {
        IPlotPoint point = new PlotPoint(point3d.getX(), point3d.getY(), 0);
        point.setPropertyInheritance(true);
        _plotShape.addPoint(point);
      }
    } else {
      _plotShape = new PlotPolyline();
      _plotShape.setName(_traceSection.getDisplayName());
      _plotShape.setSelectable(false);
      _plotShape.setLineStyle(LineStyle.NONE);
      _plotShape.setLineWidth(0);
      _plotShape.setPointColor(color);
      _plotShape.setPointStyle(PointStyle.CIRCLE);
      _plotShape.setPointSize(1);
      for (Point3d point3d : _traceSection.getPointsXY()) {
        IPlotPoint point = new PlotPoint(point3d.getX(), point3d.getY(), 0);
        point.setPropertyInheritance(true);
        _plotShape.addPoint(point);
      }
    }
    addShape(_plotShape);
  }

  /* (non-Javadoc)
   * @see org.geocraft.ui.viewer.IRenderer#getSettingsModel()
   */
  @Override
  public TraceSectionRendererModel getSettingsModel() {
    // TODO Auto-generated method stub
    return null;
  }

}
