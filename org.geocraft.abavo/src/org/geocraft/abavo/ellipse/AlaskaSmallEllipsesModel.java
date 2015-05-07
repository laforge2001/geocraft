/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.ellipse;


import org.geocraft.abavo.crossplot.CrossplotBoundsModel;
import org.geocraft.abavo.crossplot.CrossplotBoundsModel.BoundsType;
import org.geocraft.abavo.ellipse.EllipseRegionsModel.EllipseType;


public class AlaskaSmallEllipsesModel extends EllipseRegionsModelDef {

  public AlaskaSmallEllipsesModel() {
    super("Alaska Small Ellipses");
  }

  @Override
  protected double[] getEllipseModelValues(EllipseType ellipseType) {
    switch (ellipseType) {
      case Background:
        return new double[] { 0.0, 0.0, 2.59652, -0.950, 0.52668 };
      case Maximum:
        return new double[] { 0.0, 0.0, 3.18037, -0.950, 1.26906 };
      default:
        throw new IllegalArgumentException("Invalid ellipse type: " + ellipseType);
    }
  }

  @Override
  protected double[] getRegionsBoundaryValues(RegionsBoundary regionsBoundary) {
    switch (regionsBoundary) {
      case P1toP2:
        return new double[] { 0.250, -0.838, 0.250, -2.500 };
      case P2toP3:
        return new double[] { -0.250, -0.376, -0.250, -2.500 };
      case P3toP4:
        return new double[] { -0.612, 0.000, -2.500, 0.000 };
      case P4toNULL:
        return new double[] { -1.464, 1.137, -2.500, 2.174 };
      case NULLtoN1:
        return new double[] { -1.153, 1.481, -2.171, 2.500 };
      case N1toN2:
        return new double[] { -0.250, 0.838, -0.250, 2.500 };
      case N2toN3:
        return new double[] { 0.250, 0.376, 0.250, 2.500 };
      case N3toN4:
        return new double[] { 0.612, -0.000, 2.500, -0.000 };
      case N4toNULL:
        return new double[] { 1.464, -1.137, 2.500, -2.714 };
      case NULLtoP1:
        return new double[] { 1.153, -1.481, 2.171, -2.500 };
      default:
        throw new IllegalArgumentException("Invalid regions boundary: " + regionsBoundary);
    }
  }

  @Override
  protected CrossplotBoundsModel getBoundsModel() {
    double minX = -2.5;
    double maxX = 2.5;
    double minY = -2.5;
    double maxY = 2.5;
    double xMaxAbs = 2.5;
    double yMaxAbs = 2.5;
    double commonMinMax = Math.max(xMaxAbs, yMaxAbs);
    return new CrossplotBoundsModel(BoundsType.USER_DEFINED, commonMinMax, minX, maxX, minY, maxY);
  }
}
