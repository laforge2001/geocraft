/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.action.zoom;


import java.awt.event.ActionEvent;

import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.action.AbstractPlotAction;


public class UndoZoomAction extends AbstractPlotAction {

  public UndoZoomAction(final IPlot plot) {
    super(plot);
  }

  @SuppressWarnings("unused")
  public void actionPerformed(final ActionEvent e) {
    // TODO: implement this
    //    _plot.setActionMode(ActionMode.General);
    //    for (IModelSpace modelSpace : _plot.getModelSpaces()) {
    //      PlotBounds bounds = modelSpace.getBoundsModel().getAutoBounds();
    //      modelSpace.setBounds(bounds, false);
    //    }
    //    Cursor cursor = PlotCursorFactory.getDefaultCursor();
    //    _plot.getModelCanvas().getComponent().setCursor(cursor);
    //    _plot.getModelCanvas().checkAspectRatio();
    //    _plot.getModelCanvas().update(UpdateLevel.Resize);
  }
}
