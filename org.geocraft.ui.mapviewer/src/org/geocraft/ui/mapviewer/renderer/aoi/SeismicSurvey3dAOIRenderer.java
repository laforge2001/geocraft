package org.geocraft.ui.mapviewer.renderer.aoi;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.aoi.SeismicSurvey3dAOI;
import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.event.DataSelection;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
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
import org.geocraft.ui.plot.object.IPlotPointGroup;
import org.geocraft.ui.plot.object.IPlotPolygon;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.object.PlotPointGroup;
import org.geocraft.ui.plot.object.PlotPolygon;
import org.geocraft.ui.plot.util.PlotUtil;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.ReadoutInfo;


public class SeismicSurvey3dAOIRenderer extends MapViewRenderer {

  /** The area-of-interest. */
  private AreaOfInterest _aoi;

  /** The area-of-interest display polygons. */
  private List<IPlotPolygon> _aoiPolygons;

  /** The area-of-interest display polygons. */
  private IPlotPointGroup _aoiPoints;

  public SeismicSurvey3dAOIRenderer() {
    super("SeismicSurvey3dAOI Renderer");
  }

  @Override
  public Object[] getRenderedObjects() {
    return new Object[] { _aoi };
  }

  @Override
  public void redraw() {
    updated();
    //    for (IPlotPolygon aoiPolygon : _aoiPolygons) {
    //      addShape(aoiPolygon);
    //    }
    //    if (_aoiPoints.getPointCount() > 0) {
    //      addShape(_aoiPoints);
    //    }
  }

  public PointProperties getPointProperties() {
    if (_aoiPoints.getPointCount() > 0) {
      return _aoiPoints.getFirstPoint().getPointProperties();
    }
    return null;
  }

  public void setPointProperties(final PointProperties properties) {
    for (int i = 0; i < _aoiPoints.getPointCount(); i++) {
      _aoiPoints.getPoint(i).setPointStyle(properties.getStyle());
      _aoiPoints.getPoint(i).setPointColor(properties.getColor());
      _aoiPoints.getPoint(i).setPointSize(properties.getSize());
    }
  }

  public FillProperties getFillProperties() {
    return _aoiPolygons.get(0).getFillProperties();
  }

  public void setFillProperties(final FillProperties properties) {
    for (IPlotPolygon polygon : _aoiPolygons) {
      polygon.setFillStyle(properties.getStyle());
      polygon.setFillColor(properties.getRGB());
    }
  }

  public LineProperties getLineProperties() {
    return _aoiPolygons.get(0).getLineProperties();
  }

  public void setLineProperties(final LineProperties properties) {
    for (IPlotPolygon polygon : _aoiPolygons) {
      polygon.setLineStyle(properties.getStyle());
      polygon.setLineColor(properties.getColor());
      polygon.setLineWidth(properties.getWidth());
    }
  }

  @Override
  public ReadoutInfo getReadoutInfo(final double x, final double y) {
    return new ReadoutInfo(_aoi.toString(), new String[] { "Inside?" }, new String[] { "" + _aoi.contains(x, y) });
  }

  @Override
  protected void addPopupMenuActions() {
    //Dialog dialog = new SeismicSurvey3dAOIRendererDialog(_shell, _aoi.getDisplayName(), this);
    //addSettingsPopupMenuAction(dialog, SWT.DEFAULT, SWT.DEFAULT);
  }

  @Override
  protected void addPlotShapes() {
    RGB grayColor = new Color(null, PlotUtil.RGB_LIGHT_GRAY).getRGB();

    // Set the properties for the display polygons.
    TextProperties textProps = new TextProperties();
    PointProperties pointProps = new PointProperties(PointStyle.CIRCLE, PlotUtil.RGB_LIGHT_GRAY, 5);
    LineProperties lineProps = new LineProperties(LineStyle.SOLID, grayColor, 1);
    FillProperties fillProps = new FillProperties();
    fillProps.setRGB(new RGB(150, 150, 150));
    fillProps.setStyle(FillStyle.NONE);

    SeismicSurvey3dAOI aoi = (SeismicSurvey3dAOI) _aoi;

    // Create the display polygons.
    _aoiPolygons = Collections.synchronizedList(new ArrayList<IPlotPolygon>());
    _aoiPoints = new PlotPointGroup("", textProps, pointProps);
    SeismicSurvey3d geometry = aoi.getSurvey();
    if (aoi.isDecimated()) {
      float[] inlines = { 0 };
      float[] xlines = { 0 };
      for (int i = 0; i < aoi.getNumInlines(); i++) {
        inlines[0] = aoi.getInlineStart() + i * aoi.getInlineDelta();
        for (int j = 0; j < aoi.getNumXlines(); j++) {
          xlines[0] = aoi.getXlineStart() + j * aoi.getXlineDelta();
          CoordinateSeries coord = geometry.transformInlineXlineToXY(inlines, xlines);
          Point3d p = coord.getPoint(0);
          IPlotPoint point = new PlotPoint(p.getX(), p.getY(), 0);
          point.setPropertyInheritance(true);
          _aoiPoints.addPoint(point);
        }
      }
    } else {
      pointProps.setStyle(PointStyle.NONE);
      float inlineStart = aoi.getInlineStart();
      float inlineEnd = aoi.getInlineEnd();
      float xlineStart = aoi.getXlineStart();
      float xlineEnd = aoi.getXlineEnd();
      float[] inlines = { inlineStart, inlineStart, inlineEnd, inlineEnd };
      float[] xlines = { xlineStart, xlineEnd, xlineEnd, xlineStart };
      CoordinateSeries coord = geometry.transformInlineXlineToXY(inlines, xlines);
      IPlotPolygon aoiPolygon = new PlotPolygon("", textProps, pointProps, lineProps, fillProps);
      aoiPolygon.setName(_aoi.getDisplayName());
      for (int i = 0; i < coord.getNumPoints(); i++) {
        Point3d p = coord.getPoint(i);
        IPlotPoint point = new PlotPoint(p.getX(), p.getY(), 0);
        point.setPropertyInheritance(true);
        aoiPolygon.addPoint(point);
      }
      _aoiPolygons.add(aoiPolygon);
    }
    //      _groups.add(new PlotGroup(FileUtil.getShortName(aoi.getDisplayName()) + " Inclusion"));

    for (IPlotPolygon aoiPolygon : _aoiPolygons) {
      addShape(aoiPolygon);
    }
    if (_aoiPoints.getPointCount() > 0) {
      addShape(_aoiPoints);
    }
  }

  @Override
  protected void addToLayerTree(final boolean autoUpdate) {
    addToLayerTree(IViewer.AOI_ROI_FOLDER, autoUpdate);
  }

  @Override
  protected void setRenderedObjects(final Object[] objects) {
    _aoi = (SeismicSurvey3dAOI) objects[0];
  }

  @Override
  protected void setNameAndImage() {
    setName(_aoi);
    setImage(ModelUI.getSharedImages().getImage(_aoi));
  }

  @Override
  public DataSelection getDataSelection(final double x, final double y) {
    return null;
  }

  @Override
  public SeismicSurvey3dAOIRendererModel getSettingsModel() {
    // TODO Auto-generated method stub
    return null;
  }

}
