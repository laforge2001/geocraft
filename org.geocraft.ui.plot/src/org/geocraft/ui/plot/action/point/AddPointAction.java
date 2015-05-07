/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.action.point;


import java.awt.geom.Point2D;

import org.eclipse.jface.dialogs.MessageDialog;
import org.geocraft.core.common.util.UserAssistMessageBuilder;
import org.geocraft.ui.plot.action.AbstractPlotMouseAction;
import org.geocraft.ui.plot.action.PlotActionMask;
import org.geocraft.ui.plot.action.PlotMouseEvent;
import org.geocraft.ui.plot.defs.PointInsertionMode;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPointGroup;
import org.geocraft.ui.plot.object.PlotPoint;


public class AddPointAction extends AbstractPlotMouseAction {

  private final PointInsertionMode _mode;

  public AddPointAction(final PlotActionMask mask, final PointInsertionMode mode) {
    super(mask, "Add Point", "Add a point to the selected shape.");
    _mask = mask;
    _mode = mode;
  }

  public void actionPerformed(final PlotMouseEvent event) {
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
      pointGroup.addPoint(point, _mode);
      canvas.setPointInsertionMode(_mode);
    }
  }

}
