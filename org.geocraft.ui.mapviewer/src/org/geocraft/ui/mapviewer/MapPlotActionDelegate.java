package org.geocraft.ui.mapviewer;


import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.geocraft.ui.repository.RepositoryViewData;


public class MapPlotActionDelegate implements IWorkbenchWindowActionDelegate {

  public void dispose() {
    // TODO Auto-generated method stub
  }

  public void init(final IWorkbenchWindow window) {
    // TODO Auto-generated method stub
  }

  public void run(final IAction action) {
    MapPlotAction plotAction = new MapPlotAction(RepositoryViewData.getSelectedEntities());
    plotAction.run();
  }

  public void selectionChanged(final IAction action, final ISelection selection) {
    // does nothing for now
  }
}
