/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot.action;


import java.awt.geom.Point2D;

import org.eclipse.swt.SWT;
import org.geocraft.abavo.crossplot.IABavoCrossplot;
import org.geocraft.abavo.ellipse.EllipseRegionsModel.EllipseType;
import org.geocraft.ui.plot.action.AbstractPlotMouseAction;
import org.geocraft.ui.plot.action.PlotActionMask;
import org.geocraft.ui.plot.action.PlotMouseEvent;
import org.geocraft.ui.plot.defs.ActionMaskType;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPolygon;


/**
 * The mouse action for ending a crossplot ellipse definition.
 */
public class EndEllipseDefinition extends AbstractPlotMouseAction {

  protected IABavoCrossplot _crossplot;

  protected EllipseType _ellipseType;

  public EndEllipseDefinition(final IABavoCrossplot crossplot, final EllipseType ellipseType) {
    super(new PlotActionMask(ActionMaskType.MOUSE_DOWN, 1, 1, SWT.NONE), "AB Ellipse End", "End the AB Ellipse definition.");

    _crossplot = crossplot;
    _ellipseType = ellipseType;
  }

  public void actionPerformed(final PlotMouseEvent event) {
    Point2D.Double modelCoord = event.getModelCoord();
    IPlotPolygon ellipseActive = _crossplot.getEllipseLayer(_ellipseType).getActiveEllipse();
    IPlotPolygon ellipseStatic = _crossplot.getEllipseLayer(_ellipseType).getStaticEllipse();
    ellipseStatic.blockUpdate();
    if (modelCoord == null || ellipseActive == null || ellipseStatic == null) {
      return;
    }

    for (int i = 0; i < ellipseActive.getPointCount(); i++) {
      IPlotPoint point = ellipseActive.getPoint(i);
      ellipseStatic.getPoint(i).moveTo(point.getX(), point.getY());
    }
    ellipseActive.setVisible(false);
    ellipseActive.deselect();
    ellipseStatic.setVisible(true);
    ellipseStatic.unblockUpdate();
    ellipseStatic.updated();

    _crossplot.setCursorStyle(SWT.CURSOR_ARROW);
    _crossplot.setDefaultActions();

    if (_ellipseType.equals(EllipseType.Selection)) {
      _crossplot.selectionPolygonDefined(_crossplot.getEllipseLayer(EllipseType.Selection).getStaticEllipse());
    } else {
      _crossplot.ellipseDefined(_ellipseType);
    }

  }

}
