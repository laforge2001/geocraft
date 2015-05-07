/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.util.volume;


import org.geocraft.core.model.datatypes.Trace;


/**
 * This class defines a 3D "block" of traces (e.g. Trace[][][]).
 * <p>
 * The dimensions are indexed as followed:
 * 1) volume index
 * 2) inline index
 * 3) xline index
 */
public final class TraceBlock3d extends TraceBlock<Trace[][][]> {

  /** The number of inlines in the block. */
  private int _numInlines;

  /** The number of xlines in the block. */
  private int _numXlines;

  /**
   * Constructs a 3D block of traces.
   * 
   * @param numVolumes the number of volumes (size of 1st dimension).
   * @param numInlines the number of inlines (size of 2nd dimension).
   * @param numXlines the number of xlines (size of 3rd dimension).
   * @param traces the 3D array of traces.
   */
  public TraceBlock3d(int numVolumes, int numInlines, int numXlines, Trace[][][] traces) {
    super(numVolumes, traces);
    _numInlines = numInlines;
    _numXlines = numXlines;
  }

  /**
   * Returns the number of inlines (size of 2nd dimension).
   * 
   * @return the number of inlines.
   */
  public int getNumInlines() {
    return _numInlines;
  }

  /** 
   * Returns the number of xlines (size of 3rd dimension).
   * 
   * @return the number of xlines.
   */
  public int getNumXlines() {
    return _numXlines;
  }

  /**
   * Returns the 2D array of traces for the given volume.
   * 
   * @param volumeIndex the volume index.
   * @return the array of traces.
   */
  public Trace[][] getTraces(int volumeIndex) {
    return getTraces()[volumeIndex];
  }
}
