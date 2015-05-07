/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.renderer.well;


/**
 * Defines a line segment in 3D space.
 * This includes the starting and ending x,y,z coordinates,
 * as well as the delta x,y,z of the segment.
 */
public class LineSegment {

  /** The starting x-coordinate of the line segment. */
  public double x0;

  /** The starting y-coordinate of the line segment. */
  public double y0;

  /** The starting z-coordinate of the line segment. */
  public double z0;

  /** The ending x-coordinate of the line segment. */
  public double x1;

  /** The ending y-coordinate of the line segment. */
  public double y1;

  /** The ending z-coordinate of the line segment. */
  public double z1;

  /** The delta-z of the line segment. */
  public double dx;

  /** The delta-y of the line segment. */
  public double dy;

  /** The delta-z of the line segment. */
  public double dz;
}
