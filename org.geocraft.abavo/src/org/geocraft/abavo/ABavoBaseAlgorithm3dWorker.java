/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo;


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.geocraft.abavo.input.InputProcess3d;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;


public abstract class ABavoBaseAlgorithm3dWorker implements Runnable {

  protected int _workerID;

  protected IProgressMonitor _monitor;

  protected ILogger _logger;

  protected IRepository _repository;

  protected InputProcess3d _inputProcess;

  public ABavoBaseAlgorithm3dWorker(final int workerID, final IProgressMonitor monitor, final ILogger logger, final IRepository repository) {
    _workerID = workerID;
    _monitor = new SubProgressMonitor(monitor, 100);
    _logger = logger;
    _repository = repository;
  }

  public void setInputProcess(final InputProcess3d inputProcess) {
    _inputProcess = inputProcess;
  }

}
