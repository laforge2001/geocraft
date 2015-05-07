/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.product.action;


import java.net.URI;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.geocraft.core.common.preferences.PreferencesUtil;
import org.geocraft.core.common.progress.BackgroundTask;
import org.geocraft.core.common.progress.TaskRunner;
import org.geocraft.core.common.util.Utilities;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.core.session.RepositoryIdStore;
import org.geocraft.product.workspace.ChooseWorkspaceData;


public class SaveWorkspaceAsHandler extends AbstractHandler {

  class RestartWorkspaceAction extends OpenWorkspaceAction.WorkspaceMRUAction {

    private OpenWorkspaceAction _action;

    /**
     * @param window
     */
    public RestartWorkspaceAction(OpenWorkspaceAction action, String location, ChooseWorkspaceData data) {
      action.super(location, data);
      _action = action;
    }

    @Override
    public void run() {
      _data.workspaceSelected(_location);
      _data.writePersistedDataForRestart();
      _action.restart(_location);
    }

  }

  /* (non-Javadoc)
   * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {

    DirectoryDialog dialog = new DirectoryDialog(HandlerUtil.getActiveWorkbenchWindowChecked(event).getShell());
    // Determine the current workspace directory
    String directoryName = PreferencesUtil.getPreferencesStore("org.geocraft.ui.io").get("Workspace_DIR",
        Utilities.getWorkingDirectory());
    // determine the parent directory of the current workspace directory
    int ie = directoryName.lastIndexOf(System.getProperty("file.separator"));
    String parentDirectory = directoryName;
    if (ie > 0) {
      parentDirectory = directoryName.substring(0, ie);
    }
    // Set the path to the parent directory of the current workspace directory
    dialog.setFilterPath(parentDirectory);
    final String newWorkspaceDir = dialog.open();

    if (newWorkspaceDir != null) {
      BackgroundTask saveAsTask = new BackgroundTask() {

        /**
         * @param logger  
         */
        @Override
        public Object compute(ILogger logger, IProgressMonitor monitor) throws CoreException {
          RepositoryIdStore.save();

          String currentWorkspaceDir = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();

          IFileSystem fileSystem = EFS.getLocalFileSystem();
          IFileStore currentWorkspaceLoc = fileSystem.getStore(URI.create(currentWorkspaceDir));
          IFileStore newWorkspaceLoc = fileSystem.getStore(URI.create(newWorkspaceDir));
          currentWorkspaceLoc.copy(newWorkspaceLoc, EFS.OVERWRITE, monitor);
          return null;
        }

      };

      try {
        TaskRunner.runTask(saveAsTask, "Saving Workspace...", TaskRunner.JOIN);
      } catch (UnsatisfiedLinkError e) {
        ServiceProvider.getLoggingService().getLogger(getClass()).info(e.getMessage());
      }
      /* File > Switch Session no longer supported
            final ChooseWorkspaceData data = new ChooseWorkspaceData(Platform.getInstanceLocation().getURL());
            new RestartWorkspaceAction(new OpenWorkspaceAction(HandlerUtil.getActiveWorkbenchWindowChecked(event)),
                newWorkspaceDir, data).run();
      */
    }
    return null;
  }
}
