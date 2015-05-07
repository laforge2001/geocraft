/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.util.volume;


import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.seismic.PostStack3d;


public class PostStack3dInlineIteratorStrategy extends AbstractPostStack3dIteratorStrategy {

  /** The array of traces in the current inline "slab". */
  private final Trace[][][] _inlineSlabs;

  /** The number of overlapping inlines when incrementing in the inline direction. */
  private final int _inlineBlockOverlap;

  public PostStack3dInlineIteratorStrategy(final PostStack3d primVolume, final AreaOfInterest aoi, final int inlineBlockSize, final int xlineBlockSize, final int inlineIncrement, final int xlineIncrement, final float zStart, final float zEnd, final PostStack3d... secnVolumes) {
    super(primVolume, aoi, inlineBlockSize, xlineBlockSize, inlineIncrement, xlineIncrement, zStart, zEnd, secnVolumes);
    int numVolumes = 1 + secnVolumes.length;
    _inlineSlabs = new Trace[numVolumes][inlineBlockSize][_numXlines];
    _inlineBlockOverlap = Math.max(0, _inlineBlockSize - _inlineIncrement);
  }

  public synchronized TraceBlock3d next() {

    float xlineStart = _xlineRange.getStart();
    float xlineEnd = _xlineRange.getEnd();

    if (!_isInitialized) {
      int inlineIndex0 = _currentInlineLoop * _inlineIncrement;

      for (int volumeIndex = 0; volumeIndex < _numVolumes; volumeIndex++) {
        PostStack3d volume = getVolume(volumeIndex);
        for (int inlineIndex = 0; inlineIndex < _inlineBlockSize; inlineIndex++) {
          float inline = volume.getInlineStart() + (inlineIndex0 + inlineIndex) * volume.getInlineDelta();
          Trace[] inlineTraces = volume.getInline(inline, xlineStart, xlineEnd, _zStart, _zEnd).getTraces();
          for (int xlineIndex = 0; xlineIndex < _numXlines; xlineIndex++) {
            Trace trace = inlineTraces[xlineIndex];
            double x = trace.getX();
            double y = trace.getY();

            // Filter out any traces not contained in the area-of-interest.
            if (_aoi == null || _aoi.contains(x, y)) {
              _inlineSlabs[volumeIndex][inlineIndex][xlineIndex] = trace;
            } else {
              _inlineSlabs[volumeIndex][inlineIndex][xlineIndex] = null;
            }
          }
        }
      }

      _isInitialized = true;
    } else {
      _currentXlineLoop++;

      // If the xline loop counter has reach the end of an inline, then advance the inline loop counter.
      if (_currentXlineLoop >= _numXlineLoops) {
        _currentInlineLoop++;
        _currentXlineLoop = 0;

        int inlineBlockSize = _inlineBlockSizes[_currentInlineLoop];

        // read new set of traces.
        int inlineIndex0 = _currentInlineLoop * _inlineIncrement;

        // Shift the current traces, only if there is overlap.
        int inlineBlockShift = _inlineBlockSize - _inlineBlockOverlap;
        if (_inlineBlockOverlap > 0) {
          for (int volumeIndex = 0; volumeIndex < _numVolumes; volumeIndex++) {
            for (int inlineIndex = 0; inlineIndex < _inlineBlockOverlap; inlineIndex++) {
              _inlineSlabs[volumeIndex][inlineIndex] = _inlineSlabs[volumeIndex][inlineIndex + inlineBlockShift];
            }
          }
        }

        // Read in the next required slab of traces.
        int inlineIndexMax = _inlineBlockSize - 1;
        int inlineIndexMin = inlineIndexMax - (inlineBlockShift - 1);
        for (int volumeIndex = 0; volumeIndex < _numVolumes; volumeIndex++) {
          PostStack3d volume = getVolume(volumeIndex);
          for (int inlineIndex = inlineIndexMin; inlineIndex <= inlineIndexMax && inlineIndex < inlineBlockSize; inlineIndex++) {
            float inline = volume.getInlineStart() + (inlineIndex0 + inlineIndex) * volume.getInlineDelta();
            Trace[] inlineTraces = volume.getInline(inline, xlineStart, xlineEnd, _zStart, _zEnd).getTraces();
            for (int xlineIndex = 0; xlineIndex < _numXlines; xlineIndex++) {
              Trace trace = inlineTraces[xlineIndex];
              double x = trace.getX();
              double y = trace.getY();

              // Filter out any traces not contained in the area-of-interest.
              if (_aoi == null || _aoi.contains(x, y)) {
                _inlineSlabs[volumeIndex][inlineIndex][xlineIndex] = trace;
              } else {
                _inlineSlabs[volumeIndex][inlineIndex][xlineIndex] = null;
              }
            }
          }
        }
      }
    }
    int xlineIndex0 = _currentXlineLoop * _xlineIncrement;

    // Initialize a new array of traces and insert the appropriate traces.
    int inlineBlockSize = _inlineBlockSizes[_currentInlineLoop];
    int xlineBlockSize = _xlineBlockSizes[_currentXlineLoop];
    Trace[][][] traces = new Trace[_numVolumes][inlineBlockSize][xlineBlockSize];
    for (int volumeIndex = 0; volumeIndex < _numVolumes; volumeIndex++) {
      for (int inlineIndex = 0; inlineIndex < inlineBlockSize; inlineIndex++) {
        for (int xlineIndex = 0; xlineIndex < xlineBlockSize; xlineIndex++) {
          traces[volumeIndex][inlineIndex][xlineIndex] = _inlineSlabs[volumeIndex][inlineIndex][xlineIndex0
              + xlineIndex];
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
    if (_inlineBlockSize == 1) {
      int currentInlineIndex0 = _currentInlineLoop * _inlineIncrement;
      int currentXlineIndex0 = _currentXlineLoop * _xlineIncrement;
      int currentXlineIndex1 = currentXlineIndex0 + _xlineBlockSize - 1;
      float inline = _inlineRange.getStart() + currentInlineIndex0 * _inlineRange.getDelta();
      float xline0 = _xlineRange.getStart() + currentXlineIndex0 * _xlineRange.getDelta();
      float xline1 = _xlineRange.getStart() + currentXlineIndex1 * _xlineRange.getDelta();
      return "Current slice: Inline " + inline + ", Xlines " + xline0 + "-" + xline1;
    }
    return super.getMessage();
  }
}
