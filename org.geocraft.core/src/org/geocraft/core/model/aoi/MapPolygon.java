/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.aoi;


import java.awt.geom.Path2D;


/**
 * This class defines a map polygon for using within the <code>MapPolygonAOI</code> entity.
 * <p>
 * It consists of a polygonal area and a type parameter that flags the polygon as inclusive or exclusive.
 */
public class MapPolygon {

  /** Enumeration of the types of polygons. */
  public static enum Type {
    /** Points inside are included. */
    INCLUSIVE,
    /** Points inside are excluded. */
    EXCLUSIVE;
  }

  /** The path of the polygonal area. */
  private Path2D.Double _path;

  /** The polygon type: INCLUSIVE or EXCLUSIVE. */
  private final Type _type;

  /**
   * Constructs a map polygon for use in an AOI.
   * @param type the type of polygon: INCLUSIVE or EXCLUSIVE.
   * @param xs the array of x coordinates for the polygon.
   * @param ys the array of y coordinates for the polygon.
   */
  public MapPolygon(final Type type, final double[] xs, final double[] ys) {
    // Check that the input arrays are non-null and of equal length.
    if (xs == null || ys == null) {
      throw new RuntimeException("Invalid x,y coordinate arrays: xs=" + xs + " ys=" + ys + ".");
    }
    if (xs.length != ys.length) {
      throw new RuntimeException("Invalid x,y coordinate arrays: " + xs.length + " != " + ys.length + ".");
    }

    // Store the polygon type and path.
    _type = type;
    setPath(xs, ys);
  }

  /**
   * Returns the path of the polygon.
   * 
   * @return the polygon path.
   */
  public synchronized Path2D.Double getPath() {
    return _path;
  }

  /**
   * Returns the type of the polygon.
   * <p>
   * This will be either <code>INCLUSIVE</code> or <code>EXCLUSIVE</code>.
   * 
   * @return the polygon type.
   */
  public synchronized Type getType() {
    return _type;
  }

  /**
   * Returns a flag indicating of the polygon type is <code>INCLUSIVE</code>.
   * 
   * @return <i>true</i> if <code>INCLUSIVE</code>; <i>false</i> if <code>EXCLUSIVE</code>.
   */
  public synchronized boolean isInclusive() {
    return _type.equals(Type.INCLUSIVE);
  }

  /**
   * Returns a flag indicating of the polygon type is <code>EXCLUSIVE</code>.
   * 
   * @return <i>true</i> if <code>EXCLUSIVE</code>; <i>false</i> if <code>INCLUSIVE</code>.
   */
  public synchronized boolean isExclusive() {
    return _type.equals(Type.EXCLUSIVE);
  }

  /**
   * Returns a flag indicating if the given x,y coordinate is contained inside the polygon.
   * <p>
   * Note: This is a spatial check only, and the polygon type does not affect the result.
   * 
   * @param x the x coordinate.
   * @param y the y coordinate.
   * @return <i>true</i> if contained; <i>false</i> if not.
   */
  public synchronized boolean contains(final double x, final double y) {
    return _path.contains(x, y);
  }

  public synchronized void setPath(final double[] xs, final double[] ys) {
    _path = new Path2D.Double();
    _path.moveTo((float) xs[0], (float) ys[0]);
    for (int i = 1; i < xs.length; i++) {
      _path.lineTo((float) xs[i], (float) ys[i]);
    }
  }
}
