/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.input;


import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.io.util.MultiVolumeTraceIterator;


public class TraceReader3d implements Runnable {

  private MultiVolumeTraceIterator _iterator;

  private BlockingQueue<TraceData[]> _buffer;

  private int _numWorkers;

  public TraceReader3d(final MultiVolumeTraceIterator iterator, final int numWorkers) {
    _iterator = iterator;
    _numWorkers = numWorkers;
    _buffer = new ArrayBlockingQueue<TraceData[]>(10);
  }

  public void run() {
    // Continue running, so long as the trace iterator has more traces.
    while (_iterator.hasNext()) {
      TraceData[] traceData = _iterator.next();
      try {
        _buffer.put(traceData);
      } catch (InterruptedException ex) {
        ex.printStackTrace();
      }
    }
    try {
      // Add an end-of-data signal for each worker.
      for (int i = 0; i < _numWorkers; i++) {
        _buffer.put(new TraceData[0]);
      }
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }
  }

  public TraceData[] next() {
    try {
      return _buffer.take();
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }
    return new TraceData[0];
  }

  public float getCompletion() {
    return _iterator.getCompletion();
  }
}
