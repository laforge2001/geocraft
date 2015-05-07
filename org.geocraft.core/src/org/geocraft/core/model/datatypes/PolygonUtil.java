/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.core.model.datatypes;


/**
 * The class contains static methods that calculate the types of a polygon that is represented by an array of coordinates.
 */
public class PolygonUtil {

  /**
   * The enum represents types of a polygon. A <i>complex</i> polygon is a polygon that its edges intersect. A simple polygon is a
   * polygon that its edges do not intersect. A simple polygon can be represented by a sequence of vertexes in either <i>clockwise</i>
   * order or <i>counterclockwise</i> order.
   */
  public enum PolygonType {
    Clockwise,
    CounterClockwise,
    Complex,
    Unknown
  }

  /**
   * Get the rotational direction of a polygon that is represented by a sequence of coordinates.
   * 
   * @param a
   *                sequence of coordinates.
   * @return a value in enum PolygonType.
   */
  public static PolygonType getDirection(final CoordinateSeries a) {
    // must more than two points.
    if (a.getPointsDirect().length < 3) {
      return PolygonType.Unknown;
    }
    if (containIntersection(a)) {
      return PolygonType.Complex;
    }
    double signedArea = signedPolygonArea(a);
    if (signedArea < 0) {
      return PolygonType.Clockwise;
    } else if (signedArea > 0) {
      return PolygonType.CounterClockwise;
    } else {
      return PolygonType.Unknown;
    }
  }

  /**
   * Calculate the area of a polygon that its vertexes are represented with a sequence of coordinates.
   * 
   * @param a
   *                a sequence of coordinates that represents the vertexes of the polygon.
   * @return negative area if vertexes are in clockwise order, positive area if vertexes are in counterclockwise order, zero if less
   *         than 3 vertexes.
   */
  public static double signedPolygonArea(final CoordinateSeries a) {
    double area = 0;
    int i;
    int j;
    Point3d[] points = a.getPointsDirect();
    if (points.length < 3) {
      return 0.0;
    }
    for (i = 0; i < points.length; i++) {
      j = (i + 1) % points.length;
      area += points[i].getX() * points[j].getY() - points[j].getX() * points[i].getY();
    }
    return area / 2.0;
  }

  /**
   * check if the polygon lines insect each other.
   * 
   * @param a
   *                a sequence of coordinates of the polygon.
   * @return true if an intersection is found.
   */
  public static boolean containIntersection(final CoordinateSeries a) {
    Point3d a1;
    Point3d a2;
    int i;
    int j;
    Point3d[] points = a.getPointsDirect();
    // no intersection is possible if less than 3 vertexes.
    if (points.length < 4) {
      return false;
    }
    for (i = 0; i < points.length; i++) {
      a1 = points[i];
      a2 = points[(i + 1) % points.length];
      for (j = i + 2; j < points.length; j++) {
        if ((j + 1) % points.length == i) {
          continue;
        }
        if (intersect(a1, a2, points[j], points[(j + 1) % points.length])) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Check whether line segment (a, b) intersects with line segment (c, d).
   * 
   * @param a
   *                a vertex of line segment (a, b)
   * @param b
   *                a vertex of line segment (a, b)
   * @param c
   *                a vertex of line segment (c, d)
   * @param d
   *                a vertex of line segment (c, d)
   * @return true if line segment (a, b) intersects with line segment (c, d), false otherwise.
   */
  static boolean intersect(final Point3d a, final Point3d b, final Point3d c, final Point3d d) {
    double lineABturnC = clockwiseTurn(a, b, c);
    double lineABturnD = clockwiseTurn(a, b, d);
    double lineCDturnA = clockwiseTurn(c, d, a);
    double lineCDturnB = clockwiseTurn(c, d, b);
    return lineABturnC != lineABturnD && lineCDturnA != lineCDturnB;
  }

  /**
   * check whether the sequence of three points (a, b, and c) is in clockwise order.
   * 
   * @param a
   *                the first point
   * @param b
   *                the second point
   * @param c
   *                the third point
   * @return -1.0 if counterclockwise, 1.0 if clockwise, 0 if no turn.
   */
  static double clockwiseTurn(final Point3d a, final Point3d b, final Point3d c) {
    return Math.signum((b.getY() - a.getY()) * (c.getX() - a.getX()) - (b.getX() - a.getX()) * (c.getY() - a.getY()));
  }
}
