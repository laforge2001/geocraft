/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.mapviewer;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.viewer.ViewerHelper;


/**
 * Defines an action for plotting one or more objects in a map viewer.
 * A new map viewer will be opened and the specified objects will be added into it.
 */
public class MapPlotAction extends Action {

  /** The collection of objects to be plotted in map view. */
  private final List<Object> _objects;

  public MapPlotAction(final Object[] objects) {
    _objects = Collections.synchronizedList(new ArrayList<Object>());
    for (Object object : objects) {
      _objects.add(object);
    }
  }

  @Override
  public void run() {
    try {
      // Create a new map viewer.
      String id = Integer.toString(ViewerHelper.getNextViewerId());
      IViewPart viewPart = ViewerHelper.getViewerWindow().getActivePage().showView(
          "org.geocraft.ui.mapviewer.MapViewPart", id, IWorkbenchPage.VIEW_ACTIVATE);
      if (viewPart != null) {
        // Add the collection of objects into the map viewer.
        ((MapViewPart) viewPart).addObjects(_objects.toArray());
      }
    } catch (PartInitException ex) {
      ServiceProvider.getLoggingService().getLogger(getClass()).error(ex.toString(), ex);
    }
  }
}
