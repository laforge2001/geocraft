/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.datatypes;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geocraft.core.model.base.ValueObject;


public class PolylinePick extends ValueObject {

  /** The z domain in which the pick exists (TIME or DEPTH). */
  private final Domain _domain;

  /** The collection of 3D points of the pick. */
  private final List<Point3d> _points;

  /**
   * Constructs an immutable polyline pick value object.
   * Note: a defensive copy of the points is made so that subsequent changes to the input collection
   * will not affect this pick object.
   * 
   * @param displayName the display name of the pick.
   * @param domain the z domain of the pick points (TIME or DEPTH).
   * @param points the collection of pick points.
   */
  public PolylinePick(final String displayName, final Domain domain, final List<Point3d> points) {
    super(displayName);
    _domain = domain;
    _points = Collections.synchronizedList(new ArrayList<Point3d>());
    for (Point3d point : points) {
      _points.add(point);
    }
  }

  /**
   * Returns the z domain of the pick (TIME or DEPTH).
   */
  public Domain getDomain() {
    return _domain;
  }

  /**
   * Returns the number of points in the pick.
   */
  public int getNumPoints() {
    return _points.size();
  }

  /**
   * Returns the array of points in the pick.
   * Note: the returned array is a defensive copy and changes to the points in the
   * array will not affect the pick object.
   */
  public Point3d[] getPoints() {
    return _points.toArray(new Point3d[0]);
  }

}
