/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.navigation;


import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.core.model.seismic.PreStack3d;
import org.geocraft.ui.sectionviewer.ISectionViewer;


public class PreStack3dNavigationAction extends Action {

  private AbstractNavigationDialog _navigationDialog;

  private final PreStack3d _prestack;

  private final ISectionViewer _viewer;

  public PreStack3dNavigationAction(final PreStack3d prestack, final ISectionViewer viewer) {
    _prestack = prestack;
    _viewer = viewer;
  }

  @Override
  public void run() {
    // We are already on the SWT thread, but we ensure this way that the setActive() will be called after the open().
    Display.getDefault().asyncExec(new Runnable() {

      public void run() {
        if (_navigationDialog == null) {
          Shell shell = new Shell(Display.getDefault().getActiveShell(), SWT.ON_TOP | SWT.APPLICATION_MODAL);
          _navigationDialog = new PreStack3dNavigationDialog(shell, _prestack, _viewer);
        }
        if (_navigationDialog.getShell() == null || _navigationDialog.getShell().isDisposed()) {
          _navigationDialog.create();
          _navigationDialog.getShell().pack();
          Point size = _navigationDialog.getShell().computeSize(500, SWT.DEFAULT);
          _navigationDialog.getShell().setSize(size);
        }
        _navigationDialog.getShell().setActive();
        _navigationDialog.open();
      }
    });
  }
}
