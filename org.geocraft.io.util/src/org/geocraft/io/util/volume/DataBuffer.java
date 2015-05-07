/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.util.volume;


import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class DataBuffer<T> {

  private BlockingQueue<T> _buffer;

  private boolean _endOfData;

  private int _target;

  public DataBuffer(int capacity) {
    _buffer = new ArrayBlockingQueue<T>(capacity);
    _target = 1 + capacity / 2;
    _endOfData = false;
  }

  public synchronized T get() throws InterruptedException {
    //System.out.println("Buffer.Getting():");
    if (_buffer.size() == 0) {
      if (_endOfData) {
        return null;
      }
      wait();
      if (_endOfData) {
        return null;
      }
    }
    T value = _buffer.take();
    //System.out.println("Buffer.Got(): " + value);
    notify();
    return value;
  }

  public synchronized void put(T value) throws InterruptedException {
    //System.out.println("Buffer.Putting(): " + value);
    if (_buffer.remainingCapacity() == 0) {
      wait();
    }
    _buffer.put(value);
    //System.out.println("Buffer.Put(): " + value);
    notify();
  }

  public synchronized void setEndOfData() {
    //System.out.println("Buffer: setEndofData()");
    _endOfData = true;
    notify();
  }

  public boolean isEndOfData() {
    return _endOfData;
  }

  public synchronized int size() {
    //System.out.println("Buffer: size()");
    return _buffer.size();
  }

  public BufferStatus getBufferStatus() {
    int size = _buffer.size();
    if (size < _target) {
      return BufferStatus.RUNNING_OUT;
    } else if (size > _target) {
      return BufferStatus.FILLING_UP;
    }
    return BufferStatus.STATIC;
  }
}
