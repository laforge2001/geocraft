/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.multiplot;


import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.geocraft.ui.viewer.ViewerHelper;


/**
 * The factory for create view parts capable of displaying multiple viewers
 * in a single gridded layout.
 */
public class MultiPlotFactory {

  /**
   * Creates a view part for displaying multiple viewers in a single gridded layout.
   * 
   * @param numColumns the number of columns in the gridded layout.
   * @return the multi-viewer view part.
   */
  public static MultiPlotPart createPart(final int numColumns) throws PartInitException {
    String id = Integer.toString(ViewerHelper.getNextViewerId());
    IViewPart viewPart = ViewerHelper.getViewerWindow().getActivePage().showView(
        "org.geocraft.ui.multiplot.MultiPlotPart", id, IWorkbenchPage.VIEW_ACTIVATE);
    MultiPlotPart multiPlotPart = (MultiPlotPart) viewPart;
    multiPlotPart.setNumColumns(numColumns);
    return multiPlotPart;
  }
}
