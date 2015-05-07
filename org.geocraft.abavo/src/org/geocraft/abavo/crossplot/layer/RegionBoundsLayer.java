/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot.layer;


import java.awt.geom.Point2D;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.geocraft.abavo.ABavoImages;
import org.geocraft.abavo.Activator;
import org.geocraft.abavo.crossplot.IABavoCrossplot;
import org.geocraft.abavo.ellipse.EllipseModel;
import org.geocraft.abavo.ellipse.EllipseRegionsModel;
import org.geocraft.abavo.ellipse.EllipseUtil;
import org.geocraft.abavo.ellipse.RegionsBoundary;
import org.geocraft.abavo.ellipse.EllipseRegionsModel.EllipseType;
import org.geocraft.abavo.ellipse.EllipseRegionsModelEvent.Type;
import org.geocraft.ui.plot.attribute.LineProperties;
import org.geocraft.ui.plot.attribute.PointProperties;
import org.geocraft.ui.plot.attribute.TextProperties;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.PlotEventType;
import org.geocraft.ui.plot.defs.PointStyle;
import org.geocraft.ui.plot.event.ModelSpaceEvent;
import org.geocraft.ui.plot.event.ShapeEvent;
import org.geocraft.ui.plot.layer.PlotLayer;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.ModelSpaceBounds;
import org.geocraft.ui.plot.object.IPlotLine;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.PlotLine;


public class RegionBoundsLayer extends PlotLayer {//implements IModelSpaceListener {

  private final IPlotLine[] _regionBounds;

  private final IABavoCrossplot _crossplot;

  public RegionBoundsLayer(final IABavoCrossplot crossplot) {
    super("Region Bounds");
    _crossplot = crossplot;
    TextProperties textProps = new TextProperties();
    Color pointColor = Display.getCurrent().getSystemColor(SWT.COLOR_BLUE);
    PointProperties pointProps = new PointProperties(PointStyle.FILLED_SQUARE, pointColor.getRGB(), 4);
    Color lineColor = pointColor;
    LineProperties lineProps = new LineProperties(LineStyle.SOLID, lineColor.getRGB(), 1);
    _regionBounds = new IPlotLine[EllipseRegionsModel.NUMBER_OF_REGION_BOUNDARIES];
    for (int i = 0; i < _regionBounds.length; i++) {
      _regionBounds[i] = new PlotLine("", textProps, pointProps, lineProps);
      _regionBounds[i].blockUpdate();
      _regionBounds[i].clear();
      _regionBounds[i].setSelectable(true);
      //_regionBounds[i].addShapeListener(this);
      addShape(_regionBounds[i]);
      _regionBounds[i].unblockUpdate();
    }

    //_actions.clear();
    // _actions.add(new ViewLayerEditorAction(this, "Edit Properties..."));
  }

  @Override
  public Image createImage() {
    return Activator.getDefault().createImage(ABavoImages.REGION_BOUNDS);
  }

  /**
   * Returns the line shape of the specified region boundary.
   * The index value must be in the range (0-NUMBER_OF_REGION_BOUNDARIES).
   * @param index the index of the region boundary to get.
   * @return the line shape of the specified region boundary.
   */
  public IPlotLine getRegionBound(final int index) {
    return _regionBounds[index];
  }

  public void draw() {
    for (IPlotLine line : _regionBounds) {
      addShape(line);
    }
  }

  @Override
  public void setModelSpace(final IModelSpace modelSpace) {
    super.setModelSpace(modelSpace);
    // The region bounds layer needs to listen to the model space, as the shapes will
    // need to update when the default bounds update.
    if (_modelSpace != null) {
      //_modelSpace.addListener(this);
    }
  }

  @Override
  public void shapeUpdated(final ShapeEvent event) {
    super.shapeUpdated(event);
    // The only events of interest are shape deselection (i.e. when one of the
    // region bounds has been moved in the crossplot.
    if (event.getEventType().equals(PlotEventType.SHAPE_DESELECTED)) {
      // Loop thru the region boundaries, looking for the shape.
      RegionsBoundary[] boundaries = RegionsBoundary.values();
      for (int i = 0; i < boundaries.length; i++) {
        if (event.getShape().equals(_regionBounds[i])) {
          // When found, check its region symmetry.
          if (_regionBounds[i].getPointCount() == 2) {
            checkRegionBoundSymmetry(i);
          }
          break;
        }
      }
    }
  }

  @Override
  public void modelSpaceUpdated(final ModelSpaceEvent event) {
    PlotEventType eventType = event.getEventType();
    // Only update the layer if the default bounds have updated.
    if (eventType.equals(PlotEventType.DEFAULT_BOUNDS_UPDATED)) {
      EllipseRegionsModel model = _crossplot.getEllipseRegionsModel();
      model.blockUpdate();
      RegionsBoundary[] boundaries = RegionsBoundary.values();
      for (int i = 0; i < boundaries.length; i++) {
        if (_regionBounds[i].getPointCount() == 2) {
          checkRegionBoundSymmetry(i);
        }
      }
      model.unblockUpdate();
      model.updated(Type.RegionBoundariesUpdated);
    }
  }

  private void checkRegionBoundSymmetry(final int index) {
    RegionsBoundary[] boundaries = RegionsBoundary.values();
    boolean redraw = false;
    boolean symmetric = _crossplot.getEllipseRegionsModel().getSymmetricRegions();
    double x;
    double y;
    double iXX = 0;
    double iYY = 0;
    double m = 0;
    double[] ox = new double[1];
    double[] oy = new double[1];
    double[] ix = new double[1];
    double[] iy = new double[1];
    IPlotPoint p1 = _regionBounds[index].getPoint(0);
    IPlotPoint p2 = _regionBounds[index].getPoint(1);
    double x1 = p1.getX();
    double y1 = p1.getY();
    double x2 = p2.getX();
    double y2 = p2.getY();
    double dx = x2 - x1;
    double dy = y2 - y1;
    IModelSpace modelSpace = _crossplot.getActiveModelSpace();
    ModelSpaceBounds defaultBounds = modelSpace.getDefaultBounds();
    if (index >= 2 && index <= 4 || index >= 7 && index <= 9) {
      // Adjust horizontal and diagonal lines.
      m = (y2 - y1) / (x2 - x1);
      if (index <= 4) { // Adjust lines left of origin.
        iXX = defaultBounds.getStartX();
      } else { // Adjust lines right of origin.
        iXX = defaultBounds.getEndX();
      }
      iYY = y1 + m * (iXX - x1);
      if (iYY < defaultBounds.getStartY()) {
        iYY = defaultBounds.getStartY();
        iXX = (iYY - y1) / m + x1;
      } else if (iYY > defaultBounds.getEndY()) {
        iYY = defaultBounds.getEndY();
        iXX = (iYY - y1) / m + x1;
      }
    } else {
      // Adjust vertical lines.
      m = (x2 - x1) / (y2 - y1);
      if (index <= 1) { // Adjust lines below origin.
        iYY = defaultBounds.getStartY();
      } else { // Adjust lines above origin.
        iYY = defaultBounds.getEndY();
      }
      iXX = x1 + m * (iYY - y1);
      if (iXX < defaultBounds.getStartX()) {
        iXX = defaultBounds.getStartX();
        iYY = y1 + (iXX - x1) / m;
      } else if (iXX > defaultBounds.getEndX()) {
        iXX = defaultBounds.getEndX();
        iYY = y1 + (iXX - x1) / m;
      }
    }
    x = iXX;
    y = iYY;
    p1.moveTo(x, y);

    //Point3d point;
    EllipseRegionsModel model = _crossplot.getEllipseRegionsModel();
    EllipseModel ellipseModel = model.getEllipseModel(EllipseType.Background);
    if (index >= 2 && index <= 4 || index >= 7 && index <= 9) {
      // Adjust horizontal and diagonal lines.
      m = (y2 - y1) / (x2 - x1);
      Point2D point = EllipseUtil.intersection(x, y, dx, dy, ellipseModel);
      //      if (index <= 4) { // Adjust lines left and above origin.
      //        point = EllipseUtil.intersection(true, m, y1 - m * x1, _crossplot.getEllipseLayer(EllipseType.Background).getStaticEllipse(), x2, y2);
      //      } else { // Adjust lines right and below origin.
      //        point = EllipseUtil.intersection(true, m, -y1 + m * x1, _crossplot.getEllipseLayer(EllipseType.Background).getStaticEllipse(), x2, y2);
      //      }
      iXX = point.getX();
      iYY = point.getY();
    } else {
      // Adjust vertical lines.
      m = (x2 - x1) / (y2 - y1);
      Point2D point = EllipseUtil.intersection(x, y, dx, dy, ellipseModel);
      //      if (index <= 4) { // Adjust lines below origin.
      //        point = EllipseUtil.intersection(false, m, x1 - m * y1, _crossplot.getEllipseLayer(EllipseType.Background).getStaticEllipse(), x2, y2);
      //      } else { // Adjust lines above origin.
      //        point = EllipseUtil.intersection(false, m, -x1 + m * y1, _crossplot.getEllipseLayer(EllipseType.Background).getStaticEllipse(), x2, y2);
      //      }
      iXX = point.getX();
      iYY = point.getY();
    }
    x = iXX;
    y = iYY;
    p2.moveTo(x, y);

    ox[0] = p1.getX();
    oy[0] = p1.getY();
    ix[0] = p2.getX();
    iy[0] = p2.getY();
    model.updateRegionsBoundaryModel(boundaries[index], ox[0], oy[0], ix[0], iy[0]);

    if (symmetric) {
      redraw = true;
      int symmetricIndex = (index + 5) % 10;
      p1 = _regionBounds[index].getPoint(0);
      p2 = _regionBounds[index].getPoint(1);
      x1 = 2 * ellipseModel.getCenterX() - p1.getX();
      y1 = 2 * ellipseModel.getCenterY() - p1.getY();
      x2 = 2 * ellipseModel.getCenterX() - p2.getX();
      y2 = 2 * ellipseModel.getCenterY() - p2.getY();
      _regionBounds[symmetricIndex].getPoint(0).moveTo(x1, y1);
      _regionBounds[symmetricIndex].getPoint(1).moveTo(x2, y2);
      ox[0] = x1;
      oy[0] = y1;
      ix[0] = x2;
      iy[0] = y2;
      model.updateRegionsBoundaryModel(boundaries[symmetricIndex], ox[0], oy[0], ix[0], iy[0]);
    }
  }
}
