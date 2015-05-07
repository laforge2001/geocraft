/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.util.volume;


import java.util.Iterator;


public class DataProducer<T extends TraceBlock> implements Runnable {

  private Iterator<T> _iterator;

  private DataBuffer<T> _buffer;

  private boolean _isStarted;

  public DataProducer(Iterator<T> iterator, DataBuffer<T> buffer) {
    _iterator = iterator;
    _buffer = buffer;
    _isStarted = false;
  }

  public final synchronized void start() {
    new Thread(this).start();
  }

  public final synchronized void run() {
    if (_isStarted) {
      return;
    }
    _isStarted = true;
    while (_iterator.hasNext()) {
      try {
        _buffer.put(_iterator.next());
      } catch (InterruptedException e) {
        e.printStackTrace();
        break;
      }
    }
    _buffer.setEndOfData();
  }
}
