/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.ui.sectionviewer;


import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.ui.sectionviewer.renderer.well.LineSegment;
import org.geocraft.ui.sectionviewer.renderer.well.ProjectionLine;
import org.geocraft.ui.sectionviewer.renderer.well.ProjectionPlane;
import org.geocraft.ui.sectionviewer.renderer.well.ProjectionPoint;
import org.geocraft.ui.sectionviewer.renderer.well.TriangleIntersection;
import org.geocraft.ui.sectionviewer.renderer.well.WellPathProjection;


public class SectionViewRendererUtil {

  /**
   * @param segment
   * @param plane
   * @param segmentFront
   * @param segmentBehind
   * @param criticalDistance
   * @param showAll
   * @return
   */
  public static int projectWellPathOntoSection(final LineSegment pathSegment, final ProjectionPlane plane,
      final WellPathProjection[] pathProjectionFront, final WellPathProjection[] pathProjectionBehind,
      final float criticalDistance, final boolean showAll) {
    double rtn_dist;
    double xi, yi, zi;
    double w0, w1;
    double pcnt, dw;
    double Lx, Ly;
    double t, v;
    double Mx, My;
    double Sx, Sy;
    double t0, t1;
    double ww0, ww1;
    double xc, yc, zc;
    double x0, y0, x1, y1;
    double d0, d1, dd;
    double s, w, p;
    double ss0, ss1;
    double numer, denom;
    WellPathProjection pathProjection = new WellPathProjection();
    ProjectionPoint point = new ProjectionPoint();
    ProjectionLine line = new ProjectionLine();

    // Set initial projection parameters.
    pathProjection.exists = false;
    pathProjection.section = plane;
    pathProjection.wellpath = pathSegment;
    pathProjectionFront[0].exists = false;
    pathProjectionBehind[0].exists = false;

    // Project start of well path segment onto section plane.
    denom = pathProjection.section.dx * pathProjection.section.dx + pathProjection.section.dy
        * pathProjection.section.dy;

    numer = (pathProjection.wellpath.x0 - pathProjection.section.x0) * pathProjection.section.dx
        + (pathProjection.wellpath.y0 - pathProjection.section.y0) * pathProjection.section.dy;
    pathProjection.s0 = numer / denom;

    numer = (pathProjection.wellpath.x1 - pathProjection.section.x0) * pathProjection.section.dx
        + (pathProjection.wellpath.y1 - pathProjection.section.y0) * pathProjection.section.dy;
    pathProjection.s1 = numer / denom;

    numer = (pathProjection.section.y0 - pathProjection.wellpath.y0) * pathProjection.section.dx
        + (pathProjection.wellpath.x0 - pathProjection.section.x0) * pathProjection.section.dy;
    t0 = numer / denom;
    numer = (pathProjection.section.y0 - pathProjection.wellpath.y1) * pathProjection.section.dx
        + (pathProjection.wellpath.x1 - pathProjection.section.x0) * pathProjection.section.dy;
    t1 = numer / denom;

    // Check if both projections are completely outside section plane.
    if (pathProjection.s0 < 0.0 && pathProjection.s1 < 0.0) {
      return 1;
    }
    if (pathProjection.s0 > 1.0 && pathProjection.s1 > 1.0) {
      return 2;
    }

    // Check start of well path projection.
    s = pathProjection.s0;
    if (s < 0.0) {
      s = 0.0;
    }
    if (s > 1.0) {
      s = 1.0;
    }
    pathProjection.projection.x0 = pathProjection.section.x0 + s * pathProjection.section.dx;
    pathProjection.projection.y0 = pathProjection.section.y0 + s * pathProjection.section.dy;
    pathProjection.s0 = s;
    ss0 = s;

    // Check end of well path projection.
    s = pathProjection.s1;
    if (s < 0.0) {
      s = 0.0;
    }
    if (s > 1.0) {
      s = 1.0;
    }
    pathProjection.projection.x1 = pathProjection.section.x0 + s * pathProjection.section.dx;
    pathProjection.projection.y1 = pathProjection.section.y0 + s * pathProjection.section.dy;
    pathProjection.s1 = s;
    ss1 = s;

    // Calculate the current dx,dy for projection.
    pathProjection.projection.dx = pathProjection.projection.x1 - pathProjection.projection.x0;
    pathProjection.projection.dy = pathProjection.projection.y1 - pathProjection.projection.y0;

    denom = pathProjection.section.dx * pathProjection.wellpath.dx + pathProjection.section.dy
        * pathProjection.wellpath.dy;

    // Project end points back onto original well path.
    if (denom != 0.0) {
      // The well path IS NOT perpendicular to projection plane.
      numer = (pathProjection.wellpath.y0 - pathProjection.projection.y0) * pathProjection.wellpath.dx
          + (pathProjection.projection.x0 - pathProjection.wellpath.x0) * pathProjection.wellpath.dy;
      t0 = numer / denom;
      numer = (pathProjection.wellpath.y0 - pathProjection.projection.y1) * pathProjection.wellpath.dx
          + (pathProjection.projection.x1 - pathProjection.wellpath.x0) * pathProjection.wellpath.dy;
      t1 = numer / denom;

      numer = (pathProjection.projection.x0 - pathProjection.wellpath.x0) * pathProjection.section.dx
          + (pathProjection.projection.y0 - pathProjection.wellpath.y0) * pathProjection.section.dy;
      pathProjection.w0 = numer / denom;
      numer = (pathProjection.projection.x1 - pathProjection.wellpath.x0) * pathProjection.section.dx
          + (pathProjection.projection.y1 - pathProjection.wellpath.y0) * pathProjection.section.dy;
      pathProjection.w1 = numer / denom;

      pathProjection.projection.z0 = pathProjection.wellpath.z0 + pathProjection.w0 * pathProjection.wellpath.dz;
      pathProjection.projection.z1 = pathProjection.wellpath.z0 + pathProjection.w1 * pathProjection.wellpath.dz;
      pathProjection.projection.dz = pathProjection.projection.z1 - pathProjection.projection.z0;
    } else {
      // The well path IS perpendicular to projection plane (or vertical).
      pathProjection.w0 = 0.0;
      pathProjection.w1 = 1.0;
      pathProjection.projection.z0 = pathProjection.wellpath.z0 + pathProjection.w0 * pathProjection.wellpath.dz;
      pathProjection.projection.z1 = pathProjection.wellpath.z0 + pathProjection.w1 * pathProjection.wellpath.dz;
      pathProjection.projection.dz = pathProjection.projection.z1 - pathProjection.projection.z0;

      double numer2 = pathProjection.section.dx * (pathProjection.section.y0 - pathProjection.wellpath.y0)
          + pathProjection.section.dy * (pathProjection.wellpath.x0 - pathProjection.section.x0);
      double denom2 = pathProjection.section.dx * pathProjection.section.dx + pathProjection.section.dy
          * pathProjection.section.dy;
      t0 = -numer2 / denom2;
      t1 = t0;
    }

    if (t0 <= 0.0 && t1 <= 0.0) {
      // Both ends in front of plane.
      pathProjectionFront[0] = pathProjection;
      pathProjectionFront[0].exists = true;
      pathProjectionBehind[0].exists = false;
    } else if (t0 <= 0.0 && t1 > 0.0) {
      // The start is in front and the end is behind.
      pathProjection.exists = true;
      denom = pathProjection.wellpath.dx * pathProjection.section.dy - pathProjection.wellpath.dy
          * pathProjection.section.dx;
      numer = (pathProjection.wellpath.y0 - pathProjection.section.y0) * pathProjection.section.dx
          + (pathProjection.section.x0 - pathProjection.wellpath.x0) * pathProjection.section.dy;
      w = numer / denom;
      numer = (pathProjection.wellpath.y0 - pathProjection.section.y0) * pathProjection.wellpath.dx
          + (pathProjection.section.x0 - pathProjection.wellpath.x0) * pathProjection.wellpath.dy;
      s = numer / denom;
      xc = pathProjection.wellpath.x0 + w * pathProjection.wellpath.dx;
      yc = pathProjection.wellpath.y0 + w * pathProjection.wellpath.dy;
      zc = pathProjection.wellpath.z0 + w * pathProjection.wellpath.dz;
      pathProjectionFront[0] = pathProjection;
      pathProjectionBehind[0] = pathProjection;
      pathProjectionFront[0].s1 = s;
      pathProjectionFront[0].w1 = w;
      pathProjectionFront[0].projection.x1 = xc;
      pathProjectionFront[0].projection.y1 = yc;
      pathProjectionFront[0].projection.z1 = zc;
      pathProjectionBehind[0].s0 = s;
      pathProjectionBehind[0].w0 = w;
      pathProjectionBehind[0].projection.x0 = xc;
      pathProjectionBehind[0].projection.y0 = yc;
      pathProjectionBehind[0].projection.z0 = zc;
    } else if (t0 > 0.0 && t1 <= 0.0) {
      // The start is behind and the end is in front.
      pathProjection.exists = true;
      denom = pathProjection.wellpath.dx * pathProjection.section.dy - pathProjection.wellpath.dy
          * pathProjection.section.dx;
      numer = (pathProjection.wellpath.y0 - pathProjection.section.y0) * pathProjection.section.dx
          + (pathProjection.section.x0 - pathProjection.wellpath.x0) * pathProjection.section.dy;
      w = numer / denom;
      numer = (pathProjection.wellpath.y0 - pathProjection.section.y0) * pathProjection.wellpath.dx
          + (pathProjection.section.x0 - pathProjection.wellpath.x0) * pathProjection.wellpath.dy;
      s = numer / denom;
      xc = pathProjection.wellpath.x0 + w * pathProjection.wellpath.dx;
      yc = pathProjection.wellpath.y0 + w * pathProjection.wellpath.dy;
      zc = pathProjection.wellpath.z0 + w * pathProjection.wellpath.dz;
      pathProjectionFront[0] = pathProjection;
      pathProjectionBehind[0] = pathProjection;
      pathProjectionBehind[0].s1 = s;
      pathProjectionBehind[0].w1 = w;
      pathProjectionBehind[0].projection.x1 = xc;
      pathProjectionBehind[0].projection.y1 = yc;
      pathProjectionBehind[0].projection.z1 = zc;
      pathProjectionFront[0].s0 = s;
      pathProjectionFront[0].w0 = w;
      pathProjectionFront[0].projection.x0 = xc;
      pathProjectionFront[0].projection.y0 = yc;
      pathProjectionFront[0].projection.z0 = zc;
    } else if (t0 > 0.0 && t1 > 0.0) {
      // Both ends are behind.
      pathProjectionBehind[0] = pathProjection;
      pathProjectionBehind[0].exists = true;
      pathProjectionFront[0].exists = false;
    } else {
      pathProjectionFront[0].exists = false;
      pathProjectionBehind[0].exists = false;
      return 3;
    }

    line.x0 = plane.x0;
    line.y0 = plane.y0;
    line.x1 = plane.x1;
    line.y1 = plane.y1;
    if (criticalDistance > 0.0) {
      if (!showAll) { /* show part of well within distance */
        if (pathProjectionFront[0].exists) {
          point.x = pathProjectionFront[0].wellpath.x0 + pathProjectionFront[0].w0 * pathProjectionFront[0].wellpath.dx;
          point.y = pathProjectionFront[0].wellpath.y0 + pathProjectionFront[0].w0 * pathProjectionFront[0].wellpath.dy;
          d0 = computePointToLineSegmentDistanceXY(point, line);
          point.x = pathProjectionFront[0].wellpath.x0 + pathProjectionFront[0].w1 * pathProjectionFront[0].wellpath.dx;
          point.y = pathProjectionFront[0].wellpath.y0 + pathProjectionFront[0].w1 * pathProjectionFront[0].wellpath.dy;
          d1 = computePointToLineSegmentDistanceXY(point, line);
          dd = d1 - d0;
          if (dd != 0.0) {
            if (d0 > criticalDistance && d1 > criticalDistance) {
              // Completely outside distance.
              pathProjectionFront[0].exists = false;
            } else if (d0 <= criticalDistance && d1 <= criticalDistance) {
              // Completely inside distance.
              pathProjectionFront[0].exists = true;
            } else {
              // Partially inside distance.
              pcnt = (criticalDistance - d0) / (d1 - d0);
              if (d0 <= criticalDistance) {
                pathProjectionFront[0].s1 = pathProjectionFront[0].s0 + pcnt
                    * (pathProjectionFront[0].s1 - pathProjectionFront[0].s0);
                pathProjectionFront[0].projection.x1 = pathProjectionFront[0].projection.x0 + pcnt
                    * (pathProjectionFront[0].projection.x1 - pathProjectionFront[0].projection.x0);
                pathProjectionFront[0].projection.y1 = pathProjectionFront[0].projection.y0 + pcnt
                    * (pathProjectionFront[0].projection.y1 - pathProjectionFront[0].projection.y0);
                pathProjectionFront[0].projection.z1 = pathProjectionFront[0].projection.z0 + pcnt
                    * (pathProjectionFront[0].projection.z1 - pathProjectionFront[0].projection.z0);
              } else {
                pathProjectionFront[0].s0 = pathProjectionFront[0].s0 + pcnt
                    * (pathProjectionFront[0].s1 - pathProjectionFront[0].s0);
                pathProjectionFront[0].projection.x0 = pathProjectionFront[0].projection.x0 + pcnt
                    * (pathProjectionFront[0].projection.x1 - pathProjectionFront[0].projection.x0);
                pathProjectionFront[0].projection.y0 = pathProjectionFront[0].projection.y0 + pcnt
                    * (pathProjectionFront[0].projection.y1 - pathProjectionFront[0].projection.y0);
                pathProjectionFront[0].projection.z0 = pathProjectionFront[0].projection.z0 + pcnt
                    * (pathProjectionFront[0].projection.z1 - pathProjectionFront[0].projection.z0);
              }
            }
          } else {
            if (d0 > criticalDistance) {
              pathProjectionFront[0].exists = false;
            }
          }
        }
        if (pathProjectionBehind[0].exists) {
          point.x = pathProjectionBehind[0].wellpath.x0 + pathProjectionBehind[0].w0
              * pathProjectionBehind[0].wellpath.dx;
          point.y = pathProjectionBehind[0].wellpath.y0 + pathProjectionBehind[0].w0
              * pathProjectionBehind[0].wellpath.dy;
          d0 = computePointToLineSegmentDistanceXY(point, line);
          point.x = pathProjectionBehind[0].wellpath.x0 + pathProjectionBehind[0].w1
              * pathProjectionBehind[0].wellpath.dx;
          point.y = pathProjectionBehind[0].wellpath.y0 + pathProjectionBehind[0].w1
              * pathProjectionBehind[0].wellpath.dy;
          d1 = computePointToLineSegmentDistanceXY(point, line);
          dd = d1 - d0;
          if (dd != 0.0) {
            if (d0 > criticalDistance && d1 > criticalDistance) {
              // Completely outside distance.
              pathProjectionBehind[0].exists = false;
            } else if (d0 <= criticalDistance && d1 <= criticalDistance) {
              // Completely inside distance.
              pathProjectionBehind[0].exists = true;
            } else {
              // Partially inside distance.
              pcnt = (criticalDistance - d0) / (d1 - d0);
              if (d0 <= criticalDistance) {
                pathProjectionBehind[0].s1 = pathProjectionBehind[0].s0 + pcnt
                    * (pathProjectionBehind[0].s1 - pathProjectionBehind[0].s0);
                pathProjectionBehind[0].projection.x1 = pathProjectionBehind[0].projection.x0 + pcnt
                    * (pathProjectionBehind[0].projection.x1 - pathProjectionBehind[0].projection.x0);
                pathProjectionBehind[0].projection.y1 = pathProjectionBehind[0].projection.y0 + pcnt
                    * (pathProjectionBehind[0].projection.y1 - pathProjectionBehind[0].projection.y0);
                pathProjectionBehind[0].projection.z1 = pathProjectionBehind[0].projection.z0 + pcnt
                    * (pathProjectionBehind[0].projection.z1 - pathProjectionBehind[0].projection.z0);
              } else {
                pathProjectionBehind[0].s0 = pathProjectionBehind[0].s0 + pcnt
                    * (pathProjectionBehind[0].s1 - pathProjectionBehind[0].s0);
                pathProjectionBehind[0].projection.x0 = pathProjectionBehind[0].projection.x0 + pcnt
                    * (pathProjectionBehind[0].projection.x1 - pathProjectionBehind[0].projection.x0);
                pathProjectionBehind[0].projection.y0 = pathProjectionBehind[0].projection.y0 + pcnt
                    * (pathProjectionBehind[0].projection.y1 - pathProjectionBehind[0].projection.y0);
                pathProjectionBehind[0].projection.z0 = pathProjectionBehind[0].projection.z0 + pcnt
                    * (pathProjectionBehind[0].projection.z1 - pathProjectionBehind[0].projection.z0);
              }
            }
          } else {
            if (d0 > criticalDistance) {
              pathProjectionBehind[0].exists = false;
            }
          }
        }
      }
    }

    return 0;
  }

  /**
   * @param segment
   * @param plane
   * @param cdist
   * @param distance
   * @return
   */
  public static boolean checkProjection(final LineSegment segment, final ProjectionPlane plane, final float cdist,
      final double[] distance) {

    int ier = 0;
    double s, ss0, ss1;
    double t0, t1;
    double d0, d1;
    double x0, y0;
    double x1, y1;
    double numer, denom;
    WellPathProjection segO = new WellPathProjection();
    ProjectionPoint ppt = new ProjectionPoint();
    ProjectionLine pline = new ProjectionLine();

    // Set the initial projection parameters.
    distance[0] = 0.0;
    segO.exists = false;
    segO.section = plane;
    segO.wellpath = segment;

    // Project start of well path segment onto the plane.
    denom = segO.section.dx * segO.section.dx + segO.section.dy * segO.section.dy;
    numer = (segO.wellpath.x0 - segO.section.x0) * segO.section.dx + (segO.wellpath.y0 - segO.section.y0)
        * segO.section.dy;
    segO.s0 = numer / denom;
    numer = (segO.wellpath.x1 - segO.section.x0) * segO.section.dx + (segO.wellpath.y1 - segO.section.y0)
        * segO.section.dy;
    segO.s1 = numer / denom;

    // If does not project onto segment, then return false.
    if (segO.s0 < 0.0 && segO.s1 < 0.0) {
      return false;
    }
    if (segO.s0 > 1.0 && segO.s1 > 1.0) {
      return false;
    }

    // Check start of well path projection.
    s = segO.s0;
    if (s < 0.0) {
      s = 0.0;
    }
    if (s > 1.0) {
      s = 1.0;
    }
    segO.projection.x0 = segO.section.x0 + s * segO.section.dx;
    segO.projection.y0 = segO.section.y0 + s * segO.section.dy;
    segO.s0 = s;
    ss0 = s;

    // Check end of well path projection.
    s = segO.s1;
    if (s < 0.0) {
      s = 0.0;
    }
    if (s > 1.0) {
      s = 1.0;
    }
    segO.projection.x1 = segO.section.x0 + s * segO.section.dx;
    segO.projection.y1 = segO.section.y0 + s * segO.section.dy;
    segO.s1 = s;
    ss1 = s;

    denom = segO.section.dx * segO.wellpath.dx + segO.section.dy * segO.wellpath.dy;

    // Project the projection endpoints back onto original well path.
    if (denom != 0.0) {
      // The well path is NOT perpendicular to projection plane.
      numer = (segO.wellpath.y0 - segO.projection.y0) * segO.wellpath.dx + (segO.projection.x0 - segO.wellpath.x0)
          * segO.wellpath.dy;
      t0 = numer / denom;
      numer = (segO.wellpath.y0 - segO.projection.y1) * segO.wellpath.dx + (segO.projection.x1 - segO.wellpath.x0)
          * segO.wellpath.dy;
      t1 = numer / denom;

      numer = (segO.projection.x0 - segO.wellpath.x0) * segO.section.dx + (segO.projection.y0 - segO.wellpath.y0)
          * segO.section.dy;
      segO.w0 = numer / denom;
      numer = (segO.projection.x1 - segO.wellpath.x0) * segO.section.dx + (segO.projection.y1 - segO.wellpath.y0)
          * segO.section.dy;
      segO.w1 = numer / denom;
    } else {
      // The well path is perpendicular to projection plane (or vertical).
      denom = segO.section.dx * segO.section.dx + segO.section.dy * segO.section.dy;
      numer = (segO.section.y0 - segO.wellpath.y0) * segO.section.dx + (segO.wellpath.x0 - segO.section.x0)
          * segO.section.dy;
      t0 = numer / denom;
      numer = (segO.section.y1 - segO.wellpath.y0) * segO.section.dx + (segO.wellpath.x1 - segO.section.x0)
          * segO.section.dy;
      t1 = numer / denom;

      segO.w0 = 0.0;
      segO.w1 = 1.0;
    }

    pline.x0 = plane.x0;
    pline.y0 = plane.y0;
    pline.x1 = plane.x1;
    pline.y1 = plane.y1;
    if (t0 <= 0.0 && t1 <= 0.0) {
      // Both in front.
      ppt.x = segO.wellpath.x0 + segO.w0 * segO.wellpath.dx;
      ppt.y = segO.wellpath.y0 + segO.w0 * segO.wellpath.dy;
      d0 = computePointToLineSegmentDistanceXY(ppt, pline);
      ppt.x = segO.wellpath.x0 + segO.w1 * segO.wellpath.dx;
      ppt.y = segO.wellpath.y0 + segO.w1 * segO.wellpath.dy;
      d1 = computePointToLineSegmentDistanceXY(ppt, pline);
      if (d0 <= cdist || d1 <= cdist) {
        if (d0 < d1) {
          distance[0] = d0;
        } else {
          distance[0] = d1;
        }
        return true;
      }
    } else if (t0 <= 0.0 && t1 > 0.0) {
      // The start is in front and the end is behind.
      distance[0] = 0.0;
      return true;
    } else if (t0 > 0.0 && t1 <= 0.0) {
      // The start is behind and the end is in front.
      distance[0] = 0.0;
      return true;
    } else if (t0 > 0.0 && t1 > 0.0) {
      // Both behind.
      ppt.x = segO.wellpath.x0 + segO.w0 * segO.wellpath.dx;
      ppt.y = segO.wellpath.y0 + segO.w0 * segO.wellpath.dy;
      d0 = computePointToLineSegmentDistanceXY(ppt, pline);
      ppt.x = segO.wellpath.x0 + segO.w1 * segO.wellpath.dx;
      ppt.y = segO.wellpath.y0 + segO.w1 * segO.wellpath.dy;
      d1 = computePointToLineSegmentDistanceXY(ppt, pline);
      if (d0 <= cdist || d1 <= cdist) {
        if (d0 < d1) {
          distance[0] = d0;
        } else {
          distance[0] = d1;
        }
        return true;
      }
    } else {
      return false;
    }
    return false;
  }

  /**
   * Computes the distance between a point and a line segment in x,y space.
   * 
   * @param ppt the point.
   * @param pline the line segment.
   * @return the distance between the point and line segment in x,y space.
   */
  public static double computePointToLineSegmentDistanceXY(final ProjectionPoint point, final ProjectionLine line) {
    double distance = 0;
    double w;
    double lineDX, lineDY;
    double numer, denom;
    ProjectionPoint temp = new ProjectionPoint();

    // Calculate the distance from a point to a line in x,y space.
    lineDX = line.x1 - line.x0;
    lineDY = line.y1 - line.y0;
    denom = lineDX * lineDX + lineDY * lineDY;
    numer = (point.x - line.x0) * lineDX + (point.y - line.y0) * lineDY;
    w = numer / denom;
    if (w < 0.0) {
      temp.x = line.x0;
      temp.y = line.y0;
      distance = computePointToPointDistanceXY(point, temp);
    } else if (w > 1.0) {
      temp.x = line.x1;
      temp.y = line.y1;
      distance = computePointToPointDistanceXY(point, temp);
    } else {
      temp.x = line.x0 + w * lineDX;
      temp.y = line.y0 + w * lineDY;
      distance = computePointToPointDistanceXY(point, temp);
    }
    return distance;

  }

  /**
   * Computes the distance between 2 points in x,y space.
   * 
   * @param ptA the 1st point.
   * @param ptB the 2nd point.
   * @return the distance between the points in x,y space.
   */
  public static double computePointToPointDistanceXY(final ProjectionPoint point1, final ProjectionPoint point2) {
    double dx = (point1.x - point2.x) * (point1.x - point2.x);
    double dy = (point1.y - point2.y) * (point1.y - point2.y);
    return Math.sqrt(dx + dy);
  }

  /**
   * Computes the intersection between a triangle and a plane.
   * 
   * @param points the vertices of the triangle.
   * @param plane the plane.
   * @return the end points of the resulting segment.
   */
  public static TriangleIntersection computeTrianglePlaneIntersection(final Point3d[] points,
      final ProjectionPlane plane) {
    double denom;
    double u1 = 0, u2 = 0, u3 = 0, v1 = 0, v2 = 0, v3 = 0;
    double uhi, ulo, vhi, vlo;
    double Z1, Z2, Z3, Zlo, Zhi;
    boolean side1, side2, side3;

    side1 = false;
    side2 = false;
    side3 = false;

    /* initialize results */
    //    intersect->found = false;
    //    intersect->tn0 = 0.0;
    //    intersect->tn1 = 0.0;
    //    intersect->tm0 = 0.0;
    //    intersect->tm1 = 0.0;

    /* plane XY variables */
    plane.dx = plane.x1 - plane.x0;
    plane.dy = plane.y1 - plane.y0;
    double triangle_dx01 = points[1].getX() - points[0].getX();
    double triangle_dy01 = points[1].getY() - points[0].getY();
    double triangle_dx02 = points[2].getX() - points[0].getX();
    double triangle_dy02 = points[2].getY() - points[0].getY();
    double triangle_dx12 = points[2].getX() - points[1].getX();
    double triangle_dy12 = points[2].getY() - points[1].getY();

    // Triangle side 1.
    denom = plane.dx * triangle_dy01 - triangle_dx01 * plane.dy;
    if (denom != 0.0) {
      v1 = (plane.dy * (points[0].getX() - plane.x0) - plane.dx * (points[0].getY() - plane.y0)) / denom;
      if (plane.dx == 0.0) {
        u1 = (points[0].getY() - plane.y0 + v1 * triangle_dy01) / plane.dy;
      } else {
        u1 = (points[0].getX() - plane.x0 + v1 * triangle_dx01) / plane.dx;
      }
      if (v1 >= 0.0 && v1 <= 1.0) {
        side1 = true;
      }
    }

    // Triangle side 2.
    denom = plane.dx * triangle_dy02 - triangle_dx02 * plane.dy;
    if (denom != 0.0) {
      v2 = (plane.dy * (points[0].getX() - plane.x0) - plane.dx * (points[0].getY() - plane.y0)) / denom;
      if (plane.dx == 0.0) {
        u2 = (points[0].getY() - plane.y0 + v2 * triangle_dy02) / plane.dy;
      } else {
        u2 = (points[0].getX() - plane.x0 + v2 * triangle_dx02) / plane.dx;
      }
      if (v2 >= 0.0 && v2 <= 1.0) {
        side2 = true;
      }
    }

    // Triangle side 3.
    denom = plane.dx * triangle_dy12 - triangle_dx12 * plane.dy;
    if (denom != 0.0) {
      v3 = (plane.dy * (points[1].getX() - plane.x0) - plane.dx * (points[1].getY() - plane.y0)) / denom;
      if (plane.dx == 0.0) {
        u3 = (points[1].getY() - plane.y0 + v3 * triangle_dy12) / plane.dy;
      } else {
        u3 = (points[1].getX() - plane.x0 + v3 * triangle_dx12) / plane.dx;
      }
      if (v3 >= 0.0 && v3 <= 1.0) {
        side3 = true;
      }
    }

    if (side1 && side2) {
      Z1 = points[0].getZ() + v1 * (points[1].getZ() - points[0].getZ());
      Z2 = points[0].getZ() + v2 * (points[2].getZ() - points[0].getZ());
      if (u2 > u1) {
        uhi = u2;
        ulo = u1;
        vhi = v2;
        vlo = v1;
        Zhi = Z2;
        Zlo = Z1;
      } else {
        uhi = u1;
        ulo = u2;
        vhi = v1;
        vlo = v2;
        Zhi = Z1;
        Zlo = Z2;
      }
    } else if (side1 && side3) {
      Z1 = points[0].getZ() + v1 * (points[1].getZ() - points[0].getZ());
      Z3 = points[1].getZ() + v3 * (points[2].getZ() - points[1].getZ());
      if (u3 > u1) {
        uhi = u3;
        ulo = u1;
        vhi = v3;
        vlo = v1;
        Zhi = Z3;
        Zlo = Z1;
      } else {
        uhi = u1;
        ulo = u3;
        vhi = v1;
        vlo = v3;
        Zhi = Z1;
        Zlo = Z3;
      }
    } else if (side2 && side3) {
      Z2 = points[0].getZ() + v2 * (points[2].getZ() - points[0].getZ());
      Z3 = points[1].getZ() + v3 * (points[2].getZ() - points[1].getZ());
      if (u3 > u2) {
        uhi = u3;
        ulo = u2;
        vhi = v3;
        vlo = v2;
        Zhi = Z3;
        Zlo = Z2;
      } else {
        uhi = u2;
        ulo = u3;
        vhi = v2;
        vlo = v3;
        Zhi = Z2;
        Zlo = Z3;
      }
    } else {
      // Do nothing.
      return new TriangleIntersection();
    }
    if (ulo > 1.0 || uhi < 0.0) {
      return new TriangleIntersection();
    }

    double tn0, tm0;
    double tn1, tm1;
    if (ulo < 0.0) {
      tn0 = 0.0;
      tm0 = Zlo + (0.0 - ulo) / (uhi - ulo) * (Zhi - Zlo);
    } else {
      tn0 = ulo;
      tm0 = Zlo;
    }
    if (uhi > 1.0) {
      tn1 = 1.0;
      tm1 = Zlo + (1.0 - ulo) / (uhi - ulo) * (Zhi - Zlo);
    } else {
      tn1 = uhi;
      tm1 = Zhi;
    }
    return new TriangleIntersection(tn0, tn1, tm0, tm1);
  }
}
