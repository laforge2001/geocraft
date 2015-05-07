/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.algorithm.thread;


import org.eclipse.core.runtime.IProgressMonitor;


public abstract class SourceSinkMultiThreadedAlgorithmWorker<S, K> extends MultiThreadedAlgorithmWorker<Void> {

  protected final IDataSource<S> _dataSource;

  protected final IDataSink<K> _dataSink;

  private int _counter = 0;

  public SourceSinkMultiThreadedAlgorithmWorker(int workerId, IDataSource<S> dataSource, IDataSink<K> dataSink) {
    super(workerId);
    _dataSource = dataSource;
    _dataSink = dataSink;
  }

  @Override
  protected final Void doInBackground() throws Exception {
    boolean hasMore = true;
    while (hasMore && !getMonitor().isCanceled()) {
      S inputData = null;
      // Keep a lock on the data source only as long as necessary.
      // (i.e. to check if more data available, and if so to get it)
      synchronized (_dataSource) {
        inputData = _dataSource.get();
        hasMore = inputData != null;
        //hasMore = _dataSource.hasNext();
        if (hasMore) {
          _counter++;
          //inputData = _dataSource.next();
        } else {
          continue;
        }
      }

      K outputData = process(inputData, getMonitor());

      // Keep a lock on the data sink only as long as necessary.
      // (i.e. to put the output data)
      synchronized (_dataSink) {
        _dataSink.put(outputData);
      }

      if (isPaused()) {
        synchronized (_pauseObject) {
          _pauseObject.wait();
        }
      }
    }
    return null;
  }

  protected abstract K process(S data, IProgressMonitor monitor);

}
