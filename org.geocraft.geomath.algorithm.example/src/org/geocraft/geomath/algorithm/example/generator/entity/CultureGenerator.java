/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.example.generator.entity;


import java.sql.Timestamp;
import java.util.Map;

import org.geocraft.core.model.culture.Layer;
import org.geocraft.core.model.culture.LayerType;
import org.geocraft.core.model.culture.PointFeature;
import org.geocraft.core.model.culture.PolygonFeature;
import org.geocraft.core.model.culture.PolylineFeature;
import org.geocraft.core.model.culture.SimplePolygon;
import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.CoordinateSystem;
import org.geocraft.core.model.datatypes.DataType;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.repository.specification.TypeSpecification;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.geomath.algorithm.example.generator.TestDataGenerator;


public class CultureGenerator {

  Layer _leaseBlocks; // TODO - suggests that the children are value objects?

  Layer _pipelines;

  Layer _platforms;

  IRepository _repository;

  Point3d _origin;

  CoordinateSystem _coordSys;

  public CultureGenerator(final IRepository repository, final Point3d origin, final CoordinateSystem coordSys) {
    _repository = repository;
    _origin = origin;
    _coordSys = coordSys;
  }

  public Layer getPlatformLayer() {
    if (_platforms == null) {
      DataType[] platformAttributeTypes = { DataType.STRING, DataType.INT, DataType.DATE };
      String[] _platformAttributeNames = { "Operator", "NumSlots", "CommisionDate" };
      _platforms = new Layer("Platforms", LayerType.POINT, _platformAttributeNames, platformAttributeTypes);
      _repository.add(_platforms);
    }
    return _platforms;
  }

  public Layer getLeaseLayer() {
    if (_leaseBlocks == null) {
      DataType[] leaseAttributeTypes = { DataType.STRING, DataType.DATE };
      String[] _leaseAttributeNames = { "Operator", "ExpirationDate" };
      _leaseBlocks = new Layer("LeaseBlocks", LayerType.POLYGON, _leaseAttributeNames, leaseAttributeTypes);
      _repository.add(_leaseBlocks);
    }
    return _leaseBlocks;
  }

  public Layer getPipelineLayer() {
    if (_pipelines == null) {
      String[] pipelineAttributeNames = { "PipeLineOperator", "Diameter", "FluidType" };
      DataType[] pipelineAttributeTypes = { DataType.STRING, DataType.FLOAT, DataType.STRING };
      _pipelines = new Layer("Pipelines", LayerType.POLYLINE, pipelineAttributeNames, pipelineAttributeTypes);
      _repository.add(_pipelines);
    }
    return _pipelines;
  }

  public Layer getLayer(final String name, final String[] attributeNames, final DataType[] attributeTypes) {
    Layer layer = null;

    Map<String, Object> layers = _repository.get(new TypeSpecification(Layer.class));
    for (Object obj : layers.values()) {
      Layer candidate = (Layer) obj;
      if (candidate.getDisplayName().equals(name)) {
        layer = candidate;
      }
    }

    if (layer == null) {
      layer = new Layer(name, LayerType.POLYLINE, attributeNames, attributeTypes);
      _repository.add(layer);
    }

    return layer;
  }

  public void addPointFeature(final String name, final double width, final double height, final ILogger logger) {

    PointFeature pointFeature = new PointFeature(name, getPlatformLayer());

    try {
      getPlatformLayer().addFeature(pointFeature);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }

    Point3d[] points = new Point3d[(int) (Math.random() * 2 + 1)];

    for (int i = 0; i < points.length; i++) {
      points[i] = makeRandomXyPoint(width, height);
    }

    pointFeature.setPoints(CoordinateSeries.createDirect(points, _coordSys));

    pointFeature.setIs3D(true);

    pointFeature.setIsMeasured(false);

    pointFeature.setLastModifiedDate(new Timestamp(System.currentTimeMillis()));

    int numSlots = (int) ((Math.random() + .01) * 30);
    Timestamp date = new Timestamp((long) (Math.random() * 2117345636));

    try {
      pointFeature.setAttributeValue("Operator", TestDataGenerator.getRandomOperator());
      pointFeature.setAttributeValue("NumSlots", Integer.toString(numSlots));
      pointFeature.setAttributeValue("CommisionDate", date.toString());
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }

    _repository.add(pointFeature);
  }

  public void addPolylineFeature(final String name, final double width, final double height, final ILogger logger) {

    PolylineFeature polylineFeature = new PolylineFeature(name, getPipelineLayer());

    try {
      getPipelineLayer().addFeature(polylineFeature);
    } catch (Exception e) {
      logger.error(e.toString());
    }

    polylineFeature.setNumParts((int) (Math.random() * 9 + 1));

    int countPoints = 0;
    int[] numPointsPerPart = new int[polylineFeature.getNumParts()];

    for (int i = 0; i < polylineFeature.getNumParts(); i++) {
      numPointsPerPart[i] = (int) (Math.random() * 50 + 25);
      countPoints = countPoints + numPointsPerPart[i];
    }
    polylineFeature.setNumPointsPerPart(numPointsPerPart);

    Point3d[] points = new Point3d[countPoints];

    countPoints = 0;

    for (int i = 0; i < polylineFeature.getNumParts(); i++) {

      points[countPoints] = makeRandomXyPoint(width, height);
      countPoints++;

      double prevSlopeX = (Math.random() * 2 - 1) * Math.random() * 2;
      double prevSlopeY = (Math.random() * 2 - 1) * Math.random() * 2;
      double prevSlopeZ = 0;

      for (int j = 1; j < numPointsPerPart[i]; j++) {

        double x = prevSlopeX * (Math.random() * 45 + 5) + points[countPoints - 1].getX();
        double y = prevSlopeY * (Math.random() * 45 + 5) + points[countPoints - 1].getY();
        double z = prevSlopeZ * (Math.random() * 45 + 5) + points[countPoints - 1].getZ();

        points[countPoints] = new Point3d(x, y, z);

        countPoints++;
        prevSlopeX = prevSlopeX + (Math.random() * 2 - 1) * prevSlopeX / 3;
        prevSlopeY = prevSlopeY + (Math.random() * 2 - 1) * prevSlopeY / 3;
        prevSlopeZ = prevSlopeZ + (Math.random() * 2 - 1) * prevSlopeZ / 25;
      }
    }
    polylineFeature.setPoints(CoordinateSeries.createDirect(points, _coordSys));

    polylineFeature.setIs3D(true);

    polylineFeature.setIsMeasured(false);

    String[] fluidType = { "oil", "gas", "oil/gas" };

    int iFluid = (int) (Math.random() * 3);
    float diameter = (float) ((Math.random() + .01) * 30);

    try {
      polylineFeature.setAttributeValue("PipeLineOperator", TestDataGenerator.getRandomOperator());
      polylineFeature.setAttributeValue("Diameter", Float.toString(diameter));
      polylineFeature.setAttributeValue("FluidType", fluidType[iFluid]);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }

    _repository.add(polylineFeature);
  }

  public void addPolylineFeature(final String name, final double[] x, final double[] y) {

    Layer layer = getLayer(name, new String[] {}, new DataType[] {});
    PolylineFeature polylineFeature = new PolylineFeature(name, layer);
    layer.addFeature(polylineFeature);

    polylineFeature.setNumParts(1);
    polylineFeature.setNumPointsPerPart(new int[] { x.length });

    Point3d[] points = new Point3d[x.length];

    for (int i = 0; i < x.length; i++) {
      points[i] = new Point3d(x[i], y[i], 0);
    }

    polylineFeature.setPoints(CoordinateSeries.createDirect(points, _coordSys));

    polylineFeature.setIs3D(true);

    polylineFeature.setIsMeasured(false);

    _repository.add(polylineFeature);
  }

  public void addPolygonFeature(final String name, final double width, final double height, final ILogger logger) {

    Layer layer = getLeaseLayer();
    PolygonFeature polygonFeature = new PolygonFeature(name, layer);

    layer.addFeature(polygonFeature);

    polygonFeature.setIs3D(true);

    polygonFeature.setIsMeasured(false);

    int numPolygons = 1 + (int) (Math.random() * 4);

    Timestamp date = new Timestamp((long) (Math.random() * 2117345636));

    try {
      polygonFeature.setAttributeValue("Operator", TestDataGenerator.getRandomOperator());
      polygonFeature.setAttributeValue("ExpirationDate", date.toString());
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }

    SimplePolygon[] simplePolygons = new SimplePolygon[numPolygons];

    for (int i = 0; i < numPolygons; i++) {
      simplePolygons[i] = makeSimplePolygon(i, width, height, polygonFeature);
    }
    polygonFeature.addToPolygons(simplePolygons);
    _repository.add(polygonFeature);
  }

  private SimplePolygon makeSimplePolygon(final int polyFn, final double width, final double height,
      final PolygonFeature parentPolyFeature) {

    SimplePolygon simplePolygon = new SimplePolygon("polygon" + polyFn);

    double polygonWidth = Math.random() * width * 5280 / (Math.random() * 8 + 2);
    double polygonHeight = Math.random() * height * 5280 / (Math.random() * 8 + 2);

    Point3d origin = makeRandomXyPoint(width, height);
    Point3d[] boundaryPoints = TestDataGenerator.createBoundary(origin.getX(), origin.getY(), polygonWidth,
        polygonHeight);

    simplePolygon.setPoints(CoordinateSeries.createDirect(boundaryPoints, _coordSys));
    simplePolygon.setIs3D(true);
    simplePolygon.setIsHole(false);

    SimplePolygon hole = new SimplePolygon("hole1");

    double holeWidth = (Math.random() * 0.7 + 0.2) * polygonWidth;
    double holeHeight = (Math.random() * 0.7 + 0.2) * polygonHeight;
    double polygonCenterX = (boundaryPoints[0].getX() + boundaryPoints[1].getX()) / 2;
    double polygonCenterY = (boundaryPoints[0].getY() + boundaryPoints[3].getY()) / 2;

    Point3d[] holePoint3ds = TestDataGenerator.createBoundary(polygonCenterX - holeWidth / 2, polygonCenterY
        - holeHeight / 2, holeWidth, holeHeight);

    hole.setPoints(CoordinateSeries.createDirect(holePoint3ds, _coordSys));
    hole.setIs3D(true);
    hole.setIsHole(true);

    SimplePolygon[] holeArray = { hole };

    simplePolygon.addToHoles(holeArray);

    simplePolygon.setPolygonFeature(parentPolyFeature);

    return simplePolygon;
  }

  /**
   * @param width
   * @param height
   * @return
   */
  public Point3d[] createBoundaryXX(final double xmin, final double ymin, final double width, final double height) {
    Point3d[] boundaryPoints = new Point3d[4];

    double xmax = xmin + width;
    double ymax = ymin - height;
    double z = 0;

    boundaryPoints[0] = new Point3d(xmin, ymin, z);
    boundaryPoints[1] = new Point3d(xmax, ymin, z);
    boundaryPoints[2] = new Point3d(xmax, ymax, z);
    boundaryPoints[3] = new Point3d(xmin, ymax, z);

    return boundaryPoints;
  }

  private Point3d makeRandomXyPoint(final double width, final double height) {

    double x = _origin.getX() + (Math.random() * 2 - 1) * width * 5280;
    double y = _origin.getY() + (Math.random() * 2 - 1) * height * 5280;
    return new Point3d(x, y, 0.0);

  }
}
