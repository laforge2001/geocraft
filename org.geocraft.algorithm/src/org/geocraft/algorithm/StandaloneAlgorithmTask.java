/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.algorithm;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.core.common.progress.BackgroundTask;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;


/**
 * A simple task for running a algorithm.
 */
public class StandaloneAlgorithmTask extends BackgroundTask {

  /** The algorithm to run. */
  private final StandaloneAlgorithm _algorithm;

  private final String _algorithmName;

  public StandaloneAlgorithmTask(final StandaloneAlgorithm algorithm, String algorithmName) {
    _algorithm = algorithm;
    _algorithmName = algorithmName;
  }

  @Override
  public Object compute(final ILogger logger, final IProgressMonitor monitor) throws CoreException {
    // Execute the run method of the algorithm.
    long startTime = System.currentTimeMillis();
    _algorithm.run(monitor, logger, ServiceProvider.getRepository());
    long endTime = System.currentTimeMillis();
    logger.debug(_algorithm.getClass().getSimpleName() + " ran in " + (endTime - startTime) + "ms");
    return null;
  }

  @Override
  public String toString() {
    return _algorithmName;
  }

}
