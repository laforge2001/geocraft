/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.internal.ui.volumeviewer;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.viewer.ViewerHelper;
import org.geocraft.ui.volumeviewer.VolumeViewPart;


/**
 * The volume plot.
 */
public class VolumePlotAction extends Action {

  /** The collection of objects to be plotted in map view. */
  private final List<Object> _objects;

  public VolumePlotAction(final Object[] objects) {
    _objects = Collections.synchronizedList(new ArrayList<Object>());
    for (final Object object : objects) {
      _objects.add(object);
    }
  }

  @Override
  public void run() {
    try {
      final String id = Integer.toString(ViewerHelper.getNextViewerId());
      final IViewPart viewPart = ViewerHelper.getViewerWindow().getActivePage()
          .showView("org.geocraft.ui.volumeviewer.VolumeViewPart", id, IWorkbenchPage.VIEW_ACTIVATE);
      if (viewPart != null) {
        // Add the collection of objects into the map viewer.
        ((VolumeViewPart) viewPart).addObjects(_objects.toArray());
      }
    } catch (final PartInitException e) {
      ServiceProvider.getLoggingService().getLogger(getClass()).warn("Could not open the 3D viewer", e);
    }
  }
}
