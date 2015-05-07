/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.util.volume;


/**
 * This class defines a block of traces.
 *
 * @param <T> the type of data (e.g. Trace, Trace[], Trace[][], etc).
 */
public abstract class TraceBlock<T> {

  /** The number of volumes' worth of traces. */
  private int _numVolumes;

  /** The "block" of traces. */
  private T _traces;

  /**
   * Constructs a "block" of traces.
   * 
   * @param numVolumes the number of volumes.
   * @param traces the "block" of traces.
   */
  public TraceBlock(int numVolumes, T traces) {
    _numVolumes = numVolumes;
    _traces = traces;
  }

  /**
   * Returns the number of volumes (size of 1st dimension).
   * 
   * @return the number of volumes.
   */
  public final int getNumVolumes() {
    return _numVolumes;
  }

  /**
   * Returns the "block" of traces.
   * 
   * @return the "block" of traces.
   */
  public final T getTraces() {
    return _traces;
  }
}
