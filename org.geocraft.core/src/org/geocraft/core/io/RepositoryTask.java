/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.io;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.core.common.progress.BackgroundTask;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;


public abstract class RepositoryTask extends BackgroundTask {

  @Override
  public final Object compute(ILogger logger, IProgressMonitor monitor) throws CoreException {
    run(logger, monitor, ServiceProvider.getRepository());
    return null;
  }

  /**
   * Runs the task.
   * 
   * @param logger the logger for logging debug messages.
   * @param monitor the monitor for reporting job progress.
   * @param repository the repository in which to store entities.
   */
  public abstract void run(ILogger logger, IProgressMonitor monitor, IRepository repository) throws CoreException;
}
