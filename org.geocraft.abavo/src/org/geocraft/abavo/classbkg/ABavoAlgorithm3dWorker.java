/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.classbkg;


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.geocraft.abavo.input.InputProcess3d;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.logging.ILogger;


public abstract class ABavoAlgorithm3dWorker implements Runnable {

  protected int _workerID;

  protected IProgressMonitor _monitor;

  protected ILogger _logger;

  protected IRepository _repository;

  protected InputProcess3d _inputProcess;

  public ABavoAlgorithm3dWorker(final int workerID, final IProgressMonitor monitor, final ILogger logger, final IRepository repository, final InputProcess3d inputProcess) {
    _workerID = workerID;
    _monitor = new SubProgressMonitor(monitor, 100);
    _logger = logger;
    _repository = repository;
    _inputProcess = inputProcess;
    _inputProcess.setProgressMonitor(_monitor);
  }

  public int getWorkerID() {
    return _workerID;
  }

  public abstract void cleanup();

}