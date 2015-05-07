/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.util.subvolume;


import java.util.Map;

import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.datatypes.Trace.Status;
import org.geocraft.core.model.seismic.PostStack3d;


public class XlineSubVolumeIteratorThreadStrategy extends AbstractSubVolumeIteratorThreadStrategy {

  public XlineSubVolumeIteratorThreadStrategy(PostStack3d primVolume, AreaOfInterest aoi, int numInlinesInSubVolume, int numXlinesInSubVolume, float zStart, float zEnd, int windowLength, PostStack3d... secnVolumes) {
    super(primVolume, aoi, numInlinesInSubVolume, numXlinesInSubVolume, zStart, zEnd, windowLength, secnVolumes);
  }

  /**
   * @param ps3d
   * @param aoi
   * @param numInlinesInSubVolume
   * @param numXlinesInSubVolume
   * @param startZ
   * @param endZ
   */
  public XlineSubVolumeIteratorThreadStrategy(PostStack3d ps3d, AreaOfInterest aoi, int numInlinesInSubVolume, int numXlinesInSubVolume, float startZ, float endZ, int windowIncrement) {
    this(ps3d, aoi, numInlinesInSubVolume, numXlinesInSubVolume, startZ, endZ, windowIncrement, new PostStack3d[0]);
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
  public XlineSubVolumeIteratorThreadStrategy(PostStack3d ps3d, AreaOfInterest aoi, int numInlinesInSubVolume, int numXlinesInSubVolume, float startZ, float endZ, int inlineIncrement, int xlineIncrement, PostStack3d[] secnVolumes) {
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
      for (int i = 0; i < _numXlinesInSubVolume; i++) {
        int iIndex = _xlineIndex + i;
        Map<Integer, Trace[]> traceMap = _traceMap.get(volumes[k]);
        if (traceMap.isEmpty()) {
          return null;
        }
        Trace[] traces = traceMap.get(iIndex);
        for (int j = 0; j < _numInlinesInSubVolume; j++) {
          int jindex = _inlineIndex + j;
          if (traces != null) {
            if (jindex >= 0 && jindex < _numInlines) {
              Trace trace = traces[jindex];
              if (_aoi == null || _aoi.contains(trace.getX(), trace.getY())) {
                subVolume[k][j][i] = trace;
              } else {
                subVolume[k][j][i] = trace;
                trace.setStatus(Status.Missing);
              }
            } else {
              subVolume[k][j][i] = new Trace(_startZ, _primVolume.getZDelta(), _primVolume.getZUnit(), Float.NaN,
                  Float.NaN, new float[numSamples], Status.Missing);
            }
          } else {
            subVolume[k][j][i] = new Trace(_startZ, _primVolume.getZDelta(), _primVolume.getZUnit(), Float.NaN,
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

    for (int k = 0; k < _numXlinesInSubVolume; ++k) {
      // Read the next line of traces.
      float[] inlines = new float[_numInlines];
      float[] xlines = new float[_numInlines];
      int currentXlineIncrement = Math.min(_xlineIndex + k, _numXlines - 1);
      for (int i = 0; i < _numInlines; i++) {
        inlines[i] = _inlineStart + i * _primVolume.getInlineDelta();
        xlines[i] = _xlineStart + currentXlineIncrement * _primVolume.getXlineDelta();
      }

      if (_xlineIndex >= _numXlines - 1) {
        _threadDone = true;
        break;
      }

      if (_aoi == null || containsAoi(_aoi, _primVolume, _secnVolumes, inlines, xlines)) {
        TraceData traceData = _primVolume.getTraces(inlines, xlines, _startZ, _endZ);
        Map<Integer, Trace[]> traceMap = _traceMap.get(_primVolume);
        traceMap.put(currentXlineIncrement, traceData.getTraces());

        for (PostStack3d secnVolume : _secnVolumes) {
          TraceData traceData2 = secnVolume.getTraces(inlines, xlines, _startZ, _endZ);
          Map<Integer, Trace[]> traceMap2 = _traceMap.get(secnVolume);
          traceMap2.put(currentXlineIncrement, traceData2.getTraces());
        }
      } else {
        continue;
      }
    }
  }

  @Override
  public float getCompletion() {
    return 100f * _xlineIndex / _numXlines;
  }

  /* (non-Javadoc)
   * @see org.geocraft.io.util.subvolume.AbstractSubVolumeIteratorThreadStrategy#incrementIndex()
   */
  @Override
  public void incrementIndex() {
    _inlineIndex = 0;
    _xlineIndex = Math.min(_xlineIndex + getXlineIncrement(), _numXlines - 1);

  }

  /* (non-Javadoc)
   * @see org.geocraft.io.util.subvolume.AbstractSubVolumeIteratorThreadStrategy#incrementCondition()
   */
  @Override
  protected boolean isInnerIndexComplete() {
    return _inlineIndex < _numInlines;
  }

  /* (non-Javadoc)
   * @see org.geocraft.io.util.subvolume.AbstractSubVolumeIteratorThreadStrategy#incrementInnerIndex()
   */
  @Override
  protected void incrementInnerIndex() {
    _inlineIndex = _inlineIndex + getInlineIncrement();
  }

}
