/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.repository.action;


import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.well.Well;
import org.geocraft.core.model.well.WellCheckShot;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.message.Topic;
import org.geocraft.ui.repository.RepositoryViewData;


public class SetActiveTimeDepthTable implements IWorkbenchWindowActionDelegate {

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
    Entity[] entities = RepositoryViewData.getSelectedEntities();
    for (Entity entity : entities) {
      if (entity instanceof WellCheckShot) {
        WellCheckShot checkshot = (WellCheckShot) entity;
        Well well = checkshot.getWell();
        well.getWellBore().setDefaultCheckShot(checkshot);
        ServiceProvider.getMessageService().publish(Topic.REPOSITORY_OBJECT_UPDATED, well);
      }
    }
  }
}
