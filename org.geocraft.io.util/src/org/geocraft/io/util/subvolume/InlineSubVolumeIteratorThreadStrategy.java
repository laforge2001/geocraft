/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.util.subvolume;


import java.util.Map;

import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.Trace.Status;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.seismic.PostStack3d;


public class InlineSubVolumeIteratorThreadStrategy extends AbstractSubVolumeIteratorThreadStrategy {

  public InlineSubVolumeIteratorThreadStrategy(PostStack3d primVolume, AreaOfInterest aoi, int numInlinesInSubVolume, int numXlinesInSubVolume, float zStart, float zEnd, int windowLength, PostStack3d... secnVolumes) {
    super(primVolume, aoi, numInlinesInSubVolume, numXlinesInSubVolume, zStart, zEnd, windowLength, windowLength,
        secnVolumes);
  }

  /**
   * @param ps3d
   * @param aoi
   * @param numInlinesInSubVolume
   * @param numXlinesInSubVolume
   * @param startZ
   * @param endZ
   */
  public InlineSubVolumeIteratorThreadStrategy(PostStack3d ps3d, AreaOfInterest aoi, int numInlinesInSubVolume, int numXlinesInSubVolume, float startZ, float endZ, int windowIncrement) {
    this(ps3d, aoi, numInlinesInSubVolume, numXlinesInSubVolume, startZ, endZ, windowIncrement, windowIncrement,
        new PostStack3d[0]);
  }

  /**
   * @param ps3d
   * @param aoi
   * @param numInlinesInSubVolume
   * @param numXlinesInSubVolume
   * @param startZ
   * @param endZ
   * @param inlineIncrement
   * @param xlineIncrement
   * @param secnVolumes
   */
  public InlineSubVolumeIteratorThreadStrategy(PostStack3d ps3d, AreaOfInterest aoi, int numInlinesInSubVolume, int numXlinesInSubVolume, float startZ, float endZ, int inlineIncrement, int xlineIncrement, PostStack3d[] secnVolumes) {
    super(ps3d, aoi, numInlinesInSubVolume, numXlinesInSubVolume, startZ, endZ, inlineIncrement, xlineIncrement,
        secnVolumes);
  }

  /**
   * @return
   */
  @Override
  protected Trace[][][] processSubcubesFromCache() {
    int numVolumes = 1 + _secnVolumes.length;
    PostStack3d[] volumes = new PostStack3d[numVolumes];
    volumes[0] = _primVolume;
    for (int i = 0; i < _secnVolumes.length; i++) {
      volumes[i + 1] = _secnVolumes[i];
    }
    int numSamples = 1 + Math.round((_endZ - _startZ) / _primVolume.getZDelta());
    Trace[][][] subVolume = new Trace[numVolumes][_numInlinesInSubVolume][_numXlinesInSubVolume];

    for (int k = 0; k < numVolumes; k++) {
      for (int i = 0; i < _numInlinesInSubVolume; i++) {
        int iIndex = _inlineIndex + i;

        Map<Integer, Trace[]> traceMap = _traceMap.get(volumes[k]);
        if (traceMap.isEmpty()) {
          return null;
        }
        Trace[] traces = traceMap.get(iIndex);
        for (int j = 0; j < _numXlinesInSubVolume; j++) {
          int jindex = _xlineIndex + j;
          if (traces != null) {
            if (jindex >= 0 && jindex < _numXlines) {
              Trace trace = traces[jindex];
              if (_aoi == null || _aoi.contains(trace.getX(), trace.getY())) {
                subVolume[k][i][j] = trace;
              } else {
                subVolume[k][i][j] = trace;
                trace.setStatus(Status.Missing);
              }
            } else {
              subVolume[k][i][j] = new Trace(_startZ, _primVolume.getZDelta(), _primVolume.getZUnit(), Float.NaN,
                  Float.NaN, new float[numSamples], Status.Missing);
            }
          } else {
            subVolume[k][i][j] = new Trace(_startZ, _primVolume.getZDelta(), _primVolume.getZUnit(), Float.NaN,
                Float.NaN, new float[numSamples], Status.Missing);
          }
        }
      }
    }

    return subVolume;
  }

  /**
   * @return
   */
  @Override
  protected void readNextWindow() {

    for (int k = 0; k < _numInlinesInSubVolume; ++k) {
      // Read the next line of traces.
      float[] inlines = new float[_numXlines];
      float[] xlines = new float[_numXlines];
      int currentInlineIncrement = Math.min(_inlineIndex + k, _numInlines - 1);
      for (int i = 0; i < _numXlines; i++) {
        inlines[i] = _inlineStart + currentInlineIncrement * _primVolume.getInlineDelta();
        xlines[i] = _xlineStart + i * _primVolume.getXlineDelta();
      }

      if (_inlineIndex >= _numInlines - 1) {
        _threadDone = true;
        break;
      }

      if (_aoi == null || containsAoi(_aoi, _primVolume, _secnVolumes, inlines, xlines)) {
        TraceData traceData = _primVolume.getTraces(inlines, xlines, _startZ, _endZ);
        Map<Integer, Trace[]> traceMap = _traceMap.get(_primVolume);
        traceMap.put(currentInlineIncrement, traceData.getTraces());

        for (PostStack3d secnVolume : _secnVolumes) {
          TraceData traceData2 = secnVolume.getTraces(inlines, xlines, _startZ, _endZ);
          Map<Integer, Trace[]> traceMap2 = _traceMap.get(secnVolume);
          traceMap2.put(currentInlineIncrement, traceData2.getTraces());
        }
      } else {
        continue;
      }
    }
  }

  @Override
  public float getCompletion() {
    return 100f * _inlineIndex / _numInlines;
  }

  @Override
  protected boolean isInnerIndexComplete() {
    return _xlineIndex < _numXlines;
  }

  @Override
  protected void incrementInnerIndex() {
    _xlineIndex = _xlineIndex + getXlineIncrement();
  }

  /* (non-Javadoc)
   * @see org.geocraft.io.util.subvolume.AbstractSubVolumeIteratorThreadStrategy#populateBuffer()
   */
  @Override
  public void populateBuffer() {
    while (_xlineIndex < _numXlines) {
      addToBuffer();
      _xlineIndex = _xlineIndex + getXlineIncrement();
    }
  }

  /* (non-Javadoc)
   * @see org.geocraft.io.util.subvolume.AbstractSubVolumeIteratorThreadStrategy#incrementIndex()
   */
  @Override
  public void incrementIndex() {
    _xlineIndex = 0;
    _inlineIndex = Math.min(_inlineIndex + getInlineIncrement(), _numInlines - 1);
  }

}
