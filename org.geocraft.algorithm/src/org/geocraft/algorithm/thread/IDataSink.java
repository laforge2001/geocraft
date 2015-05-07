/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.algorithm.thread;


/**
 * The common interface for all data sinks.
 *
 * @param <T> the type of data to be passed into the data sink.
 */
public interface IDataSink<T> {

  /**
   * Puts data into the data sink.
   * 
   * @param data the data to put.
   */
  void put(T data);

  /**
   * Closes any required resources associated with the data sink.
   */
  void close();
}
