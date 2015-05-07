/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.renderer.well;


/**
 * Defines a vertical plane onto which a well path is projected.
 * The x,y coordinates define the size and orientation of the plane
 * and the z coordinate define the vertical extent of the plane.
 */
public class ProjectionPlane {

  /** The starting x-coordinate of the projection plane. */
  public double x0;

  /** The starting y-coordinate of the projection plane. */
  public double y0;

  /** The starting z-coordinate of the projection plane. */
  public double z0;

  /** The ending x-coordinate of the projection plane. */
  public double x1;

  /** The ending y-coordinate of the projection plane. */
  public double y1;

  /** The ending z-coordinate of the projection plane. */
  public double z1;

  /** The delta-z of the projection plane. */
  public double dx;

  /** The delta-y of the projection plane. */
  public double dy;

  /** The delta-z of the projection plane. */
  public double dz;
}
