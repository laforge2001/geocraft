/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.action.point;


import java.awt.geom.Point2D;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Point;
import org.geocraft.core.common.util.UserAssistMessageBuilder;
import org.geocraft.ui.plot.action.AbstractPlotMouseAction;
import org.geocraft.ui.plot.action.PlotActionMask;
import org.geocraft.ui.plot.action.PlotMouseEvent;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.object.IPlotPoint;
import org.geocraft.ui.plot.object.IPlotPointGroup;


public class DeletePointAction extends AbstractPlotMouseAction {

  public DeletePointAction(final PlotActionMask mask) {
    super(mask, "Remove Point", "Remove the selected point from the selected shape.");
  }

  public void actionPerformed(final PlotMouseEvent event) {
    IModelSpaceCanvas canvas = event.getModelCanvas();
    if (canvas == null) {
      throw new RuntimeException("No model canvas.");
    }
    IPlotPointGroup pointGroup = (IPlotPointGroup) canvas.getActiveShape();
    Point pixelCoord = event.getPixelCoord();
    if (pointGroup == null) {
      UserAssistMessageBuilder message = new UserAssistMessageBuilder();
      message.setDescription("Could not delete point.");
      message.addReason("No active shape.");
      message.addSolution("Select an editable shape to make it active.");
      MessageDialog.openError(canvas.getComposite().getShell(), "Point Deletion Error", message.toString());
      return;
    }
    if (!pointGroup.isEditable()) {
      UserAssistMessageBuilder message = new UserAssistMessageBuilder();
      message.setDescription("Could not delete point.");
      message.addReason("Active shape is not editable.");
      message.addSolution("Select an editable shape to make it active.");
      MessageDialog.openError(canvas.getComposite().getShell(), "Point Deletion Error", message.toString());
      return;
    }
    if (pixelCoord != null) {
      int px = pixelCoord.x;
      int py = pixelCoord.y;
      IPlotPoint point = canvas.getNearestPoint(pointGroup, px, py);
      if (point != null) {
        Point2D.Double p = new Point2D.Double();
        canvas.transformModelToPixel(point.getModelSpace(), point.getX(), point.getY(), p);
        double dx = px - p.x;
        double dy = py - p.y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist <= canvas.getSelectionTolerance()) {
          pointGroup.removePoint(point);
        }
      }
    }
  }

}
