package org.geocraft.ui.repository.action;


import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.geocraft.core.common.progress.TaskRunner;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PreStack3d;
import org.geocraft.core.model.seismic.SeismicDataset;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.repository.RepositoryViewData;


public class CreateFoldMapAction implements IWorkbenchWindowActionDelegate {

  @Override
  public void dispose() {
    // Nothing to do.
  }

  @Override
  public void init(IWorkbenchWindow window) {
    // Nothing to do.
  }

  @Override
  public void selectionChanged(IAction action, ISelection selection) {
    // Nothing to do.
  }

  @Override
  public void run(IAction action) {
    Display.getDefault().asyncExec(new Runnable() {

      public void run() {
        // Get the repository.
        IRepository repository = ServiceProvider.getRepository();
        if (repository == null) {
          ServiceProvider.getLoggingService().getLogger(getClass()).error("Could not find repository.");
        }

        // Get the entities currently selected in the repository.
        Entity[] entities = RepositoryViewData.getSelectedEntities();
        for (Entity entity : entities) {
          Class klass = entity.getClass();
          // Create and run the fold map task if the volume entity is supported.
          if (klass.equals(PostStack3d.class) || klass.equals(PreStack3d.class)) {
            CreateFoldMapTask task = new CreateFoldMapTask((SeismicDataset) entity);
            TaskRunner.runTask(task, "Creating Fold Map: " + entity.getDisplayName(), TaskRunner.INTERACTIVE);
          }
        }
      }

    });
  }

}
