/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.algorithm;


import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.algorithm.thread.MultiThreadedAlgorithmWorker;


public class WorkerMessageQuery implements Runnable {

  private IProgressMonitor _monitor;

  private MultiThreadedAlgorithmWorker[] _workers;

  private boolean _isDone;

  private int _counter = 0;

  public WorkerMessageQuery(IProgressMonitor monitor, MultiThreadedAlgorithmWorker[] workers) {
    _monitor = monitor;
    _workers = Arrays.copyOf(workers, workers.length);
    _isDone = false;
  }

  public void run() {

    while (!_isDone) {
      // Loop thru the workers, looking for the 1st one that is still running.
      for (MultiThreadedAlgorithmWorker worker : _workers) {
        if (!worker.isDone()) {
          // Update the monitor with the worker's message.
          _monitor.subTask("Worker # " + worker.getWorkerId() + ": " + worker.getMessage());
          break;
        }
      }

      // Sleep for awhile until the next query.
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        return;
      }
    }
  }

}
