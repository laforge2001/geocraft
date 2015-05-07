package org.geocraft.product.action;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.MultiStatus;
import org.geocraft.Activator;
import org.geocraft.core.session.RepositoryIdStore;


public class SaveWorkspaceHandler extends AbstractHandler {

  @SuppressWarnings("unused")
  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {

    final MultiStatus status = new MultiStatus(Activator.PLUGIN_ID, 1, "Saving Workspace", null);
    RepositoryIdStore.save();

    return null;
  }
}
