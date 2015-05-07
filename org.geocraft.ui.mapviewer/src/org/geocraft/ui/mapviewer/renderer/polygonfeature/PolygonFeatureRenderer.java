package org.geocraft.ui.mapviewer.renderer.polygonfeature;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.culture.Layer;
import org.geocraft.core.model.culture.PolygonFeature;
import org.geocraft.core.model.culture.SimplePolygon;
import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.event.DataSelection;
import org.geocraft.ui.mapviewer.MapViewRenderer;
import org.geocraft.ui.model.ModelUI;
import org.geocraft.ui.plot.attribute.FillProperties;
import org.geocraft.ui.plot.attribute.LineProperties;
import org.geocraft.ui.plot.attribute.PointProperties;
import org.geocraft.ui.plot.attribute.TextProperties;
import org.geocraft.ui.plot.defs.FillStyle;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPolygon;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.object.PlotPolygon;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.ReadoutInfo;


public class PolygonFeatureRenderer extends MapViewRenderer {

  private PolygonFeature _polygonFeature;

  private IPlotPolygon[] _polygonFeatureLines;

  public PolygonFeatureRenderer() {
    super("PolygonFeature");
    showReadoutInfo(false);
  }

  @Override
  public Object[] getRenderedObjects() {
    return new Object[] { _polygonFeature };
  }

  @Override
  public void redraw() {
    // No action.
  }

  public PointProperties getPointProperties() {
    return _polygonFeatureLines[0].getPointProperties();
  }

  public void setPointProperties(final PointProperties properties) {
    for (IPlotPolygon featureLine : _polygonFeatureLines) {
      featureLine.setPointStyle(properties.getStyle());
      featureLine.setPointColor(properties.getColor());
      featureLine.setPointSize(properties.getSize());
    }
  }

  public FillProperties getFillProperties() {
    return _polygonFeatureLines[0].getFillProperties();
  }

  public void setFillProperties(final FillProperties properties) {
    for (IPlotPolygon featureLine : _polygonFeatureLines) {
      featureLine.setFillStyle(properties.getStyle());
      featureLine.setFillColor(properties.getRGB());
    }
  }

  public LineProperties getLineProperties() {
    return _polygonFeatureLines[0].getLineProperties();
  }

  public void setLineProperties(final LineProperties properties) {
    for (IPlotPolygon featureLine : _polygonFeatureLines) {
      featureLine.setLineStyle(properties.getStyle());
      featureLine.setLineColor(properties.getColor());
      featureLine.setLineWidth(properties.getWidth());
    }
  }

  @Override
  protected void addPopupMenuActions() {
    Dialog dialog = new PolygonFeatureDialog(getShell(), _polygonFeature.getDisplayName(), this);
    addSettingsPopupMenuAction(dialog, SWT.DEFAULT, SWT.DEFAULT);
  }

  @Override
  protected void addPlotShapes() {
    RGB blueColor = new RGB(0, 0, 255);

    TextProperties textProps = new TextProperties();
    PointProperties pointProps = new PointProperties(PointStyle.NONE, blueColor, 5);
    LineProperties lineProps = new LineProperties(LineStyle.SOLID, blueColor, 1);
    FillProperties fillProperties = new FillProperties();
    fillProperties.setRGB(blueColor);
    fillProperties.setStyle(FillStyle.SOLID);
    FillProperties fillProps = new FillProperties(fillProperties);

    int numPolygons = _polygonFeature.getNumPolygons();
    SimplePolygon[] polygons = _polygonFeature.getPolygons();
    _polygonFeatureLines = new IPlotPolygon[numPolygons];
    for (int i = 0; i < numPolygons; i++) {
      _polygonFeatureLines[i] = new PlotPolygon("", textProps, pointProps, lineProps, fillProps);
      _polygonFeatureLines[i].setName(_polygonFeature.getDisplayName());
      CoordinateSeries coords = polygons[i].getPoints();
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
        _polygonFeatureLines[i].addPoint(point);
      }
      addShape(_polygonFeatureLines[i]);
    }
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    Layer layer = _polygonFeature.getLayer();
    addToLayerTree(layer, layer.getDisplayName(), IViewer.CULTURE_FOLDER, autoUpdate);
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    _polygonFeature = (PolygonFeature) objects[0];
  }

  @Override
  protected void setNameAndImage() {
    setName(_polygonFeature);
    setImage(ModelUI.getSharedImages().getImage(_polygonFeature));
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
  public Model getSettingsModel() {
    // TODO Auto-generated method stub
    return null;
  }
}
