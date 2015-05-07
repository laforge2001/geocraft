/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.math;


/**
 * Clip a line to a rectangle. 
 */
public class CohenSutherlandClip {

  double _xLeft;

  double _xRight;

  double _yBottom;

  double _yTop;

  public CohenSutherlandClip(final double xLeft, final double yBottom, final double xRight, final double yTop) {
    _xLeft = xLeft;
    _yBottom = yBottom;
    _xRight = xRight;
    _yTop = yTop;
  }

  public double[] clip(final double x0, final double y0, final double x1, final double y1) {
    Point p1 = new Point(x0, y0);
    Point p2 = new Point(x1, y1);

    boolean clipped = clip(p1, p2);

    if (clipped) {
      return new double[] { p1.x, p1.y, p2.x, p2.y };
    }

    return new double[0];
  }

  public boolean clip(final Point point0, final Point point1) {
    int outCode0, outCode1;

    while (true) {

      outCode0 = outCodes(point0);
      outCode1 = outCodes(point1);

      if (rejectCheck(outCode0, outCode1)) {
        return false;
      }

      if (acceptCheck(outCode0, outCode1)) {
        return true;
      }

      if (outCode0 == 0) {
        double tempCoord;
        int tempCode;

        tempCoord = point0.x;
        point0.x = point1.x;
        point1.x = tempCoord;

        tempCoord = point0.y;
        point0.y = point1.y;
        point1.y = tempCoord;

        tempCode = outCode0;
        outCode0 = outCode1;
        outCode1 = tempCode;
      }

      if ((outCode0 & 1) != 0) {
        point0.x += (point1.x - point0.x) * (_yTop - point0.y) / (point1.y - point0.y);
        point0.y = _yTop;
      } else if ((outCode0 & 2) != 0) {
        point0.x += (point1.x - point0.x) * (_yBottom - point0.y) / (point1.y - point0.y);
        point0.y = _yBottom;
      } else if ((outCode0 & 4) != 0) {
        point0.y += (point1.y - point0.y) * (_xRight - point0.x) / (point1.x - point0.x);
        point0.x = _xRight;
      } else if ((outCode0 & 8) != 0) {
        point0.y += (point1.y - point0.y) * (_xLeft - point0.x) / (point1.x - point0.x);
        point0.x = _xLeft;
      }
    }
  }

  private int outCodes(final Point p) {

    int code = 0;

    if (p.y > _yTop) {
      code += 1; /* code for above */
    } else if (p.y < _yBottom) {
      code += 2; /* code for below */
    }

    if (p.x > _xRight) {
      code += 4; /* code for right */
    } else if (p.x < _xLeft) {
      code += 8; /* code for left */
    }

    return code;
  }

  private boolean rejectCheck(final int outCode1, final int outCode2) {
    if ((outCode1 & outCode2) != 0) {
      return true;
    }
    return false;
  }

  private boolean acceptCheck(final int outCode1, final int outCode2) {
    if (outCode1 == 0 && outCode2 == 0) {
      return true;
    }
    return false;
  }

  private static class Point {

    double x;

    double y;

    Point(final double xcoord, final double ycoord) {
      x = xcoord;
      y = ycoord;
    }
  }
}
