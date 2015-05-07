/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.ui.io;


import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.geocraft.core.common.progress.SafeTask;
import org.geocraft.core.common.progress.TaskRunner;
import org.geocraft.core.io.RepositoryTask;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;


public class RunIOTask {

  private RepositoryTask _task;

  private String _taskName;

  private int _flags;

  public RunIOTask(RepositoryTask task, String taskName, int flags) {
    _task = task;
    _taskName = taskName;
    _flags = flags;
  }

  public void run() {
    // Run the task.
    Object job = TaskRunner.runTask(_task, _taskName, _flags);
    if (job instanceof SafeTask) {
      //reportJobCompletion((SafeTask) job);
    }
  }

  /**
   * Creates a new thread and waits for the job to complete.
   * It then checks the job status and posts corresponding completion message to log.
   * 
   * @param task the safe task wrapper for the I/O task.
   */
  private void reportJobCompletion(final SafeTask task) {
    Thread thread = new Thread() {

      @Override
      public void run() {
        IStatus status;
        while ((status = task.getStatus()) == null) {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        ILogger logger = ServiceProvider.getLoggingService().getLogger(getClass());
        if (status.equals(Status.OK_STATUS)) {
          // If the task completed with an OK status, then log a "success" message.
          logger.info(_taskName + " completed successfully.");
        } else if (status.equals(Status.CANCEL_STATUS)) {
          // If the task was canceled, then log a "canceled" message.
          logger.warn(_taskName + " canceled.");
        } else {
          // Otherwise the task did not complete, so log an error message.
          logger.error(_taskName + " failed: " + status.getMessage());
        }
      }
    };
    thread.start();

  }
}
