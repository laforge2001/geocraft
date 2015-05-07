/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.crossplot.action;


import java.awt.geom.Point2D;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.geocraft.abavo.crossplot.IABavoCrossplot;
import org.geocraft.abavo.polygon.PolygonRegionsModel.PolygonType;
import org.geocraft.core.common.util.UserAssistMessageBuilder;
import org.geocraft.ui.plot.action.AbstractPlotMouseAction;
import org.geocraft.ui.plot.action.PlotActionMask;
import org.geocraft.ui.plot.action.PlotMouseEvent;
import org.geocraft.ui.plot.defs.ActionMaskType;
import org.geocraft.ui.plot.defs.PointInsertionMode;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPointGroup;
import org.geocraft.ui.plot.object.IPlotPolygon;
import org.geocraft.ui.plot.object.PlotPoint;


/**
 * The mouse action for ending a crossplot polygon definition.
 */
public class EndPolygonDefinition extends AbstractPlotMouseAction {

  protected IABavoCrossplot _crossplot;

  protected PolygonType _polygonType;

  private final int _polygonIndex;

  private boolean _polygonSymmetry;

  public EndPolygonDefinition(final IABavoCrossplot crossplot, final PolygonType polygonType, final int polygonIndex, boolean polygonSymmetry) {
    super(new PlotActionMask(ActionMaskType.MOUSE_DOWN, 2, 1, 0), "AB Polygon End", "End the AB Polygon definition.");

    _crossplot = crossplot;
    _polygonType = polygonType;
    _polygonIndex = polygonIndex;
    _polygonSymmetry = polygonSymmetry;
  }

  public void actionPerformed(final PlotMouseEvent event) {
    addLastPoint(event);
    cleanup();
  }

  public void cleanup() {
    IPlotPolygon polygonActive = _crossplot.getPolygonLayer(_polygonIndex).getPolygon();
    polygonActive.deselect();

    _crossplot.setDefaultActions();
    _crossplot.setCursorStyle(SWT.CURSOR_ARROW);

    if (_polygonType.equals(PolygonType.Selection)) {
      _crossplot.selectionPolygonDefined(_crossplot.getPolygonLayer(-1).getPolygon());
    } else {
      _crossplot.regionPolygonDefined(_polygonIndex, _polygonSymmetry);
    }
  }

  private void addLastPoint(final PlotMouseEvent event) {
    IModelSpaceCanvas canvas = event.getModelCanvas();
    IPlotPointGroup pointGroup = (IPlotPointGroup) canvas.getActiveShape();
    if (pointGroup == null) {
      UserAssistMessageBuilder message = new UserAssistMessageBuilder();
      message.setDescription("Could not add point.");
      message.addReason("No active shape.");
      message.addSolution("Select an editable shape to make it active.");
      MessageDialog.openError(canvas.getComposite().getShell(), "Point Addition Error", message.toString());
      return;
    }
    if (!pointGroup.isEditable()) {
      UserAssistMessageBuilder message = new UserAssistMessageBuilder();
      message.setDescription("Could not add point.");
      message.addReason("Active shape is not editable.");
      message.addSolution("Select an editable shape to make it active.");
      MessageDialog.openError(canvas.getComposite().getShell(), "Point Addition Error", message.toString());
      return;
    }
    Point2D.Double modelCoord = event.getModelCoord();
    if (modelCoord != null) {
      double x = modelCoord.getX();
      double y = modelCoord.getY();
      IPlotPoint point = new PlotPoint(x, y, 0);
      point.setPropertyInheritance(true);
      pointGroup.addPoint(point, PointInsertionMode.LAST);
      canvas.setPointInsertionMode(PointInsertionMode.LAST);
    }
  }
}
