/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer;


import org.eclipse.jface.action.Action;


public abstract class AbstractTreeAction extends Action {

  protected ISectionViewer _viewer;

  public abstract void setRenderer(final SectionViewRenderer renderer);

  public void setViewer(final ISectionViewer viewer) {
    _viewer = viewer;
  }
}
