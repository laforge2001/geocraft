/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.util.subvolume;


import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.Trace.Status;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.seismic.PostStack3d;


/**
 * This class defines the sub-volume read strategy for iterating thru one or more PostStack3d volumes in the inline direction.
 */
public class InlineSubVolumeIteratorStrategy extends AbstractSubVolumeIteratorStrategy {

  /** The primary volume. */
  protected PostStack3d _primVolume;

  /** The optional secondary volumes. */
  protected PostStack3d[] _secnVolumes;

  /** The number of inlines in each sub-volume. */
  protected int _numInlinesInSubVolume;

  /** The number of xlines in each sub-volume. */
  protected int _numXlinesInSubVolume;

  /** The temporary cache for read traces, used to prevent reading the same trace multiple times. */
  protected Map<PostStack3d, Map<Integer, Trace[]>> _traceMap;

  public InlineSubVolumeIteratorStrategy(PostStack3d primVolume, AreaOfInterest aoi, int numInlinesInSubVolume, int numXlinesInSubVolume, float zStart, float zEnd) {
    this(primVolume, aoi, numInlinesInSubVolume, numXlinesInSubVolume, zStart, zEnd, new PostStack3d[0]);
  }

  public InlineSubVolumeIteratorStrategy(PostStack3d primVolume, AreaOfInterest aoi, int numInlinesInSubVolume, int numXlinesInSubVolume, float zStart, float zEnd, PostStack3d... secnVolumes) {
    super(primVolume, aoi, zStart, zEnd);
    _primVolume = primVolume;
    _secnVolumes = Arrays.copyOf(secnVolumes, secnVolumes.length);
    _numInlinesInSubVolume = numInlinesInSubVolume;
    _numXlinesInSubVolume = numXlinesInSubVolume;
    _traceMap = Collections.synchronizedMap(new HashMap<PostStack3d, Map<Integer, Trace[]>>());
    _traceMap.put(primVolume, Collections.synchronizedMap(new HashMap<Integer, Trace[]>()));
    for (PostStack3d secnVolume : _secnVolumes) {
      _traceMap.put(secnVolume, Collections.synchronizedMap(new HashMap<Integer, Trace[]>()));
    }
  }

  @Override
  protected int getNumLoops() {
    return _numInlines * _numXlines;
  }

  public Trace[][][] next() {
    int numLoops = getNumLoops();
    if (_nextLoop >= numLoops) {
      _message = "Done reading sub-volumes...";
      return new Trace[0][0][0];
    }
    int inlineIndex = _nextLoop / _numXlines;
    int xlineIndex = _nextLoop % _numXlines;
    float inline = _inlineStart + inlineIndex * _primVolume.getInlineDelta();
    float xline = _xlineStart + xlineIndex * _primVolume.getXlineDelta();
    _message = "Reading inline " + inline + " and xline " + xline + ".";
    //    System.out.println(_message);
    if (xlineIndex == 0) {
      if (inlineIndex == 0) {
        for (int k = 0; k < 1 + _numInlinesInSubVolume / 2; k++) {
          int inlineIndexAdj = inlineIndex + k;
          // Read the next line of traces.
          float[] inlines = new float[_numXlines];
          float[] xlines = new float[_numXlines];
          for (int i = 0; i < _numXlines; i++) {
            inlines[i] = _inlineStart + inlineIndexAdj * _primVolume.getInlineDelta();
            xlines[i] = _xlineStart + i * _primVolume.getXlineDelta();
          }

          TraceData traceData = _primVolume.getTraces(inlines, xlines, _startZ, _endZ);
          Map<Integer, Trace[]> traceMap = _traceMap.get(_primVolume);
          traceMap.put(inlineIndexAdj, traceData.getTraces());

          for (PostStack3d secnVolume : _secnVolumes) {
            TraceData traceData2 = secnVolume.getTraces(inlines, xlines, _startZ, _endZ);
            Map<Integer, Trace[]> traceMap2 = _traceMap.get(secnVolume);
            traceMap2.put(inlineIndexAdj, traceData2.getTraces());
          }
        }
      } else {
        // Read the next line of traces.
        int inlineIndexAdj = inlineIndex + _numInlinesInSubVolume / 2;
        if (inlineIndexAdj < _primVolume.getNumInlines()) {
          float[] inlines = new float[_numXlines];
          float[] xlines = new float[_numXlines];
          for (int i = 0; i < _numXlines; i++) {
            inlines[i] = _inlineStart + inlineIndexAdj * _primVolume.getInlineDelta();
            xlines[i] = _xlineStart + i * _primVolume.getXlineDelta();
          }

          TraceData traceData = _primVolume.getTraces(inlines, xlines, _startZ, _endZ);
          Map<Integer, Trace[]> traceMap = _traceMap.get(_primVolume);
          traceMap.put(inlineIndexAdj, traceData.getTraces());
          if (inlineIndex > _numInlinesInSubVolume) {
            traceMap.remove(inlineIndex - 1 - _numInlinesInSubVolume / 2);
          }

          for (PostStack3d secnVolume : _secnVolumes) {
            TraceData traceData2 = secnVolume.getTraces(inlines, xlines, _startZ, _endZ);
            Map<Integer, Trace[]> traceMap2 = _traceMap.get(secnVolume);
            traceMap2.put(inlineIndexAdj, traceData2.getTraces());
            if (inlineIndex > _numInlinesInSubVolume) {
              traceMap2.remove(inlineIndex - 1 - _numInlinesInSubVolume / 2);
            }
          }
        }
      }
    }

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
        int iIndex = inlineIndex - _numInlinesInSubVolume / 2 + i;
        Map<Integer, Trace[]> traceMap = _traceMap.get(volumes[k]);
        Trace[] traces = traceMap.get(iIndex);
        for (int j = 0; j < _numXlinesInSubVolume; j++) {
          int jindex = xlineIndex - _numXlinesInSubVolume / 2 + j;
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

    // Increment the loop index.
    incrementNextLoop();

    return subVolume;
  }
}
