/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.polygon;


import org.geocraft.abavo.classbkg.IRegionsClassifier;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.ui.plot.util.PolygonRegionsUtil;


/**
 * TODO
 */
public class PolygonRegionsClassifier implements IRegionsClassifier {

  /** The number of polygons. */
  protected int _numPolygons;

  /** The arrays of polygon x coordinates. */
  protected double[][] _polygonXs;

  /** The arrays of polygon y coordinates. */
  protected double[][] _polygonYs;

  /** The array of polygon class background values. */
  protected float[] _polygonValues;

  /**
   * The default constructor.
   * @param polygonModel the polygon regions model.
   */
  public PolygonRegionsClassifier(final PolygonRegionsModel polygonModel) {
    this(polygonModel.getPolygonModels());
  }

  public PolygonRegionsClassifier(final PolygonModel[] polygonModels) {
    int numPolygons = polygonModels.length;
    _polygonXs = new double[numPolygons][];
    _polygonYs = new double[numPolygons][];
    for (int i = 0; i < numPolygons; i++) {
      int numPoints = polygonModels[i].getNumPoints();
      _polygonXs[i] = new double[numPoints];
      _polygonYs[i] = new double[numPoints];
      for (int j = 0; j < numPoints; j++) {
        Point3d point = polygonModels[i].getPoint(j);
        _polygonXs[i][j] = point.getX();
        _polygonYs[i][j] = point.getY();
      }
    }
    _polygonValues = new float[numPolygons];
    for (int i = 0; i < numPolygons; i++) {
      _polygonValues[i] = polygonModels[i].getValue();
    }
    if (_polygonXs.length != _polygonYs.length || _polygonXs.length != _polygonValues.length) {
      throw new RuntimeException("Size of the polygon arrays must match.");
    }
    _numPolygons = _polygonValues.length;
  }

  public String getName() {
    return "Polygon Regions Classification";
  }

  /**
   * Processes the A,B coordinate.
   * @param a the A coordinate.
   * @param b the B coordinate.
   * @return the class background value.
   */
  public double processAB(final double a, final double b) {
    double value = Double.NaN;
    for (int i = 0; i < _numPolygons; i++) {
      int npts = _polygonXs[i].length;
      if (npts > 2 && PolygonRegionsUtil.isPointInside(npts, _polygonXs[i], _polygonYs[i], a, b)) {
        value = _polygonValues[i];
      }
    }
    return value;
  }

}
