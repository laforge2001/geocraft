/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.ellipse;


import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.abavo.crossplot.IABavoCrossplot.EllipseComputation;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.ui.plot.object.IPlotPolygon;


/**
 * A utility class for ellipse-related computation.
 */
public class EllipseUtil {

  /** The ellipse drawing scalar. */
  protected static final int ELLIPSE_DRAWING_SCALAR = 2;

  /**
   * Computes the x,y points of an ellipse for drawing, based on slope, length and width.
   * @param mterm the slope of the ellipse.
   * @param aterm the half-length of the ellipse (major axis).
   * @param bterm the half-width of the ellipse (minor axis).
   * @param ex the ellipse x-coordinates.
   * @param ey the ellipse y-coordinates.
   */
  public static void computeEllipse(final double mterm, final double aterm, final double bterm, final double centerX,
      final double centerY, final double[] ex, final double[] ey) {

    double xppp = 0;
    double yppp = 0;
    EllipseComputation ellipseComputation = EllipseComputation.Mathematical;
    double[] alpha = new double[EllipseRegionsModel.NUMBER_OF_ELLIPSE_POINTS];
    int n90 = EllipseRegionsModel.NUMBER_OF_ELLIPSE_POINTS / 4;
    int n180 = n90 + n90;
    int n270 = n180 + n90;
    int n360 = n270 + n90;
    int summation = 0;

    for (int i = 0; i < n90; i++) {
      summation += i;
    }

    double aLength = aterm;
    double bLength = bterm;

    double abRatio = aLength / bLength;

    abRatio = Math.max(1.01, abRatio);

    double cosTheta = 1 / Math.sqrt(1 + mterm * mterm);
    double sinTheta = cosTheta * mterm;

    double numer = aLength * aLength * bLength * bLength;
    double deltaMain = 90 / (n90 + summation - summation / (abRatio * abRatio));
    double deltaSecn = (1 - 1 / (abRatio * abRatio)) * deltaMain;

    alpha[0] = 0;

    double alphaOld = alpha[0];

    for (int i = 1; i < EllipseRegionsModel.NUMBER_OF_ELLIPSE_POINTS; i++) {

      if (i > 1 && i <= n90) {
        alpha[i] = alphaOld + deltaMain + (i - 1) * deltaSecn;
      }
      if (i > n90 && i <= n180) {
        alpha[i] = alphaOld + deltaMain + (30 - i) * deltaSecn;
      }
      if (i > n180 && i <= n270) {
        alpha[i] = alphaOld + deltaMain + (i - 31) * deltaSecn;
      }
      if (i > n270 && i < n360) {
        alpha[i] = alphaOld + deltaMain + (60 - i) * deltaSecn;
      }
      alphaOld = alpha[i];
    }

    for (int i = 0; i < EllipseRegionsModel.NUMBER_OF_ELLIPSE_POINTS; i++) {

      double cosTerm = Math.cos(Math.PI * alpha[i] / 180);
      double sinTerm = Math.sin(Math.PI * alpha[i] / 180);
      double denom = bLength * bLength * cosTerm * cosTerm + aLength * aLength * sinTerm * sinTerm;
      double r = Math.sqrt(numer / denom);
      double xtmp = r * cosTerm;
      double ytmp = r * sinTerm;

      ex[i] = centerX + xtmp * cosTheta - ytmp * sinTheta;
      ey[i] = centerY + xtmp * sinTheta + ytmp * cosTheta;

      if (ellipseComputation.equals(EllipseComputation.Visual)) {
        ex[i] = ex[i] / xppp;
        ey[i] = ey[i] / yppp;
      }
    }
  }

  /**
   * Computes the x,y points of an ellipse for drawing, based on cursor location.
   * @param cursorX the cursor x-coordinate.
   * @param cursorY the cursor y-coordinate.
   * @param mterm the slope of the ellipse.
   * @param aterm the half-length of the ellipse (major axis).
   * @param bterm the half-width of the ellipse (minor axis).
   * @param ex the ellipse x-coordinates.
   * @param ey the ellipse y-coordinates.
   */
  public static void computeEllipse(final double cursorX, final double cursorY, final double mterm,
      final double centerX, final double centerY, final double[] aterm, final double[] bterm, final double[] ex,
      final double[] ey) {

    EllipseComputation ellipseComputation = EllipseComputation.Mathematical;

    double x1 = cursorX - centerX;
    double y1 = cursorY - centerY;

    // Compute the x,y intersection point on the regression line (the point on the line that
    // is the shortest distance from the cursor).
    double xi = 0;
    double yi = 0;
    if (ellipseComputation.equals(EllipseComputation.Mathematical)) {
      xi = (mterm * y1 + x1) / (mterm * mterm + 1);
      yi = mterm * xi;
    } else {
      throw new RuntimeException("Invalid ellipse computation.");
    }

    double aLength = Math.sqrt(xi * xi + yi * yi);
    double bLength = Math.sqrt((xi - x1) * (xi - x1) + (yi - y1) * (yi - y1));

    aLength *= ELLIPSE_DRAWING_SCALAR;
    bLength *= ELLIPSE_DRAWING_SCALAR;

    double abRatio = aLength / bLength;

    if (abRatio < 1.01) {

      double delx = (x1 - xi) * abRatio / 1.01;
      double dely = (y1 - yi) * abRatio / 1.01;

      x1 = xi + delx;
      y1 = yi + dely;
      bLength = Math.sqrt((xi - x1) * (xi - x1) + (yi - y1) * (yi - y1));
      bLength *= ELLIPSE_DRAWING_SCALAR;
      abRatio = aLength / bLength;
    }

    aterm[0] = (float) aLength;
    bterm[0] = (float) bLength;

    computeEllipse(mterm, aLength, bLength, centerX, centerY, ex, ey);
  }

  /**
   * Checks the intersection of the region boundaries with the background ellipse and the bounds of the crossplot.
   * @param x storage for the x-coordinate.
   * @param y storage for the y-coordinate.
   * @param slope the slope of the region bound.
   * @param b the intersect of the region bound.
   * @param ellipse the background ellipse (as cgPolygon).
   * @deprecated this method was based on a (0,0) center and should no longer be used in the general case.
   */
  @Deprecated
  public static Point3d intersection(final boolean regxy, final double slope, final double b,
      final IPlotPolygon ellipse, final double x1, final double y1) {

    double m1;
    double b1;
    double closeX = 0;
    double negX = 0;
    double posX = 0;
    double histX = 0;
    double intersectionX = 0;
    double closeY = 0;
    double negY = 0;
    double posY = 0;
    double histY = 0;
    double intersectionY = 0;
    double closeDisc = 0;
    double negDisc = 0;
    double posDisc = 0;
    double histDisc = 0;
    double intersectionDisc = 0;
    int closeIndex = 0;
    int negIndex = 0;
    int posIndex = 0;
    int histIndex = 0;
    int intersectionIndex = 0;
    int flag = 0;
    int intersectionCount = 0;
    int i = 0;

    int numPoints = ellipse.getPointCount();
    for (int index = 0; index < numPoints; index++) {

      double d = 0;
      double px = ellipse.getPoint(index).getX();
      double py = ellipse.getPoint(index).getY();
      if (regxy) {
        d = px * slope + b - py;
      } else {
        d = py * slope + b - px;
      }
      if (d < 0) {

        negX = px;
        negY = py;
        negDisc = -d;
        negIndex = i;
        if (i == 0 || negDisc < closeDisc) {
          closeX = negX;
          closeY = negY;
          closeDisc = negDisc;
          closeIndex = negIndex;
        }
        flag |= 1;
      } else {

        posX = px;
        posY = py;
        posDisc = d;
        posIndex = i;
        if (i == 0 || posDisc < closeDisc) {
          closeX = posX;
          closeY = posY;
          closeDisc = posDisc;
          closeIndex = posIndex;
        }
        flag |= 2;
      }
      if (flag == 3) {

        if (Math.abs(negIndex - posIndex) > 1) {
          Shell shell = new Shell(Display.getCurrent());
          MessageDialog.openWarning(shell, "intercept()", "Subpoint indices: " + negIndex + "," + posIndex);
          shell.dispose();
        }
        if (intersectionCount == 0) {

          if (regxy) {
            m1 = (posY - negY) / (posX - negX);
            b1 = negY - m1 * negX;
            histX = (b - b1) / (m1 - slope);
            histY = slope * histX + b;
          } else {
            m1 = (posX - negX) / (posY - negY);
            b1 = negX - m1 * negY;
            histY = (b - b1) / (m1 - slope);
            histX = slope * histY + b;
          }
          intersectionCount++;
        } else if (intersectionCount == 1) {

          if (regxy) {
            m1 = (posY - negY) / (posX - negX);
            b1 = negY - m1 * negX;
            intersectionX = (b - b1) / (m1 - slope);
            intersectionY = slope * intersectionX + b;
          } else {
            m1 = (posX - negX) / (posY - negY);
            b1 = negX - m1 * negY;
            intersectionY = (b - b1) / (m1 - slope);
            intersectionX = slope * intersectionY + b;
          }
          if ((x1 - histX) * (x1 - histX) + (y1 - histY) * (y1 - histY) < (x1 - intersectionX) * (x1 - intersectionX)
              + (y1 - intersectionY) * (y1 - intersectionY)) {
            intersectionX = histX;
            intersectionY = histY;
            intersectionDisc = histDisc;
            intersectionIndex = histIndex;
          }
          intersectionCount++;
        } else {
          Shell shell = new Shell(Display.getCurrent());
          MessageDialog.openWarning(shell, "intercept()", "More than two intersections!");
          shell.dispose();
        }
        if (posIndex > negIndex) {
          flag = 2;
        } else {
          flag = 1;
        }
      }
      i++;
    }
    if (intersectionCount == 0) {
      intersectionX = closeX;
      intersectionY = closeY;
      intersectionDisc = closeDisc;
      intersectionIndex = closeIndex;
    }
    return new Point3d(intersectionX, intersectionY, 0);
  }

  /**
   * Computes the intersection of a line through a point and an ellipse.
   * @param x the x-coordinate of the point.
   * @param y the y-coordinate of the point.
   * @param intersectAngle the angle (in degrees) of the line thru the point.
   * @param ellipseModel the ellipse model.
   */
  public static Point2D intersection(final double x, final double y, final double dx, final double dy,
      final EllipseModel ellipseModel) {
    return intersection(x, y, dx, dy, ellipseModel.getSlope(), ellipseModel.getLength(), ellipseModel.getWidth(),
        ellipseModel.getCenterX(), ellipseModel.getCenterY());
  }

  /**
   * Computes the intersection of a line through a point and an ellipse.
   * @param x the x-coordinate of the point.
   * @param y the y-coordinate of the point.
   * @param intersectAngle the angle (in degrees) of the line thru the point.
   * @param ellipseSlope the slope of the major axis of the ellipse.
   * @param ellipseLength the length of the ellipse along the major axis.
   * @param ellipseWidth the width of the ellipse along the minor axis.
   * @param centerX the x-coordinate of the ellipse center.
   * @param centerY the y-coordinate of the ellipse center.
   */
  public static Point2D intersection(final double x, final double y, final double dx, final double dy,
      final double ellipseSlope, final double ellipseLength, final double ellipseWidth, final double centerX,
      final double centerY) {
    AffineTransform transform = new AffineTransform();

    double theta = -Math.atan(ellipseSlope);
    if (ellipseSlope < 0) {
      theta = Math.atan(-ellipseSlope);
    }
    transform.rotate(theta);
    transform.translate(-centerX, -centerY);
    double intersectAngle = Math.atan2(dy, dx);
    double beta = intersectAngle + theta;
    Point2D ptSrc = new Point2D.Double(x, y);
    Point2D ptDst = new Point2D.Double();
    transform.transform(ptSrc, ptDst);
    Point2D intersection = new Point2D.Double(Double.NaN, Double.NaN);

    double costerm = Math.cos(beta);
    double sinterm = Math.sin(beta);
    double a = ellipseLength;
    double b = ellipseWidth;
    double a2 = a * a;
    double b2 = b * b;
    double xp = ptDst.getX();
    double yp = ptDst.getY();
    double aterm = b2 * costerm * costerm + a2 * sinterm * sinterm;
    double bterm = b2 * 2 * xp * costerm + a2 * 2 * yp * sinterm;
    double cterm = b2 * xp * xp + a2 * yp * yp - a2 * b2;

    double term = bterm * bterm - 4 * aterm * cterm;
    //System.out.println("term=" + term + " " + aterm + " " + bterm + " " + cterm);
    if (term > 0) {
      double sqrtTerm = Math.sqrt(term);
      double t1 = (-bterm + sqrtTerm) / (2 * aterm);
      double t2 = (-bterm - sqrtTerm) / (2 * aterm);
      double xi1 = xp + t1 * costerm;
      double yi1 = yp + t1 * sinterm;
      double dx1 = xi1 - xp;
      double dy1 = yi1 - yp;
      double dist1 = Math.sqrt(dx1 * dx1 + dy1 * dy1);
      double xi2 = xp + t2 * costerm;
      double yi2 = yp + t2 * sinterm;
      double dx2 = xi2 - xp;
      double dy2 = yi2 - yp;
      double dist2 = Math.sqrt(dx2 * dx2 + dy2 * dy2);
      double xi = 0;
      double yi = 0;
      //System.out.println("int1=" + xi1 + " " + yi1);
      //System.out.println("int2=" + xi2 + " " + yi2);
      if (dist1 <= dist2) {
        xi = xi1;
        yi = yi1;
      } else {
        xi = xi2;
        yi = yi2;
      }
      intersection.setLocation(xi, yi);
      try {
        transform.inverseTransform(intersection, intersection);
      } catch (NoninvertibleTransformException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    //    double term = ellipseLength * ellipseLength * (1 - ptDst.getY() * ptDst.getY() / (ellipseWidth * ellipseWidth));
    //    if (term > 0) {
    //      if (ptDst.getX() < 0) {
    //        System.out.println("FOO");
    //        intersection.setLocation(-Math.sqrt(term), ptDst.getY());
    //      } else {
    //        intersection.setLocation(Math.sqrt(term), ptDst.getY());
    //        System.out.println("BAR");
    //      }
    //      try {
    //        transform.inverseTransform(intersection, intersection);
    //      } catch (NoninvertibleTransformException e) {
    //        e.printStackTrace();
    //      }
    //    }

    return intersection;
  }

  //  public static void main(final String[] args) {
  //    double x = -10;
  //    double y = 8;
  //    double slope = -1;
  //    double length = 6;
  //    double width = 3;
  //    double cx = 0;
  //    double cy = 0;
  //    Point2D result1 = intersection(x, y, 1, slope, slope, length, width, cx, cy);
  //    System.out.println("RESULT1 = " + result1.toString());
  //    x = 3;
  //    y = -8;
  //    Point2D result2 = intersection(x, y, 0, 1, slope, length, width, cx, cy);
  //    System.out.println("RESULT2 = " + result2.toString());
  //  }

}
