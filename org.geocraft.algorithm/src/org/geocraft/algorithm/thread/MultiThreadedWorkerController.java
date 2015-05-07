/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.algorithm.thread;


import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.io.util.volume.BufferStatus;


public class MultiThreadedWorkerController implements Runnable {

  private IProgressMonitor _monitor;

  private IDataSource _dataSource;

  private MultiThreadedAlgorithmWorker[] _workers;

  private int _numThreads;

  public MultiThreadedWorkerController(IProgressMonitor monitor, IDataSource dataSource, MultiThreadedAlgorithmWorker[] workers) {
    _monitor = monitor;
    _dataSource = dataSource;
    _workers = Arrays.copyOf(workers, workers.length);
    _numThreads = workers.length;
  }

  public void run() {
    while (!_monitor.isCanceled() && !_dataSource.isEndOfData()) {
      BufferStatus bufferStatus = _dataSource.getBufferStatus();
      switch (bufferStatus) {
        case RUNNING_OUT:
          System.out.println("Buffer Running Out: Preparing to pause worker...");
          // Be sure to always keep at least 1 worker.
          for (int i = _numThreads - 1; i > 0; i--) {
            MultiThreadedAlgorithmWorker worker = _workers[i];
            if (!worker.isPaused()) {
              System.out.println("Pausing worker #" + worker.getWorkerId());
              worker.pause();
              break;
            }
          }
          break;
        case FILLING_UP:
          System.out.println("Buffer Filling Up: Preparing to resume worker...");
          for (int i = 0; i < _numThreads; i++) {
            MultiThreadedAlgorithmWorker worker = _workers[i];
            if (worker.isPaused()) {
              System.out.println("Resuming worker #" + worker.getWorkerId());
              worker.resume();
              break;
            }
          }
          break;
        default:
          // Do nothing.
      }

      try {
        int numThreadsRunning = 0;
        for (MultiThreadedAlgorithmWorker worker : _workers) {
          if (!worker.isPaused()) {
            numThreadsRunning++;
          }
        }
        System.out.println("# Threads Running: " + numThreadsRunning);
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    // Resume any paused workers so they can end.
    System.out.println("Resume any paused workers so they can wrapup...");
    for (MultiThreadedAlgorithmWorker worker : _workers) {
      if (worker.isPaused()) {
        System.out.println("Resuming " + worker.getWorkerId() + " so it can wrapup.");
        worker.resume();
      }
    }
  }
}
