/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.algorithm.thread;


import javax.swing.SwingWorker;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.core.service.logging.ILogger;


public abstract class MultiThreadedAlgorithmWorker<T> extends SwingWorker<T, Void> {

  private ILogger _logger;

  private IProgressMonitor _monitor;

  private int _workerId;

  private boolean _paused = false;

  protected String _pauseObject = "PAUSE";

  public MultiThreadedAlgorithmWorker(int workerId) {
    _workerId = workerId;
  }

  public int getWorkerId() {
    return _workerId;
  }

  protected final ILogger getLogger() {
    return _logger;
  }

  protected final IProgressMonitor getMonitor() {
    return _monitor;
  }

  public void init(ILogger logger, IProgressMonitor monitor) {
    _logger = logger;
    _monitor = monitor;
  }

  public synchronized void pause() {
    _paused = true;
  }

  public synchronized void resume() {
    _paused = false;
    synchronized (_pauseObject) {
      _pauseObject.notify();
    }
  }

  public synchronized boolean isPaused() {
    return _paused;
  }

  public abstract String getMessage();
}
