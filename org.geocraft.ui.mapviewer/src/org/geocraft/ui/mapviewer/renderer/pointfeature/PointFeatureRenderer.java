package org.geocraft.ui.mapviewer.renderer.pointfeature;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.model.culture.Layer;
import org.geocraft.core.model.culture.PointFeature;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.event.DataSelection;
import org.geocraft.ui.mapviewer.MapViewRenderer;
import org.geocraft.ui.model.ModelUI;
import org.geocraft.ui.plot.attribute.PointProperties;
import org.geocraft.ui.plot.attribute.TextProperties;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPointGroup;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.object.PlotPointGroup;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.ReadoutInfo;


public class PointFeatureRenderer extends MapViewRenderer {

  private PointFeature _pointFeature;

  private IPlotPointGroup[] _pointFeaturePoint;

  /**
   * Constructor
   */
  public PointFeatureRenderer() {
    super("PointFeatureRenderer");
    showReadoutInfo(false);
  }

  @Override
  public Object[] getRenderedObjects() {
    return new Object[] { _pointFeature };
  }

  @Override
  public void redraw() {
    // No action.
  }

  /**
   * point properties getter
   * @return the current point properties
   */
  public PointProperties getPointProperties() {
    return _pointFeaturePoint[0].getPoint(0).getPointProperties();
  }

  /**
   * setter for the point properties
   * @param properties the new point properties
   */
  public void setPointProperties(final PointProperties properties) {
    for (IPlotPointGroup pointFeaturePoint : _pointFeaturePoint) {
      for (int i = 0; i < pointFeaturePoint.getPointCount(); i++) {
        pointFeaturePoint.getPoint(i).setPointStyle(properties.getStyle());
        pointFeaturePoint.getPoint(i).setPointColor(properties.getColor());
        pointFeaturePoint.getPoint(i).setPointSize(properties.getSize());

      }
    }
  }

  @Override
  protected void addPopupMenuActions() {
    Dialog dialog = new PointFeatureDialog(getShell(), _pointFeature.getDisplayName(), this);
    addSettingsPopupMenuAction(dialog, SWT.DEFAULT, SWT.DEFAULT);
  }

  @Override
  protected void addPlotShapes() {
    TextProperties textProps = new TextProperties();
    RGB pointColor = new RGB(255, 0, 0);
    PointProperties pointProps = new PointProperties(PointStyle.FILLED_SQUARE, pointColor, 5);

    Point3d[] pathPoints = _pointFeature.getPoints().getPointsDirect();
    int i = 0;
    _pointFeaturePoint = new PlotPointGroup[pathPoints.length];
    for (Point3d pathPoint : pathPoints) {
      _pointFeaturePoint[i] = new PlotPointGroup("", textProps, pointProps);
      _pointFeaturePoint[i].setName(_pointFeature.getDisplayName());
      double x = pathPoint.getX();
      double y = pathPoint.getY();
      double z = pathPoint.getZ();
      IPlotPoint point = new PlotPoint("", x, y, z);
      point.setPropertyInheritance(false);
      point.setPointSize(5);
      point.setPointColor(pointColor);
      point.setPointStyle(PointStyle.FILLED_SQUARE);
      _pointFeaturePoint[i].addPoint(point);
      addShape(_pointFeaturePoint[i]);
      i++;
    }
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    Layer layer = _pointFeature.getLayer();
    addToLayerTree(layer, layer.getDisplayName(), IViewer.CULTURE_FOLDER, autoUpdate);
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    _pointFeature = (PointFeature) objects[0];
  }

  @Override
  protected void setNameAndImage() {
    setName(_pointFeature);
    setImage(ModelUI.getSharedImages().getImage(_pointFeature));
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
  public PointFeatureRendererModel getSettingsModel() {
    // TODO Auto-generated method stub
    return null;
  }
}
