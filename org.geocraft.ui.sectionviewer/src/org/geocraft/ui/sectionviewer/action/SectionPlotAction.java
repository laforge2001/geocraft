/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.action;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.geocraft.ui.sectionviewer.SectionViewerPart;
import org.geocraft.ui.viewer.ViewerHelper;


public class SectionPlotAction extends Action {

  private final List<Object> _objects;

  public SectionPlotAction(final Object object) {
    _objects = Collections.synchronizedList(new ArrayList<Object>());
    _objects.add(object);
  }

  public SectionPlotAction(final Object[] objects) {
    _objects = Collections.synchronizedList(new ArrayList<Object>());
    for (Object object : objects) {
      _objects.add(object);
    }
  }

  @Override
  public void run() {
    try {
      String id = Integer.toString(ViewerHelper.getNextViewerId());
      IWorkbenchWindow window = ViewerHelper.getViewerWindow();
      IWorkbenchPage page = window.getActivePage();
      IViewPart viewPart = page.showView("org.geocraft.ui.sectionviewer.SectionViewerPart", id,
          IWorkbenchPage.VIEW_ACTIVATE);
      if (viewPart != null) {
        ((SectionViewerPart) viewPart).addObjects(_objects.toArray());
      }
    } catch (PartInitException e) {
      e.printStackTrace();
    }
  }
}
