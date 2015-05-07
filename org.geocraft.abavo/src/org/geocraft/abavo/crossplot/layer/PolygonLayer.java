/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot.layer;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.geocraft.abavo.ABavoImages;
import org.geocraft.abavo.Activator;
import org.geocraft.abavo.polygon.PolygonModel;
import org.geocraft.abavo.polygon.PolygonRegionsModel;
import org.geocraft.abavo.polygon.PolygonRegionsModel.PolygonType;
import org.geocraft.core.model.IModelListener;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.defs.RenderLevel;
import org.geocraft.ui.plot.layer.PlotLayer;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPolygon;
import org.geocraft.ui.plot.object.PlotPoint;
import org.geocraft.ui.plot.object.PlotPolygon;
import org.geocraft.ui.plot.util.PolygonRegionsUtil;
import org.geocraft.ui.viewer.ReadoutInfo;


public class PolygonLayer extends PlotLayer implements IModelListener {

  /** The polygon model. */
  private final PolygonModel _polygonModel;

  /** The plot polygon for the active polygon. */
  private final IPlotPolygon _polygon;

  /** The polygon point, line and fill color. */
  protected RGB _color;

  public PolygonLayer(final PolygonModel polygonModel) {
    super(polygonModel.getType().toString() + " Polygon");

    showReadoutInfo(false);

    _polygonModel = polygonModel;
    _polygonModel.addListener(this);

    // Create the image and color for the layer.
    PolygonType type = polygonModel.getType();
    if (type.equals(PolygonRegionsModel.PolygonType.Region)) {
      _image = Activator.getDefault().createImage(ABavoImages.SELECTION_POLYGON);
    } else if (type.equals(PolygonRegionsModel.PolygonType.Selection)) {
      _image = Activator.getDefault().createImage(ABavoImages.SELECTION_POLYGON);
    }
    _color = polygonModel.getColor();

    _polygon = new PlotPolygon();
    _polygon.setSelectable(true);
    _polygon.setRenderLevel(RenderLevel.IMAGE_UNDER_GRID);
    _polygon.setName("Polygon" + polygonModel.getId());
    _polygon.setLineColor(_color);
    _polygon.setFillColor(_color);
    Color pointColor = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
    _polygon.setPointColor(pointColor.getRGB());
    _polygon.setPointStyle(PointStyle.FILLED_SQUARE);
    _polygon.setPointSize(3);
    for (int i = 0; i < polygonModel.getNumPoints(); i++) {
      Point3d pt = polygonModel.getPoint(i);
      IPlotPoint point = new PlotPoint(pt.getX(), pt.getY(), pt.getZ());
      point.setPropertyInheritance(true);
      _polygon.addPoint(point);
    }
    addShape(_polygon);
  }

  @Override
  protected Image createImage() {
    return Activator.getDefault().createImage(ABavoImages.SELECTION_POLYGON);
  }

  /**
   * Returns the plot polygon.
   * @return the plot polygon.
   */
  public IPlotPolygon getPolygon() {
    return _polygon;
  }

  @Override
  public ReadoutInfo getReadoutInfo(final double x, final double y) {
    List<String> keys = new ArrayList<String>();
    List<String> values = new ArrayList<String>();

    if (isVisible() && _polygon != null && PolygonRegionsUtil.isPointInside(_polygon, x, y)) {
      String className = _polygonModel.getText();
      double classValue = _polygonModel.getValue();
      keys.add("Class");
      values.add(className + " (" + classValue + ")");
    }

    return new ReadoutInfo("Region Polygon " + _polygonModel.getId(), keys, values);
  }

  public RGB getColor() {
    return _color;
  }

  public void setColor(final RGB color) {
    _color = color;
    _polygon.setLineColor(color);
    _polygon.setFillColor(color);
  }

  public void propertyChanged(final String key) {
    if (!key.equalsIgnoreCase("POINTS")) {
      return;
    }
    _polygon.blockUpdate();
    _polygon.clear();
    for (int i = 0; i < _polygonModel.getNumPoints(); i++) {
      Point3d pt = _polygonModel.getPoint(i);
      IPlotPoint point = new PlotPoint(pt.getX(), pt.getY(), pt.getZ());
      point.setPropertyInheritance(true);
      _polygon.addPoint(point);
    }
    _polygon.unblockUpdate();
    _polygon.updated();
  }

}
