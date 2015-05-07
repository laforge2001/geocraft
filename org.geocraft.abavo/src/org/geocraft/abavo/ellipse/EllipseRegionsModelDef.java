/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.ellipse;


import org.geocraft.abavo.crossplot.ABavoCrossplot;
import org.geocraft.abavo.crossplot.CrossplotBoundsModel;
import org.geocraft.abavo.ellipse.EllipseRegionsModel.EllipseType;
import org.geocraft.abavo.ellipse.EllipseRegionsModelEvent.Type;
import org.geocraft.internal.abavo.ABavoCrossplotRegistry;


public abstract class EllipseRegionsModelDef {

  protected static int CENTER_X = 0;

  protected static int CENTER_Y = 1;

  protected static int LENGTH = 2;

  protected static int SLOPE = 3;

  protected static int WIDTH = 4;

  protected static int INNER_X = 0;

  protected static int INNER_Y = 1;

  protected static int OUTER_X = 2;

  protected static int OUTER_Y = 3;

  private String _name;

  public EllipseRegionsModelDef(String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }

  public void updateEllipseRegionsModel(EllipseRegionsModel model) {

    EllipseType[] ellipseTypes = { EllipseType.Background, EllipseType.Maximum };
    for (EllipseType ellipseType : ellipseTypes) {
      EllipseModel ellipseModel = model.getEllipseModel(ellipseType);
      double[] values = getEllipseModelValues(ellipseType);
      ellipseModel.setCenterX(values[CENTER_X]);
      ellipseModel.setCenterY(values[CENTER_X]);
      ellipseModel.setLength(values[LENGTH]);
      ellipseModel.setSlope(values[SLOPE]);
      ellipseModel.setWidth(values[WIDTH]);
    }

    for (RegionsBoundary regionsBoundary : RegionsBoundary.values()) {
      RegionsBoundaryModel regionsModel = model.getRegionsBoundaryModel(regionsBoundary);
      double[] values = getRegionsBoundaryValues(regionsBoundary);
      regionsModel.setInnerX(values[INNER_X]);
      regionsModel.setInnerY(values[INNER_Y]);
      regionsModel.setOuterX(values[OUTER_X]);
      regionsModel.setOuterY(values[OUTER_Y]);
    }

    model.setSymmetricRegions(getSymmetricRegions());

    CrossplotBoundsModel boundsModel = getBoundsModel();
    ABavoCrossplot crossplot = (ABavoCrossplot) ABavoCrossplotRegistry.get().getCrossplots()[0];
    crossplot.applyBounds(boundsModel);
    model.updated(Type.EllipsesUpdated);
    model.updated(Type.RegionBoundariesUpdated);
    model.updated(Type.RegionSymmetryUpdated);
  }

  protected boolean getSymmetricRegions() {
    return true;
  }

  protected abstract CrossplotBoundsModel getBoundsModel();

  protected abstract double[] getEllipseModelValues(EllipseType ellipseType);

  protected abstract double[] getRegionsBoundaryValues(RegionsBoundary regionsBoundary);
}
