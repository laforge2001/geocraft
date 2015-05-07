/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot.action;


import java.awt.geom.Point2D;

import org.eclipse.swt.SWT;
import org.geocraft.abavo.crossplot.IABavoCrossplot;
import org.geocraft.abavo.crossplot.IABavoCrossplot.PlotMode;
import org.geocraft.abavo.ellipse.EllipseRegionsModel;
import org.geocraft.abavo.ellipse.EllipseUtil;
import org.geocraft.abavo.ellipse.EllipseRegionsModel.EllipseType;
import org.geocraft.ui.plot.action.AbstractPlotMouseAction;
import org.geocraft.ui.plot.action.PlotActionMask;
import org.geocraft.ui.plot.action.PlotMouseEvent;
import org.geocraft.ui.plot.defs.ActionMaskType;
import org.geocraft.ui.plot.object.IPlotPolygon;


/**
 * The mouse action for crossplot ellipse definition.
 */
public class EllipseDefinition extends AbstractPlotMouseAction {

  protected IABavoCrossplot _crossplot;

  protected EllipseType _ellipseType;

  protected double _slope;

  protected double _intercept;

  protected double _xCenter;

  protected double _yCenter;

  protected PlotMode _plotMode;

  public EllipseDefinition(final IABavoCrossplot crossplot, final EllipseType ellipseType, final double slope, final double intercept, final double xCenter, final double yCenter) {
    super(new PlotActionMask(ActionMaskType.MOUSE_MOVE, 0, 0, 0), "AB Ellipse Definition", "Defines the size and shape of an AB Ellipse");

    _crossplot = crossplot;
    _ellipseType = ellipseType;
    _slope = slope;
    _intercept = intercept;
    _xCenter = xCenter;
    _yCenter = yCenter;
    if (ellipseType.equals(EllipseType.Selection)) {
      _plotMode = PlotMode.EllipseSelection;
    } else if (ellipseType.equals(EllipseType.Background)) {
      _plotMode = PlotMode.EllipseMin;
    } else if (ellipseType.equals(EllipseType.Maximum)) {
      _plotMode = PlotMode.EllipseMax;
    } else {
      throw new IllegalArgumentException("Invalid ellipse.");
    }
  }

  public void actionPerformed(final PlotMouseEvent event) {
    Point2D.Double modelCoord = event.getModelCoord();
    //Reactivate this.
    IPlotPolygon ellipseActive = _crossplot.getEllipseLayer(_ellipseType).getActiveEllipse();
    if (modelCoord == null || ellipseActive == null || Double.isNaN(_slope)) {
      return;
    }

    _crossplot.setCursorStyle(SWT.CURSOR_HAND);
    if (!ellipseActive.isSelected()) {
      ellipseActive.select();
    }
    ellipseActive.setVisible(true);
    double x = event.getModelCoord().getX();
    double y = event.getModelCoord().getY();
    double mterm = _slope;
    double[] aterm = { Double.NaN };
    double[] bterm = { Double.NaN };
    double[] ex = new double[EllipseRegionsModel.NUMBER_OF_ELLIPSE_POINTS];
    double[] ey = new double[EllipseRegionsModel.NUMBER_OF_ELLIPSE_POINTS];
    EllipseUtil.computeEllipse(x, y, mterm, _xCenter, _yCenter, aterm, bterm, ex, ey);
    _crossplot.getEllipseLayer(_ellipseType).updateActiveEllipseModel(mterm, aterm[0], bterm[0], _xCenter, _yCenter);
    ellipseActive.blockUpdate();
    for (int i = 0; i < EllipseRegionsModel.NUMBER_OF_ELLIPSE_POINTS; i++) {
      ellipseActive.getPoint(i).moveTo(ex[i], ey[i]);
    }
    ellipseActive.unblockUpdate();
    ellipseActive.updated();
  }

}
