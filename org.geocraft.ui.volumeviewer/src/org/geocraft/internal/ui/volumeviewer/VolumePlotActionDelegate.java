/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.ui.volumeviewer;


import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.geocraft.ui.repository.RepositoryViewData;


public class VolumePlotActionDelegate implements IWorkbenchWindowActionDelegate {

  public void dispose() {
    // TODO Auto-generated method stub
  }

  public void init(final IWorkbenchWindow window) {
    // TODO Auto-generated method stub
  }

  public void run(final IAction action) {
    final VolumePlotAction plotAction = new VolumePlotAction(RepositoryViewData.getSelectedEntities());
    plotAction.run();
  }

  public void selectionChanged(final IAction action, final ISelection selection) {
    // does nothing for now
  }
}
