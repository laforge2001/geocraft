/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.ellipse;


import org.geocraft.abavo.crossplot.CrossplotBoundsModel;
import org.geocraft.abavo.crossplot.CrossplotBoundsModel.BoundsType;
import org.geocraft.abavo.ellipse.EllipseRegionsModel.EllipseType;


public class AlaskaBigEllipsesModel extends EllipseRegionsModelDef {

  public AlaskaBigEllipsesModel() {
    super("Alaska Big Ellipses");
  }

  @Override
  protected double[] getEllipseModelValues(EllipseType ellipseType) {
    switch (ellipseType) {
      case Background:
        return new double[] { 0.0, 0.0, 2.44, -1.028, 0.716 };
      case Maximum:
        return new double[] { 0.0, 0.0, 2.96, -1.028, 1.82 };
      default:
        throw new IllegalArgumentException("Invalid ellipse type: " + ellipseType);
    }
  }

  @Override
  protected double[] getRegionsBoundaryValues(RegionsBoundary regionsBoundary) {
    switch (regionsBoundary) {
      case P1toP2:
        return new double[] { 0.2500, -1.1886, 0.2500, -2.5000 };
      case P2toP3:
        return new double[] { -0.2500, -0.7580, -0.2500, -2.500 };
      case P3toP4:
        return new double[] { -0.9605, 0.0000, -2.5000, 0.0000 };
      case P4toNULL:
        return new double[] { -1.7300, 1.2650, -2.5000, 2.0566 };
      case NULLtoN1:
        return new double[] { -1.2168, 1.7643, -1.9325, 2.5000 };
      case N1toN2:
        return new double[] { -0.2500, 1.1886, -0.2500, 2.5000 };
      case N2toN3:
        return new double[] { 0.2500, 0.7580, 0.2500, 2.5000 };
      case N3toN4:
        return new double[] { 0.9605, 0.0000, 2.5000, 0.0000 };
      case N4toNULL:
        return new double[] { 1.7300, -1.2650, 2.5000, -2.0566 };
      case NULLtoP1:
        return new double[] { 1.2168, -1.7643, 1.9325, -2.5000 };
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
