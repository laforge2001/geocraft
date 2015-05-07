/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.ui.mapviewer.renderer.polylinefeature;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.culture.Layer;
import org.geocraft.core.model.culture.PolylineFeature;
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
import org.geocraft.ui.plot.object.IPlotPolyline;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.object.PlotPolyline;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.ReadoutInfo;


public class PolylineFeatureRenderer extends MapViewRenderer {

  private PolylineFeature _polylineFeature;

  private IPlotPolyline[] _polylineFeatureLines;

  public PolylineFeatureRenderer() {
    super("Polyline Feature");
    showReadoutInfo(false);
  }

  public LineProperties getLineProperties() {
    if (_polylineFeatureLines.length > 0) {
      return _polylineFeatureLines[0].getLineProperties();
    }
    return null;
  }

  public void setLineProperties(final LineProperties properties) {
    for (IPlotPolyline line : _polylineFeatureLines) {
      line.setLineStyle(properties.getStyle());
      line.setLineColor(properties.getColor());
      line.setLineWidth(properties.getWidth());
    }
  }

  // TODO: do we need this?
  //  public void addToModel(final IPlot plot, final IPlotModel model) {
  //    super.addToModel(plot, model);
  //
  //    Unit xUnits = _model.getAxisX().getUnits();
  //    Unit yUnits = _model.getAxisY().getUnits();
  //    Unit xyUnits = xUnits; // TODO: Need to resolve units issue.
  //
  //    int numParts = _polylineFeature.getNumParts();
  //    int[] numPointsPerPart = _polylineFeature.getNumPointsPerPart();
  //    Point3d[] points = _polylineFeature.getPoints().getPoints();
  //    int index = 0;
  //    for (int i = 0; i < numParts; i++) {
  //      try {
  //        for (int j = 0; j < numPointsPerPart[i]; j++) {
  //          double x = points[index].getX();
  //          double y = points[index].getY();
  //          x = Unit.convert(x, xUnits, xyUnits);
  //          y = Unit.convert(y, yUnits, xyUnits);
  //          _polylineFeatureLines[i].getPoint(j).moveTo(x, y);
  //          index++;
  //        }
  //      } catch (Exception ex) {
  //        ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.toString(), ex);
  //      }
  //    }
  //  }

  @Override
  public void redraw() {
    // No action.
  }

  @Override
  public Object[] getRenderedObjects() {
    return new Object[] { _polylineFeature };
  }

  @Override
  protected void addPopupMenuActions() {
    Dialog dialog = new PolylineFeatureDialog(getShell(), _polylineFeature.getDisplayName(), this);
    addSettingsPopupMenuAction(dialog, SWT.DEFAULT, SWT.DEFAULT);
  }

  @Override
  protected void addPlotShapes() {
    RGB lineColor = new RGB(0, 255, 0);
    TextProperties textProps = new TextProperties();
    PointProperties pointProps = new PointProperties(PointStyle.NONE, lineColor, 5);
    LineProperties lineProps = new LineProperties(LineStyle.SOLID, lineColor, 1);

    int numParts = _polylineFeature.getNumParts();
    int[] numPointsPerPart = _polylineFeature.getNumPointsPerPart();
    _polylineFeatureLines = new IPlotPolyline[numParts];
    Point3d[] pathPoints = _polylineFeature.getPoints().getPointsDirect();
    int index = 0;
    for (int i = 0; i < numParts; i++) {
      _polylineFeatureLines[i] = new PlotPolyline("", textProps, pointProps, lineProps);
      _polylineFeatureLines[i].setName(_polylineFeature.getDisplayName());
      for (int j = 0; j < numPointsPerPart[i]; j++) {
        double x = pathPoints[index].getX();
        double y = pathPoints[index].getY();
        double z = pathPoints[index].getZ();
        IPlotPoint point = new PlotPoint("", x, y, z);
        point.setPointSize(0);
        point.setPropertyInheritance(true);
        point.setPointColor(pointProps.getColor());
        point.setPointStyle(pointProps.getStyle());
        _polylineFeatureLines[i].addPoint(point);
        index++;
      }
      addShape(_polylineFeatureLines[i]);
    }
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    Layer layer = _polylineFeature.getLayer();
    addToLayerTree(layer, layer.getDisplayName(), IViewer.CULTURE_FOLDER, autoUpdate);
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    _polylineFeature = (PolylineFeature) objects[0];
  }

  @Override
  protected void setNameAndImage() {
    setName(_polylineFeature);
    setImage(ModelUI.getSharedImages().getImage(_polylineFeature));
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
