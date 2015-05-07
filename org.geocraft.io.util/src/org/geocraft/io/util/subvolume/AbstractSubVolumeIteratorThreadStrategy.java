/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.util.subvolume;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.SeismicSurvey3d;


public abstract class AbstractSubVolumeIteratorThreadStrategy extends AbstractSubVolumeIteratorStrategy implements
    Runnable {

  /** The primary volume. */
  protected PostStack3d _primVolume;

  /** The optional secondary volumes. */
  protected PostStack3d[] _secnVolumes;

  /** The number of inlines in each sub-volume. */
  protected int _numInlinesInSubVolume;

  /** The number of xlines in each sub-volume. */
  protected int _numXlinesInSubVolume;

  protected BlockingQueue<Trace[][][]> _buffer;

  protected int _inlineIndex;

  protected int _xlineIndex;

  protected int _inlineIncrement;

  protected int _xlineIncrement;

  protected volatile boolean _isDone = false;

  protected volatile boolean _threadDone = false;

  protected final static int BUFFER_LENGTH = 1;

  /** The temporary cache for read traces, used to prevent reading the same trace multiple times. */
  protected Map<PostStack3d, Map<Integer, Trace[]>> _traceMap;

  private Thread _readerThread;

  public AbstractSubVolumeIteratorThreadStrategy(PostStack3d primVolume, AreaOfInterest aoi, int numInlinesInSubVolume, int numXlinesInSubVolume, float zStart, float zEnd, int windowIncrement, PostStack3d... secnVolumes) {
    this(primVolume, aoi, numInlinesInSubVolume, numXlinesInSubVolume, zStart, zEnd, windowIncrement, windowIncrement,
        secnVolumes);
  }

  public AbstractSubVolumeIteratorThreadStrategy(PostStack3d primVolume, AreaOfInterest aoi, int numInlinesInSubVolume, int numXlinesInSubVolume, float zStart, float zEnd, int inlineIncrement, int xlineIncrement, PostStack3d... secnVolumes) {
    super(primVolume, aoi, zStart, zEnd);

    _primVolume = primVolume;
    _secnVolumes = Arrays.copyOf(secnVolumes, secnVolumes.length);
    _numInlinesInSubVolume = numInlinesInSubVolume;
    _numXlinesInSubVolume = numXlinesInSubVolume;

    _traceMap = new HashMap<PostStack3d, Map<Integer, Trace[]>>();
    _traceMap.put(primVolume, new HashMap<Integer, Trace[]>());
    for (PostStack3d secnVolume : _secnVolumes) {
      _traceMap.put(secnVolume, new HashMap<Integer, Trace[]>());
    }

    int window = numInlinesInSubVolume;
    _inlineIncrement = Math.min(window, inlineIncrement);
    _inlineIncrement = Math.max(1, _inlineIncrement);

    window = numXlinesInSubVolume;
    _xlineIncrement = Math.min(window, xlineIncrement);
    _xlineIncrement = Math.max(1, _xlineIncrement);

    _buffer = new ArrayBlockingQueue<Trace[][][]>(BUFFER_LENGTH);

    //start the reader thread to populate buffer
    _readerThread = new Thread(this);
    _readerThread.start();

  }

  @Override
  public void finalize() {
    _threadDone = true;
  }

  public boolean containsAoi(AreaOfInterest aoi, PostStack3d primVol, PostStack3d[] secnVols, float[] inlines,
      float[] xlines) {
    SeismicSurvey3d primSurvey = primVol.getSurvey();
    for (int i = 0; i < inlines.length; ++i) {
      double[] xy = primSurvey.transformInlineXlineToXY(inlines[i], xlines[i]);
      if (aoi.contains(xy[0], xy[1])) {
        return true;
      }
    }
    return false;
  }

  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    while (!_threadDone) {
      readNextWindow();
      populateBuffer();
      dumpCurrentWindow();
      incrementIndex();
    }
  }

  /**
   * populate the buffer of cubes to be consumed by the iterator thread
   */
  public void populateBuffer() {
    while (isInnerIndexComplete()) {
      addToBuffer();
      incrementInnerIndex();
    }
  }

  @Override
  public boolean isDone() {
    return _isDone;
  }

  protected int getInlineIncrement() {
    return _inlineIncrement;
  }

  protected int getXlineIncrement() {
    return _xlineIncrement;
  }

  /**
   * check if the current index of the line in the secondary direction (direction that is not preferred order), 
   * is greater than the maximum number of lines in that direction.
   * 
   * @return true if it is otherwise false
   */
  protected abstract boolean isInnerIndexComplete();

  /**
   * increments the current index of the line in the preferred order direction by the subvolume window increment value
   */
  protected abstract void incrementIndex();

  /**
   * increments the current index of the line NOT in the preferred order direction by the subvolume window increment value
   */
  protected abstract void incrementInnerIndex();

  /**
   * reads the next window increment of lines in the preferred order direction into cache. 
   * If an AOI is present then, it will only read the lines that cover the AOI
   */
  protected abstract void readNextWindow();

  /**
   * process the subcubes from data present in the cache
   * 
   * @return a cube of traces 
   */
  protected abstract Trace[][][] processSubcubesFromCache();

  /**
   * @param addme
   * @return
   */
  protected boolean isEmpty(Trace[][][] addme) {
    for (Trace[][] twoTraces : addme) {
      for (Trace[] oneTraces : twoTraces) {
        for (Trace trace : oneTraces) {
          if (!trace.isMissing()) {
            return false;
          }
        }
      }
    }
    return true;
  }

  /**
   * adds the trace cube to the internal buffer for the iterator thread to consume
   */
  protected void addToBuffer() {
    Trace[][][] addme = processSubcubesFromCache();
    if (addme != null && !isEmpty(addme) && !_buffer.offer(addme)) {
      try {
        _buffer.put(addme);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * clears the cache that we've finished consuming
   */
  protected void dumpCurrentWindow() {
    Map<Integer, Trace[]> traceMap = _traceMap.get(_primVolume);
    traceMap.clear();

    for (PostStack3d secnVolume : _secnVolumes) {
      Map<Integer, Trace[]> traceMap2 = _traceMap.get(secnVolume);
      traceMap2.clear();
    }

  }

  /* (non-Javadoc)
   * @see org.geocraft.io.util.subvolume.ISubVolumeIteratorStrategy#next()
   */
  @Override
  public Trace[][][] next() {
    Trace[][][] result = new Trace[0][0][0];

    try {
      if (!_threadDone) {
        result = _buffer.poll();
        while (result == null && !_threadDone) {
          result = _buffer.poll(10, TimeUnit.SECONDS);
        }

      } else if (!_buffer.isEmpty()) {
        result = _buffer.poll();
      }

      if (_buffer.isEmpty() && _threadDone) {
        //the buffer is empty at this point and the producer thread is finished so 
        //set isDone in the iterator to be true
        try {
          if (result == null || result.length == 0) {
            throw new NoSuchElementException("No more traces.. iterator is complete");
          }
          return result;
        } finally {
          _isDone = true;
        }
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    if (result.length == 0) {
      throw new NoSuchElementException("No more traces.. continuing");
    }
    return result;
  }

  /* (non-Javadoc)
   * @see org.geocraft.io.util.subvolume.AbstractSubVolumeIteratorStrategy#getNumLoops()
   */
  @Override
  protected int getNumLoops() {
    // TODO Auto-generated method stub
    return 0;
  }

}
