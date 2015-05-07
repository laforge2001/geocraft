package org.geocraft.ui.mapviewer.renderer.well;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.event.DataSelection;
import org.geocraft.core.model.well.Well;
import org.geocraft.core.model.well.WellBore;
import org.geocraft.ui.mapviewer.MapViewRenderer;
import org.geocraft.ui.model.ModelUI;
import org.geocraft.ui.plot.attribute.LineProperties;
import org.geocraft.ui.plot.attribute.PointProperties;
import org.geocraft.ui.plot.attribute.TextProperties;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.defs.TextAnchor;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPolyline;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.object.PlotPolyline;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.ReadoutInfo;


public class WellRenderer extends MapViewRenderer {

  /** The well to render. */

  private Well _well;

  /** The well bore to render. */
  //private WellBore _wellBore;

  /** The model of display settings. */
  private final WellRendererModel _model;

  /** The plot shape representing the well bore. */
  private IPlotPolyline _positionLogLine;

  public WellRenderer() {
    super("WellBore");
    _model = new WellRendererModel();
  }

  @Override
  public Object[] getRenderedObjects() {
    return new Object[] { _well };
  }

  @Override
  public void redraw() {
    // No action.
  }

  @Override
  protected void addPopupMenuActions() {
    Dialog dialog = new WellRendererDialog(getShell(), _well.getDisplayName(), this);
    addSettingsPopupMenuAction(dialog, SWT.DEFAULT, SWT.DEFAULT);
  }

  @Override
  protected void addPlotShapes() {

    WellBore wellBore = _well.getWellBore();
    TextProperties textProperties = new TextProperties();
    PointProperties pointProperties = new PointProperties(PointStyle.NONE, new RGB(0, 0, 0), 0);
    LineProperties lineProperties = _model.getBoreLineProperties();
    _positionLogLine = new PlotPolyline("", textProperties, pointProperties, lineProperties);
    _positionLogLine.setName(_well.getDisplayName());
    double xmin = Double.MAX_VALUE;
    double xmax = -1 * Double.MAX_VALUE;
    double ymin = Double.MAX_VALUE;
    double ymax = -1 * Double.MAX_VALUE;
    CoordinateSeries pathTVDSS = wellBore.getPathTVDSS();
    CoordinateSeries pathTWT = wellBore.getPathTWT();
    Point3d[] pathPoints = new Point3d[0];
    if (pathTVDSS.getNumPoints() > 0) {
      pathPoints = pathTVDSS.getPointsDirect();
    } else if (pathTWT.getNumPoints() > 0) {
      pathPoints = pathTWT.getPointsDirect();
    }
    for (int i = 0; i < pathPoints.length; i++) {
      double x = pathPoints[i].getX();
      double y = pathPoints[i].getY();
      double ztvd = _well.getTotalDepth();
      IPlotPoint point = new PlotPoint("", x, y, ztvd);
      point.setPointSize(5);
      point.setPointStyle(PointStyle.NONE);
      point.setPropertyInheritance(true);
      if (i == 0) {
        point.setPropertyInheritance(false);
        point.setPointStyle(_model.getTopHoleSymbol());
        point.setPointSize(_model.getTopHoleSymbolSize());
        point.setPointColor(_model.getTopHoleSymbolColor());
      } else if (i == pathPoints.length - 1) {
        point.setPropertyInheritance(false);
        point.setPointStyle(_model.getBottomHoleSymbol());
        point.setPointSize(_model.getBottomHoleSymbolSize());
        point.setPointColor(_model.getBottomHoleSymbolColor());
        point.setName(wellBore.getDisplayName());
        double dx = 0;
        double dy = 0;
        if (pathPoints.length >= 2) {
          dx = pathPoints[i].getX() - pathPoints[i - 1].getX();
          dy = pathPoints[i].getY() - pathPoints[i - 1].getY();
        }
        _positionLogLine.setTextColor(_model.getBottomHoleSymbolColor());
        _positionLogLine.setTextAnchor(TextAnchor.getBest(dx, dy));
      }
      _positionLogLine.addPoint(point);
      xmin = Math.min(xmin, x);
      xmax = Math.max(xmax, x);
      ymin = Math.min(ymin, y);
      ymax = Math.max(ymax, y);
    }
    addShape(_positionLogLine);
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree(null, _well.getDisplayName(), IViewer.WELL_FOLDER, autoUpdate);
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    _well = (Well) objects[0];
  }

  @Override
  protected void setNameAndImage() {
    setName(_well);
    setImage(ModelUI.getSharedImages().getImage(_well));
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

  public WellRendererModel getSettingsModel() {
    return _model;
  }

  public void updateSettings(final WellRendererModel model) {
    _model.updateFrom(model);
    setTextProperties(model);
    setPointProperties(model);
    setLineProperties(model);
  }

  protected void setTextProperties(final WellRendererModel model) {
    final TextProperties properties = new TextProperties();

    _positionLogLine.setTextColor(model.getBottomHoleSymbolColor());
  }

  protected void setPointProperties(final WellRendererModel model) {
    IPlotPoint topHolePoint = _positionLogLine.getPoint(0);
    topHolePoint.setPointStyle(model.getTopHoleSymbol());
    topHolePoint.setPointSize(model.getTopHoleSymbolSize());
    topHolePoint.setPointColor(model.getTopHoleSymbolColor());

    IPlotPoint bottomHolePoint = _positionLogLine.getPoint(_positionLogLine.getPointCount() - 1);
    bottomHolePoint.setPointStyle(model.getBottomHoleSymbol());
    bottomHolePoint.setPointSize(model.getBottomHoleSymbolSize());
    bottomHolePoint.setPointColor(model.getBottomHoleSymbolColor());
  }

  protected void setLineProperties(final WellRendererModel model) {
    LineProperties properties = model.getBoreLineProperties();
    PointProperties firstPointProperties = _positionLogLine.getPoint(0).getPointProperties();
    PointProperties lastPointProperties = _positionLogLine.getPoint(_positionLogLine.getPointCount() - 1)
        .getPointProperties();

    _positionLogLine.setLineStyle(properties.getStyle());
    _positionLogLine.setLineColor(properties.getColor());
    _positionLogLine.setLineWidth(properties.getWidth());

    _positionLogLine.getPoint(0).setPointStyle(firstPointProperties.getStyle());
    _positionLogLine.getPoint(0).setPointColor(firstPointProperties.getColor());
    _positionLogLine.getPoint(0).setPointSize(firstPointProperties.getSize());

    _positionLogLine.getPoint(_positionLogLine.getPointCount() - 1).setPointStyle(lastPointProperties.getStyle());
    _positionLogLine.getPoint(_positionLogLine.getPointCount() - 1).setPointColor(lastPointProperties.getColor());
    _positionLogLine.getPoint(_positionLogLine.getPointCount() - 1).setPointSize(lastPointProperties.getSize());
  }
}
