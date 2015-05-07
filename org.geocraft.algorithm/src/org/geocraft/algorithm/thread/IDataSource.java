/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.algorithm.thread;


import org.geocraft.io.util.volume.BufferStatus;


public interface IDataSource<T> {

  T get();

  void close();

  int getTotalWork();

  BufferStatus getBufferStatus();

  boolean isEndOfData();
}
