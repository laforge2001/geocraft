/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.util.volume;


import org.geocraft.core.model.datatypes.Trace;


/**
 * This class defines a 2D "block" of traces (e.g. Trace[][]).
 * <p>
 * The dimensions are indexed as followed:
 * 1) volume index
 * 2) trace index
 */
public final class TraceBlock2d extends TraceBlock<Trace[][]> {

  /** The number of traces in the block. */
  private int _numTraces;

  /**
   * Constructs a 2D block of traces.
   * 
   * @param numVolumes the number of volumes (size of 1st dimension).
   * @param numTraces the number of traces (size of 2nd dimension).
   * @param traces the 2D array of traces.
   */
  public TraceBlock2d(int numVolumes, int numTraces, Trace[][] traces) {
    super(numVolumes, traces);
    _numTraces = numTraces;
  }

  /**
   * Returns the number of traces (size of 2nd dimension).
   * 
   * @return the number of traces.
   */
  public int getNumTraces() {
    return _numTraces;
  }

  /**
   * Returns the 1D array of traces for the given volume.
   * 
   * @param volumeIndex the volume index.
   * @return the array of traces.
   */
  public Trace[] getTraces(int volumeIndex) {
    return getTraces()[volumeIndex];
  }
}
