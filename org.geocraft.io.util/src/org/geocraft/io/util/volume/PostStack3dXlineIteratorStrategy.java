/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.util.volume;


import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.seismic.PostStack3d;


public class PostStack3dXlineIteratorStrategy extends AbstractPostStack3dIteratorStrategy {

  /** The array of traces in the current xline "slab". */
  private final Trace[][][] _xlineSlabs;

  /** The number of overlapping xlines when incrementing in the xline direction. */
  private final int _xlineBlockOverlap;

  public PostStack3dXlineIteratorStrategy(final PostStack3d primVolume, final AreaOfInterest aoi, final int inlineBlockSize, final int xlineBlockSize, final int inlineIncrement, final int xlineIncrement, final float zStart, final float zEnd, final PostStack3d... secnVolumes) {
    super(primVolume, aoi, inlineBlockSize, xlineBlockSize, inlineIncrement, xlineIncrement, zStart, zEnd, secnVolumes);
    int numVolumes = 1 + secnVolumes.length;
    _xlineSlabs = new Trace[numVolumes][xlineBlockSize][_numInlines];
    _xlineBlockOverlap = Math.max(0, _xlineBlockSize - _xlineIncrement);
  }

  public synchronized TraceBlock3d next() {

    float inlineStart = _inlineRange.getStart();
    float inlineEnd = _inlineRange.getEnd();

    if (!_isInitialized) {
      int xlineIndex0 = _currentXlineLoop * _xlineIncrement;

      for (int volumeIndex = 0; volumeIndex < _numVolumes; volumeIndex++) {
        PostStack3d volume = getVolume(volumeIndex);
        for (int xlineIndex = 0; xlineIndex < _xlineBlockSize; xlineIndex++) {
          float xline = volume.getXlineStart() + (xlineIndex0 + xlineIndex) * volume.getXlineDelta();
          Trace[] xlineTraces = volume.getXline(xline, inlineStart, inlineEnd, _zStart, _zEnd).getTraces();
          for (int inlineIndex = 0; inlineIndex < _numInlines; inlineIndex++) {
            Trace trace = xlineTraces[inlineIndex];
            double x = trace.getX();
            double y = trace.getY();

            // Filter out any traces not contained in the area-of-interest.
            if (_aoi == null || _aoi.contains(x, y)) {
              _xlineSlabs[volumeIndex][xlineIndex][inlineIndex] = trace;
            } else {
              _xlineSlabs[volumeIndex][xlineIndex][inlineIndex] = null;
            }
          }
        }
      }

      _isInitialized = true;
    } else {
      _currentInlineLoop++;

      // If the xline loop counter has reach the end of an inline, then advance the inline loop counter.
      if (_currentInlineLoop >= _numInlineLoops) {
        _currentXlineLoop++;
        _currentInlineLoop = 0;

        int xlineBlockSize = _xlineBlockSizes[_currentXlineLoop];

        // read new set of traces.
        int xlineIndex0 = _currentXlineLoop * _xlineIncrement;

        // Shift the current traces, only if there is overlap.
        int xlineBlockShift = _xlineBlockSize - _xlineBlockOverlap;
        if (_xlineBlockOverlap > 0) {
          for (int volumeIndex = 0; volumeIndex < _numVolumes; volumeIndex++) {
            for (int xlineIndex = 0; xlineIndex < _xlineBlockOverlap; xlineIndex++) {
              _xlineSlabs[volumeIndex][xlineIndex] = _xlineSlabs[volumeIndex][xlineIndex + xlineBlockShift];
            }
          }
        }

        // Read in the next required slab of traces.
        int xlineIndexMax = _xlineBlockSize - 1;
        int xlineIndexMin = xlineIndexMax - (xlineBlockShift - 1);
        for (int volumeIndex = 0; volumeIndex < _numVolumes; volumeIndex++) {
          PostStack3d volume = getVolume(volumeIndex);
          for (int xlineIndex = xlineIndexMin; xlineIndex <= xlineIndexMax && xlineIndex < xlineBlockSize; xlineIndex++) {
            float xline = volume.getXlineStart() + (xlineIndex0 + xlineIndex) * volume.getXlineDelta();
            Trace[] xlineTraces = volume.getXline(xline, inlineStart, inlineEnd, _zStart, _zEnd).getTraces();
            for (int inlineIndex = 0; inlineIndex < _numInlines; inlineIndex++) {
              Trace trace = xlineTraces[inlineIndex];
              double x = trace.getX();
              double y = trace.getY();

              // Filter out any traces not contained in the area-of-interest.
              if (_aoi == null || _aoi.contains(x, y)) {
                _xlineSlabs[volumeIndex][xlineIndex][inlineIndex] = trace;
              } else {
                _xlineSlabs[volumeIndex][xlineIndex][inlineIndex] = null;
              }
            }
          }
        }
      }
    }
    int inlineIndex0 = _currentInlineLoop * _inlineIncrement;

    // Initialize a new array of traces and insert the appropriate traces.
    int inlineBlockSize = _inlineBlockSizes[_currentInlineLoop];
    int xlineBlockSize = _xlineBlockSizes[_currentXlineLoop];
    Trace[][][] traces = new Trace[_numVolumes][inlineBlockSize][xlineBlockSize];
    for (int volumeIndex = 0; volumeIndex < _numVolumes; volumeIndex++) {
      for (int inlineIndex = 0; inlineIndex < inlineBlockSize; inlineIndex++) {
        for (int xlineIndex = 0; xlineIndex < xlineBlockSize; xlineIndex++) {
          traces[volumeIndex][inlineIndex][xlineIndex] = _xlineSlabs[volumeIndex][xlineIndex][inlineIndex0
              + inlineIndex];
        }
      }
    }

    // Reset the initialization flag.
    _isInitialized = true;

    // Increment the current loop.
    _currentLoop++;

    // Return the block of traces.
    return new TraceBlock3d(_numVolumes, inlineBlockSize, xlineBlockSize, traces);
  }

  @Override
  public synchronized String getMessage() {
    if (_xlineBlockSize == 1) {
      int currentInlineIndex0 = _currentInlineLoop * _inlineIncrement;
      int currentInlineIndex1 = currentInlineIndex0 + _inlineBlockSize - 1;
      int currentXlineIndex0 = _currentXlineLoop * _xlineIncrement;
      float inline0 = _inlineRange.getStart() + currentInlineIndex0 * _inlineRange.getDelta();
      float inline1 = _inlineRange.getStart() + currentInlineIndex1 * _inlineRange.getDelta();
      float xline = _xlineRange.getStart() + currentXlineIndex0 * _xlineRange.getDelta();
      return "Current slice: Xline " + xline + ", Inlines " + inline0 + "-" + inline1;
    }
    return super.getMessage();
  }
}
