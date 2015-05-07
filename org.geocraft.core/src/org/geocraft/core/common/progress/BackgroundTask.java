/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.progress;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.core.service.logging.ILogger;


/**
 * Abstract implementation of IRepositoryTask to aid in API evolution.
 */
public abstract class BackgroundTask {

  public abstract Object compute(ILogger logger, final IProgressMonitor monitor) throws CoreException;

}
