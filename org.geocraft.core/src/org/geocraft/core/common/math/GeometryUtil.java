/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.math;


public class GeometryUtil {

  /**
   * Compute the normalized normal to a plane defined by three points. 
   * 
   * @param x0
   * @param y0
   * @param z0
   * @param x1
   * @param y1
   * @param z1
   * @param x2
   * @param y2
   * @param z2
   * @return normalized normal
   */
  public static double[] computeNormal(final double x0, final double y0, final double z0, final double x1,
      final double y1, final double z1, final double x2, final double y2, final double z2) {
    double[] result = new double[3];
    double[] a = { x1 - x0, y1 - y0, z1 - z0 };
    double[] b = { x2 - x0, y2 - y0, z2 - z0 };
    result[0] = a[1] * b[2] - a[2] * b[1];
    result[1] = a[2] * b[0] - a[0] * b[2];
    result[2] = a[0] * b[1] - a[1] * b[0];
    double mag = Math.sqrt(result[0] * result[0] + result[1] * result[1] + result[2] * result[2]);
    return new double[] { result[0] / mag, result[1] / mag, result[2] / mag };
  }

  /**
   * Compute the distance to a plane from a point. 
   * 
   * @param x0 any point that lies on the plane
   * @param y0 any point that lies on the plane
   * @param z0 any point that lies on the plane
   * @param normal the normal to the plane
   * @param xp the other point
   * @param yp the other point
   * @param zp the other point
   * @return the signed distance from the point to the plane. 
   */
  public static double distancePointToPlane(final double x0, final double y0, final double z0, final double[] normal,
      final double xp, final double yp, final double zp) {
    double x = xp - x0;
    double y = yp - y0;
    double z = zp - z0;

    return normal[0] * x + normal[1] * y + normal[2] * z;
  }

  /**
   * Compute the distance of a point from a line. Note that the line
   * is merely defined by the two points and continues to infinity
   * in both directions. So the projection of the point does
   * not have to lie within the two points (they are not the end points).  
   * 
   * @param x0 x coordinate of P0 that defines the line
   * @param y0 y coordinate of P0 that defines the line
   * @param x1 x coordinate of P1 that defines the line
   * @param y1 y coordinate of P1 that defines the line
   * @param xp x coordinate of point 
   * @param yp y coordinate of point
   * @return distance between the line and the point.
   */
  public static double distancePointToLine(final double x0, final double y0, final double x1, final double y1,
      final double xp, final double yp) {

    double[] pt = projectPointOntoLine(x0, y0, x1, y1, xp, yp);

    return Math.sqrt((pt[0] - xp) * (pt[0] - xp) + (pt[1] - yp) * (pt[1] - yp));
  }

  /**
   * The distance between 2 points in the same plane. 
   * @param x1 x coordinate of P1
   * @param y1 y coordinate of P1
   * @param x2 x coordinate of P2
   * @param y2 y coordinate of P2
   * @return distance
   */
  public static double distancePointToPoint(final double x1, final double y1, final double x2, final double y2) {
    return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
  }

  /**
   * Find the point on the line that is closest to the specified point. 
   * 
   * Equation of a line:
   * P = P0 + u(P1 - P0)
   * 
   * P2 is the point on the line that is closest to P
   * (P2-P) dot (P2-P1) = 0 
   * 
   * @param x0 x coordinate of P0 that defines the line
   * @param y0 y coordinate of P0 that defines the line
   * @param x1 x coordinate of P1 that defines the line
   * @param y1 y coordinate of P1 that defines the line
   * @param xp x coordinate of point 
   * @param yp y coordinate of point
   * @return an array containing the x,y coordinate of the closest point. 
   */
  public static double[] projectPointOntoLine(final double x0, final double y0, final double x1, final double y1,
      final double xp, final double yp) {

    double u = (xp - x0) * (x1 - x0) + (yp - y0) * (y1 - y0);
    u = u / ((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0));

    // location of the point on the line where the tangent intersects
    double x = x0 + u * (x1 - x0);
    double y = y0 + u * (y1 - y0);

    return new double[] { x, y };
  }
}
