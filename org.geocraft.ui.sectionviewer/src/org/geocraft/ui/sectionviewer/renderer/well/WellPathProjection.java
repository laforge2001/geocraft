/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.renderer.well;


/**
 * Utility class used for projecting a segment of a well path
 * onto a trace section plane.
 */
public class WellPathProjection {

  /** Flag indicating if a projection exists. */
  public boolean exists;

  /** The plane onto which the well path is being projected. */
  public ProjectionPlane section;

  /** The segment of the well path being projected. */
  public LineSegment wellpath;

  /** The projection of the well path segment onto the plane. */
  public LineSegment projection;

  public double s0, s1;

  public double w0, w1;

  public WellPathProjection() {
    exists = false;
    section = new ProjectionPlane();
    wellpath = new LineSegment();
    projection = new LineSegment();
  }

}
