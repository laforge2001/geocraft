/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package beta.cop.model.points;




public class PointSet3DUtil {

  /**
   * Returns the nearest Point from the given PointSet and MultiPoint input.
   * This assumes that the dimension indices of the input point match the dimension
   * indices of the PointSet and that the input MultiPoint is in World coordinates.
   * 
   * @param points the PointSet to search for the nearest point
   * @param point the point to search against
   * @return the index of the closest point in the pointset
   */
  public int findNearest(IPointSet3D points, IMultiPoint point) {
    double[] wxyz = point.getLocation();
    return findNearest(points, wxyz);
  }

  /**
   * Returns the nearest Point from the given PointSet and MultiPoint input.
   * This assumes that the dimension indices of the input point match the dimension
   * indices of the PointSet and that the input MultiPoint is in World coordinates.
   * 
   * @param points the PointSet to search for the nearest point
   * @param wxyz the coordinates of the point to search against
   * @return
   */
  public static int findNearest(IPointSet3D points, double[] wxyz) {
    double mindist = Double.MAX_VALUE;
    int imin = -100;
    int xdim = points.getXdim();
    int ydim = points.getYdim();
    int zdim = points.getZdim();

    for (int i = 0; i < points.size(); i++) {
      double[] xyz = points.getWorldLocation(i);

      // TODO: use a kd-tree to speed up search ?
      double distance = getDistance(wxyz, xyz, zdim, ydim, xdim);

      if (Double.compare(distance, mindist) < 0) {
        mindist = distance;
        imin = i;
      }
    } // end for i
    return imin;
  }

  /**
   * returns the distance squared between two multipoints 
   * @param point1 
   * @param point2
   * @return the distance 
   */
  public static double getDistance(IMultiPoint point1, IMultiPoint point2) {
    double[] wxyz1 = point1.getLocation();
    double[] wxyz2 = point2.getLocation();
    return getDistance(wxyz1, wxyz2);
  }

  public static double getDistance(double[] wxyz1, double[] wxyz2) {
    return getDistance(wxyz1, wxyz2, 2, 1, 0);
  }

  public static double getDistance(double[] wxyz1, double[] wxyz2, int zdim, int ydim, int xdim) {
    double distance = (wxyz1[zdim] - wxyz2[zdim]) * (wxyz1[zdim] - wxyz2[zdim]);
    distance += (wxyz1[xdim] - wxyz2[xdim]) * (wxyz1[xdim] - wxyz2[xdim]);
    distance += (wxyz1[ydim] - wxyz2[ydim]) * (wxyz1[ydim] - wxyz2[ydim]);
    distance = Math.sqrt(distance);

    return distance;
  }

}
