/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.util.volume;


import org.geocraft.core.model.datatypes.Trace;


/**
 * This class defines a 1D "block" of traces (e.g. Trace[]).
 * <p>
 * The dimensions are indexed as followed:
 * 1) volume index
 */
public class TraceBlock1d extends TraceBlock<Trace[]> {

  /**
   * Constructs a 1D block of traces.
   * 
   * @param numVolumes the number of volumes (size of 1st dimension).
   * @param traces the 1D array of traces.
   */
  public TraceBlock1d(int numVolumes, Trace[] traces) {
    super(numVolumes, traces);
  }

  /**
   * Returns the trace for the given volume.
   * 
   * @param volumeIndex the volume index.
   * @return the trace.
   */
  public Trace getTrace(int volumeIndex) {
    return getTraces()[volumeIndex];
  }
}
