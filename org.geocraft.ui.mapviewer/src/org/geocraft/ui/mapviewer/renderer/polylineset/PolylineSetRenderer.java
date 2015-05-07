package org.geocraft.ui.mapviewer.renderer.polylineset;


import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.model.Polyline;
import org.geocraft.core.model.PolylineSet;
import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.event.DataSelection;
import org.geocraft.ui.mapviewer.MapViewRenderer;
import org.geocraft.ui.model.ModelUI;
import org.geocraft.ui.plot.attribute.LineProperties;
import org.geocraft.ui.plot.attribute.PointProperties;
import org.geocraft.ui.plot.attribute.TextProperties;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPointGroup;
import org.geocraft.ui.plot.object.IPlotPolyline;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.object.PlotPolyline;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.ReadoutInfo;


public class PolylineSetRenderer extends MapViewRenderer {

  private PolylineSet _polylineSet;

  protected IPlotPolyline[] _polylineSetLines;

  protected IPlotPointGroup[] _pointFeaturePoint;

  public PolylineSetRenderer() {
    super("PolylineSet");
    showReadoutInfo(false);
  }

  @Override
  public Object[] getRenderedObjects() {
    return new Object[] { _polylineSet };
  }

  @Override
  public void redraw() {
    // No action.
  }

  @Override
  protected void addPopupMenuActions() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void addPlotShapes() {
    RGB orangeColor = new RGB(250, 120, 50);
    RGB polylineSetColor = orangeColor;
    //java.awt.Color oldColor = _polylineSet.getGeologicFeature().getColor();
    //Color polylineSetColor = new Color(null, new RGB(oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue()));
    RGB blueColor = new RGB(0, 0, 255);

    TextProperties textProps = new TextProperties();
    PointProperties pointProps = new PointProperties(PointStyle.NONE, polylineSetColor, 5);
    LineProperties lineProps = new LineProperties(LineStyle.SOLID, polylineSetColor, 1);

    int numPolylines = _polylineSet.getNumPolylines();
    Polyline[] polylines = _polylineSet.getPolylines();
    _polylineSetLines = new IPlotPolyline[numPolylines];
    double xmin = Double.MAX_VALUE;
    double xmax = -1 * Double.MAX_VALUE;
    double ymin = Double.MAX_VALUE;
    double ymax = -1 * Double.MAX_VALUE;
    for (int i = 0; i < numPolylines; i++) {
      _polylineSetLines[i] = new PlotPolyline("", textProps, pointProps, lineProps);
      _polylineSetLines[i].setName(_polylineSet.getDisplayName());
      CoordinateSeries coords = polylines[i].getPolyline();
      Point3d[] points = coords.getPointsDirect();
      for (Point3d point2 : points) {
        double x = point2.getX();
        double y = point2.getY();
        double z = point2.getZ();
        IPlotPoint point = new PlotPoint("", x, y, z);
        point.setPointSize(0);
        point.setPropertyInheritance(true);
        point.setPointColor(blueColor);
        point.setPointStyle(PointStyle.NONE);
        _polylineSetLines[i].addPoint(point);
        xmin = Math.min(xmin, x);
        xmax = Math.max(xmax, x);
        ymin = Math.min(ymin, y);
        ymax = Math.max(ymax, y);
      }
      addShape(_polylineSetLines[i]);
    }
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree(IViewer.FAULT_FOLDER, autoUpdate);
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    _polylineSet = (PolylineSet) objects[0];
  }

  @Override
  protected void setNameAndImage() {
    setName(_polylineSet);
    setImage(ModelUI.getSharedImages().getImage(_polylineSet));
  }

  @Override
  public DataSelection getDataSelection(final double x, final double y) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ReadoutInfo getReadoutInfo(final double x, final double y) {
    return new ReadoutInfo("" + getClass());
  }

  /* (non-Javadoc)
   * @see org.geocraft.ui.viewer.IRenderer#getSettingsModel()
   */
  @Override
  public PolylineSetRendererModel getSettingsModel() {
    // TODO Auto-generated method stub
    return null;
  }
}
