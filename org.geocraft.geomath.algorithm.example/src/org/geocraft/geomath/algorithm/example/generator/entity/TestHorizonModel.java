/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.example.generator.entity;


import java.awt.Polygon;

import org.geocraft.core.model.datatypes.Point3d;


/**
 * This class is a model used to describe a set of undulating surfaces used to generate synthetic data (grids, volumes, well picks, etc).
 */
public final class TestHorizonModel {

  /** The value used to represent a null horizon value. */
  public static final float ZNULL = (float) (Math.random() * 1e10);

  /** The origin of the horizon corner points. */
  private Point3d _origin;

  /** The size of the geologic structure in the x direction. */
  private double _xStructureSize;

  /** The size of the geologic structure in the y direction. */
  private double _yStructureSize;

  /** The origin of the faulting. */
  private Point3d _faultOrigin;

  /** The width of the faulting. */
  private double _faultWidth;

  /** The length of the faulting. */
  private double _faultLength;

  /** The throws of the faulting. */
  private double _faultThrow = 300;

  /** The number of horizons. */
  private int _numHorizons;

  /** The polygon used to generate faulting in the horizons. */
  private Polygon _faultPolygon;

  /**
   * Constructs a horizon model used to generate synthetic data.
   * 
   * @param numHorizons the number of horizons to include in model.
   * @param origin the origin of the horizon corner point (only x,y are used...z is ignored).
   * @param xStructureSize the width of geologic structure in the x direction.
   * @param yStructureSize the width of geologic structure in the y direction.
   * @param faultOrigin the origin of the faulting (only x,y are used...z is ignored).
   * @param faultWidth the width of the faulting.
   * @param faultLength the length of the faulting.
   * 
   * Fault Polygon
   * 
   *                               2 
   *                              / \ 
   *                             /   \ 
   *                            /     \ 
   *                           /       \ 
   *                          /         \ 
   *                         1  -width - 3 
   *                          \         / 
   *                           \       / 
   *                            \     / 
   *                             \   / 
   *                              \ / 
   *                               0
   */
  public TestHorizonModel(final CultureGenerator cultureGenerator, final int numHorizons, final Point3d origin, final double xStructureSize, final double yStructureSize, final Point3d faultOrigin, final double faultWidth, final double faultLength) {
    _origin = origin;
    _xStructureSize = xStructureSize;
    _yStructureSize = yStructureSize;
    _numHorizons = numHorizons;
    _faultOrigin = faultOrigin;
    _faultWidth = faultWidth;
    _faultLength = faultLength;

    int[] x = new int[4];
    int[] y = new int[4];
    x[0] = (int) faultOrigin.getX();
    x[1] = (int) faultOrigin.getX() - (int) (faultWidth / 2);
    x[2] = (int) faultOrigin.getX();
    x[3] = (int) faultOrigin.getX() + (int) (faultWidth / 2);
    y[0] = (int) faultOrigin.getY() - (int) (faultLength / 2);
    y[1] = (int) faultOrigin.getY();
    y[2] = (int) faultOrigin.getY() + (int) (faultLength / 2);
    y[3] = (int) faultOrigin.getY();

    _faultPolygon = new Polygon(x, y, 4);

    cultureGenerator.addPolylineFeature("debug fault", new double[] { x[0], x[1], x[2], x[3], x[0] }, new double[] {
        y[0], y[1], y[2], y[3], y[0] });
  }

  /**
   * Returns the number of requested horizons in the model.
   * 
   * @return the number of horizons.
   */
  public int getNumHorizons() {
    return _numHorizons;
  }

  /**
   * Get the z value for a given horizon at a given x,y location.
   * 
   * @param horizonNumber the horizon number (0 to n) - the average depth to a horizon is 1000 * (horizonNumber +1).
   * @param x the x coordinate.
   * @param y the y coordinate.
   * @return the elevation of the surface (positive below sea level) at the specified x,y.
   */
  public float getHorizonZ(final int horizonNumber, final double x, final double y) {
    float z = ZNULL;
    if (!_faultPolygon.contains(x, y)) {
      z = (float) (1000 * (horizonNumber + 1) + 200 * Math.sin(2 * Math.PI * (x - _origin.getX()) / _xStructureSize)
          * Math.sin(2 * Math.PI * (y - _origin.getY()) / _yStructureSize));
      if (x >= _faultOrigin.getX() && x < _faultOrigin.getX() + 3 * _faultWidth) {
        z = z + (float) _faultThrow * getFactor(_faultOrigin.getY(), _faultLength, y)
            * getFactor(_faultOrigin.getX(), 3 * _faultWidth, x);
      }
    }
    return z;
  }

  /**
   * Get the thickness of the layer for a given horizon at a given x,y location.
   * 
   * @param horizonNumber the horizon number (0 to n) - the average depth to a horizon is 1000 * (horizonNumber +1).
   * @param x the x coordinate.
   * @param y the y coordinate.
   * @return the thickness of the layer at the specified x,y
   */
  public float getThickness(final int horizonNumber, final double x, final double y) {
    float z = 0;
    if (!_faultPolygon.contains(x, y)) {
      z = (float) (150 + 50 * Math.sin(2 * Math.PI * (x - _origin.getX()) / _xStructureSize));
    }
    return z;
  }

  /**
   * Get the seismic amplitude of the given horizon at a given x,y location.
   * 
   * @param horizonNumber the horizon number (0 to n) - the average depth to a horizon is -1000 * (hrzNum +1)
   * @param x the x coordinate.
   * @param y the y coordinate.
   * @return the thickness of the layer at the specified x,y
   */
  public float getAmplitude(final int horizonNumber, final double x, final double y) {
    float z = ZNULL;
    if (!_faultPolygon.contains(x, y)) {
      z = (float) (1 + Math.sin(2 * Math.PI * (y - _origin.getY()) / _yStructureSize));
    }
    return z;
  }

  private float getFactor(final double yOrigin, final double dist, final double y) {
    float factor = 0;
    if (y > yOrigin - dist / 2 && y < yOrigin + dist / 2) {
      factor = (float) (1 - Math.abs((y - yOrigin) / (dist / 2)));
    }
    return factor;
  }

  /**
   * Computes the azimuth (in degrees) at the x,y location of the given point.
   * 
   * @param x the x coordinate.
   * @param y the y coordinate.
   * @return the azimuth (in degrees).
   */
  public float getAzumith(final double x, final double y) {
    float azimuth = 0;
    double dzdx = 200 * 2 * Math.PI / _xStructureSize * Math.cos(2 * Math.PI * (x - _origin.getX()) / _xStructureSize)
        * Math.sin(2 * Math.PI * (y - _origin.getY()) / _yStructureSize);
    double dzdy = 200 * 2 * Math.PI / _yStructureSize * Math.cos(2 * Math.PI * (y - _origin.getY()) / _yStructureSize)
        * Math.sin(2 * Math.PI * (x - _origin.getX()) / _xStructureSize);
    azimuth = (float) (Math.atan2(dzdy, dzdx) * 180.0 / Math.PI);
    return azimuth;
  }

  /**
   * Computes the dip (in degrees) at the x,y location of the given point.
   * 
   * @param x the x coordinate.
   * @param y the y coordinate.
   * @return the dip (in degrees).
   */
  public float getDip(final double x, final double y) {
    float dip = 0;
    double dzdx = 200 * 2 * Math.PI / _xStructureSize * Math.cos(2 * Math.PI * (x - _origin.getX()) / _xStructureSize)
        * Math.sin(2 * Math.PI * (y - _origin.getY()) / _yStructureSize);
    double dzdy = 200 * 2 * Math.PI / _yStructureSize * Math.cos(2 * Math.PI * (y - _origin.getY()) / _yStructureSize)
        * Math.sin(2 * Math.PI * (x - _origin.getX()) / _xStructureSize);
    double dz = Math.sqrt(dzdx * dzdx + dzdy * dzdy);
    dip = (float) (Math.atan2(dz, 1) * 180.0 / Math.PI);
    return dip;
  }
}
