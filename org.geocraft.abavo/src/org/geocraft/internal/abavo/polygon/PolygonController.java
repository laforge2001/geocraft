/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.abavo.polygon;


import org.geocraft.abavo.crossplot.ABavoCrossplot;
import org.geocraft.abavo.polygon.PolygonRegionsModel;
import org.geocraft.abavo.polygon.PolygonRegionsModelEvent;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.ui.plot.event.ShapeEvent;
import org.geocraft.ui.plot.listener.IPlotShapeListener;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotShape;


public class PolygonController implements IPlotShapeListener {

  private ABavoCrossplot _crossplot;

  private PolygonRegionsModel _polygonRegionsModel;

  private final int _polygonIndex;

  public PolygonController(ABavoCrossplot crossplot, PolygonRegionsModel polygonRegionsModel, int polygonIndex) {
    _crossplot = crossplot;
    _polygonRegionsModel = polygonRegionsModel;
    _polygonIndex = polygonIndex;
  }

  public void shapeUpdated(final ShapeEvent event) {
    IPlotShape shape = event.getShape();
    int numPoints = shape.getPointCount();
    Point3d[] points = new Point3d[numPoints];
    for (int i = 0; i < numPoints; i++) {
      IPlotPoint plotPoint = shape.getPoint(i);
      points[i] = new Point3d(plotPoint.getX(), plotPoint.getY(), plotPoint.getZ());
    }
    _polygonRegionsModel.getPolygonModel(_polygonIndex).setPoints(points, false);
    if (_polygonRegionsModel.getSymmetricRegions()) {
      int symmetryIndex = PolygonRegionsModel.NUMBER_OF_POLYGONS - 1 - _polygonIndex;
      Point3d[] symmetryPoints = _crossplot.computeSymmetricPoints(points);
      _polygonRegionsModel.getPolygonModel(symmetryIndex).setPoints(symmetryPoints, false);
      _crossplot.polygonModelUpdated(new PolygonRegionsModelEvent(PolygonRegionsModelEvent.Type.PolygonsUpdated,
          _polygonRegionsModel, new int[] { symmetryIndex }));
    }
  }

}
