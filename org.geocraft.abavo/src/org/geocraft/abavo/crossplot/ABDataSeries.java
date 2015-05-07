/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.internal.abavo.ServiceComponent;
import org.geocraft.math.regression.IRegressionMethodService;
import org.geocraft.math.regression.RegressionData;
import org.geocraft.math.regression.RegressionDataStatistics;
import org.geocraft.math.regression.RegressionMethodDescription;
import org.geocraft.math.regression.RegressionStatistics;
import org.geocraft.math.regression.RegressionType;


/**
 * Define a data series for the A vs B crossplot.
 */
public class ABDataSeries {

  /** The series name. */
  private String _name;

  /** The series id (1-10, or 0 to assign based on crossplot). */
  private int _id;

  /** The number of points. */
  private int _numPoints;

  /** The array of A values. */
  private float[] _a;

  /** The array of B values. */
  private float[] _b;

  /** The array of 3d points. */
  private Point3d[] _points;

  /** The x,y unit. */
  private final Unit _xyUnit;

  /** The z unit. */
  private final Unit _zUnit;

  /** The domain type (Time or Depth). */
  private final Domain _domain;

  /** The minimum A value. */
  private float _minimumA;

  /** The maximum A value. */
  private float _maximumA;

  /** The minimum B value. */
  private float _minimumB;

  /** The maximum B value. */
  private float _maximumB;

  /** The minimum z value. */
  private float _minimumZ;

  /** The maximum z value. */
  private float _maximumZ;

  /** The map of regressions. */
  private final Map<String, RegressionStatistics> _regressions;

  /** The regression data statistics. */
  private final RegressionDataStatistics _regressionDataStats;

  /**
   * Generates an array of 3d points from arrays of x,y,z values.
   * @param numPoints the number of points.
   * @param x the array of x values.
   * @param y the array of y values.
   * @param z the array of z values.
   * @return the array of 3d points.
   */
  private static Point3d[] getPoints(final int numPoints, final double[] x, final double[] y, final float[] z) {
    Point3d[] points = new Point3d[numPoints];
    if (x.length == 0 || y.length == 0 || z.length == 0) {
      for (int i = 0; i < numPoints; i++) {
        points[i] = new Point3d(Double.NaN, Double.NaN, Double.NaN);
      }
    } else {
      for (int i = 0; i < numPoints; i++) {
        points[i] = new Point3d(x[i], y[i], z[i]);
      }
    }
    return points;
  }

  /**
   * Creates an array of NaN 3d points.
   * @param numPoints the number of points.
   * @return the array of 3d points.
   */
  private static Point3d[] createEmptyPoints(final int numPoints) {
    Point3d[] points = new Point3d[numPoints];
    for (int i = 0; i < numPoints; i++) {
      points[i] = new Point3d(Double.NaN, Double.NaN, Double.NaN);
    }
    return points;
  }

  /**
   * The parameterized constructor, without any x,y,z coordinate values.
   * @param name the data series name.
   * @param id the data series id.
   * @param numPoints the number of points in the data series arrays.
   * @param a the array of A values.
   * @param b the array of B values.
   */
  public ABDataSeries(final String name, final int id, final int numPoints, final float[] a, final float[] b, final RegressionType regressionType) {
    this(name, id, numPoints, a, b, createEmptyPoints(numPoints), Unit.UNDEFINED, Unit.UNDEFINED, Domain.TIME, regressionType);
  }

  /**
   * The parameterized constructor, using x,y,z arrays for the coordinate values.
   * @param name the data series name.
   * @param id the data series id.
   * @param numPoints the number of points in the data series arrays.
   * @param a the array of A values.
   * @param b the array of B values.
   * @param x the array of x values.
   * @param y the array of y values.
   * @param z the array of z values.
   * @param xyUnit the x,y unit.
   * @param zUnit the z unit.
   * @param domainType the domain type (Time or Depth).
   */
  public ABDataSeries(final String name, final int id, final int numPoints, final float[] a, final float[] b, final double[] x, final double[] y, final float[] z, final Unit xyUnit, final Unit zUnit, final Domain domain, final RegressionType regressionType) {
    this(name, id, numPoints, a, b, getPoints(numPoints, x, y, z), xyUnit, zUnit, domain, regressionType);
  }

  /**
   * The parameterized constructor, using 3d points for the coordinate values.
   * @param name the data series name.
   * @param id the data series id.
   * @param numPoints the number of points in the data series arrays.
   * @param a the array of A values.
   * @param b the array of B values.
   * @param points the array of 3d points.
   * @param xyUnit the x,y unit.
   * @param zUnit the z unit.
   * @param domainType the domain type (Time or Depth).
   */
  public ABDataSeries(final String name, final int id, final int numPoints, final float[] a, final float[] b, final Point3d[] points, final Unit xyUnit, final Unit zUnit, final Domain domain, final RegressionType regressionType) {
    _name = name;
    _id = id;
    _regressions = Collections.synchronizedMap(new HashMap<String, RegressionStatistics>());
    clear();
    _numPoints = numPoints;
    _a = new float[numPoints];
    _b = new float[numPoints];
    System.arraycopy(a, 0, _a, 0, numPoints);
    System.arraycopy(b, 0, _b, 0, numPoints);
    _points = new Point3d[numPoints];
    for (int i = 0; i < numPoints; i++) {
      _points[i] = new Point3d(points[i]);
    }
    _xyUnit = xyUnit;
    _zUnit = zUnit;
    _domain = domain;
    _minimumA = a[0];
    _maximumA = a[0];
    _minimumB = b[0];
    _maximumB = b[0];
    _minimumZ = (float) points[0].getZ();
    _maximumZ = (float) points[0].getZ();
    for (int i = 0; i < numPoints; i++) {
      _minimumA = Math.min(_minimumA, a[i]);
      _maximumA = Math.max(_maximumA, a[i]);
      _minimumB = Math.min(_minimumB, b[i]);
      _maximumB = Math.max(_maximumB, b[i]);
      _minimumZ = Math.min(_minimumZ, (float) points[i].getZ());
      _maximumZ = Math.max(_maximumZ, (float) points[i].getZ());
    }
    // Compute and store the regressions.
    RegressionData data = new RegressionData(a, b);
    _regressionDataStats = data.getStatistics();
    IRegressionMethodService regressionService = ServiceComponent.getRegressionMethodService();
    RegressionMethodDescription[] methods = regressionService.getRegressionMethods();
    for (RegressionMethodDescription method : methods) {
      RegressionStatistics regression = regressionService.compute(method, regressionType, data);
      if (regression != null) {
        _regressions.put(method.getAcronym(), regression);
      }
    }
  }

  /**
   * Clears the data arrays.
   */
  public void clear() {
    _numPoints = 0;
    _a = new float[0];
    _b = new float[0];
    _points = new Point3d[0];
    _regressions.clear();
  }

  /**
   * Gets to point count.
   * @return the number of points in the series.
   */
  public int getNumPoints() {
    return _numPoints;
  }

  /**
   * Gets to point count.
   * @return the name of the series.
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets to point count.
   * @param name the name for the series.
   */
  public void setName(final String name) {
    _name = name;
  }

  /**
   * Gets to series index.
   * @return the series index;
   */
  public int getId() {
    return _id;
  }

  /**
   * Sets to series index.
   * @param id the series id.
   */
  public void setId(final int id) {
    if (_name == null || _name.length() == 0) {
      setName("Series #" + id);
    }
    _id = id;
  }

  /**
   * Gets the array of A values (NOT a copy).
   * @return the array of A values.
   */
  public float[] getA() {
    return _a;
  }

  /**
   * Gets the array of B values (NOT a copy).
   * @return the array of B values.
   */
  public float[] getB() {
    return _b;
  }

  /**
   * Gets the array of 3d points (NOT a copy).
   * @return the array of 3d points.
   */
  public Point3d[] getPoints() {
    return _points;
  }

  /**
   * Gets the minimum A value.
   * @return the minimum A value.
   */
  public float getMinimumA() {
    return _minimumA;
  }

  /**
   * Gets the maximum A value.
   * @return the maximum A value.
   */
  public float getMaximumA() {
    return _maximumA;
  }

  /**
   * Gets the minimum B value.
   * @return the minimum B value.
   */
  public float getMinimumB() {
    return _minimumB;
  }

  /**
   * Gets the maximum B value.
   * @return the maximum B value.
   */
  public float getMaximumB() {
    return _maximumB;
  }

  /**
   * Gets the minimum z value.
   * @return the minimum z value.
   */
  public float getMinimumZ() {
    return _minimumZ;
  }

  /**
   * Gets the maximum z value.
   * @return the maximum z value.
   */
  public float getMaximumZ() {
    return _maximumZ;
  }

  /**
   * Gets the x,y unit.
   * @return the x,y unit.
   */
  public Unit getXYUnit() {
    return _xyUnit;
  }

  /**
   * Gets the z unit.
   * @return the z unit.
   */
  public Unit getZUnit() {
    return _zUnit;
  }

  /**
   * Gets the domain type.
   * @return the domain type.
   */
  public Domain getDomainType() {
    return _domain;
  }

  /**
   * Gets the data series regression for the method of interest.
   * @param method the regression method.
   */
  public RegressionStatistics getRegression(final RegressionMethodDescription method) {
    return getRegression(method.getAcronym());
  }

  /**
   * Gets the data series regression for the method of interest.
   * @param methodAcronym the acronym of the regression method of interest.
   */
  public RegressionStatistics getRegression(final String methodAcronym) {
    return _regressions.get(methodAcronym);
  }

  /**
   * Returns the regression data statistics for this series.
   * @return the regression data statistics for this series.
   */
  public RegressionDataStatistics getRegressionDataStatistics() {
    return _regressionDataStats;
  }
}
