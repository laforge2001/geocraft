/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.util.volume;


import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.SeismicSurvey3d;


/**
 * The abstract base class for all trace iterator strategies
 * for <code>PostStack3d</code>.
 */
public abstract class AbstractPostStack3dIteratorStrategy implements IPostStack3dIteratorStrategy {

  /** The primary volume. */
  protected PostStack3d _primVolume;

  /** The area-of-interest (null for none). */
  protected AreaOfInterest _aoi;

  /** The number of inlines in a block. */
  protected int _inlineBlockSize;

  /** The number of xlines in a block. */
  protected int _xlineBlockSize;

  /** The increment in the inline direction. */
  protected int _inlineIncrement;

  /** The increment in the xline direction. */
  protected int _xlineIncrement;

  /** The inline range (start,end). */
  protected FloatRange _inlineRange;

  /** The xline range (start,end). */
  protected FloatRange _xlineRange;

  /** The starting z value. */
  protected float _zStart;

  /** The ending z value. */
  protected float _zEnd;

  /** The optional array of secondary volumes. */
  protected PostStack3d[] _secnVolumes;

  /** The total number of volumes (primary + secondary). */
  protected int _numVolumes;

  /** The number of inlines. */
  protected int _numInlines;

  /** The number of xlines. */
  protected int _numXlines;

  /** The number of inline loops. */
  protected int _numInlineLoops;

  /** The number of xline loops. */
  protected int _numXlineLoops;

  /** The total number of loops. */
  protected int _totalLoops;

  /** The current loop. */
  protected int _currentLoop;

  /** The array of inline block sizes. */
  protected int[] _inlineBlockSizes;

  /** The array of xline block sizes. */
  protected int[] _xlineBlockSizes;

  /** The current inline loop. */
  protected int _currentInlineLoop;

  /** The current xline loop. */
  protected int _currentXlineLoop;

  /** The flag indicating if the iterator has been initialized with the 1st set of traces. */
  protected boolean _isInitialized;

  public AbstractPostStack3dIteratorStrategy(final PostStack3d primVolume, final AreaOfInterest aoi, final int inlineBlockSize, final int xlineBlockSize, final int inlineIncrement, final int xlineIncrement, final float zStart, final float zEnd, final PostStack3d... secnVolumes) {
    // Validate the volumes all have the same geometry.
    SeismicSurvey3d survey = primVolume.getSurvey();
    for (PostStack3d secnVolume : secnVolumes) {
      if (!survey.matchesGeometry(secnVolume.getSurvey())) {
        throw new IllegalArgumentException("Survey mismatch: " + secnVolume.getDisplayName());
      }
    }

    _primVolume = primVolume;
    _aoi = aoi;
    _inlineBlockSize = inlineBlockSize;
    _xlineBlockSize = xlineBlockSize;
    _inlineIncrement = inlineIncrement;
    _xlineIncrement = xlineIncrement;
    _numInlines = primVolume.getNumInlines();
    _numXlines = primVolume.getNumXlines();
    _inlineRange = primVolume.getInlineRange();
    _xlineRange = primVolume.getXlineRange();
    _zStart = zStart;
    _zEnd = zEnd;
    _secnVolumes = secnVolumes;
    _numVolumes = 1 + secnVolumes.length;

    // Compute the number of inline loops.
    int inlineCounter = 0;
    int numInlines = _primVolume.getNumInlines();
    int inlineIndex1 = 0 + _inlineBlockSize - 1;
    while (inlineIndex1 < numInlines) {
      inlineCounter++;
      inlineIndex1 += _inlineIncrement;
    }
    inlineIndex1 -= _inlineIncrement;
    int inlineRemainder = numInlines - 1 - inlineIndex1;
    if (inlineRemainder > 0) {
      _inlineBlockSizes = new int[inlineCounter + 1];
      for (int i = 0; i < inlineCounter; i++) {
        _inlineBlockSizes[i] = _inlineBlockSize;
      }
      _inlineBlockSizes[inlineCounter] = inlineRemainder;
      _numInlineLoops = inlineCounter + 1;
    } else {
      _inlineBlockSizes = new int[inlineCounter];
      for (int i = 0; i < inlineCounter; i++) {
        _inlineBlockSizes[i] = _inlineBlockSize;
      }
      _numInlineLoops = inlineCounter;
    }

    // Compute the number of xline loops.
    int xlineCounter = 0;
    int numXlines = _primVolume.getNumXlines();
    int xlineIndex1 = 0 + _xlineBlockSize - 1;
    while (xlineIndex1 < numXlines) {
      xlineCounter++;
      xlineIndex1 += _xlineIncrement;
    }
    xlineIndex1 -= _xlineIncrement;
    int xlineRemainder = numXlines - 1 - xlineIndex1;
    if (xlineRemainder > 0) {
      _xlineBlockSizes = new int[xlineCounter + 1];
      for (int i = 0; i < xlineCounter; i++) {
        _xlineBlockSizes[i] = _xlineBlockSize;
      }
      _xlineBlockSizes[xlineCounter] = xlineRemainder;
      _numXlineLoops = xlineCounter + 1;
    } else {
      _xlineBlockSizes = new int[xlineCounter];
      for (int i = 0; i < xlineCounter; i++) {
        _xlineBlockSizes[i] = _xlineBlockSize;
      }
      _numXlineLoops = xlineCounter;
    }

    // Compute the number of total loops.
    _totalLoops = _numInlineLoops * _numXlineLoops;

    // Initialize the current loops;
    _currentLoop = 0;
    _currentInlineLoop = 0;
    _currentXlineLoop = 0;

    _isInitialized = false;
  }

  /**
   * Returns the specified volume.
   * 
   * @param volumeIndex the volume index (0=primary,1=1st secondary,2=2nd secondary,etc).
   * @return the volume.
   */
  public final PostStack3d getVolume(int volumeIndex) {
    PostStack3d volume = _primVolume;
    if (volumeIndex > 0) {
      volume = _secnVolumes[volumeIndex - 1];
    }
    return volume;
  }

  public synchronized String getMessage() {
    int currentInlineIndex0 = _currentInlineLoop * _inlineIncrement;
    int currentInlineIndex1 = currentInlineIndex0 + _inlineBlockSize - 1;
    int currentXlineIndex0 = _currentXlineLoop * _xlineIncrement;
    int currentXlineIndex1 = currentXlineIndex0 + _xlineBlockSize - 1;
    float inline0 = _inlineRange.getStart() + currentInlineIndex0 * _inlineRange.getDelta();
    float inline1 = _inlineRange.getStart() + currentInlineIndex1 * _inlineRange.getDelta();
    float xline0 = _xlineRange.getStart() + currentXlineIndex0 * _xlineRange.getDelta();
    float xline1 = _xlineRange.getStart() + currentXlineIndex1 * _xlineRange.getDelta();
    return "Current block: Inlines " + inline0 + "-" + inline1 + " and Xlines " + xline0 + "-" + xline1;
  }

  public synchronized int getCompletion() {
    return (int) (100f * _currentLoop / _totalLoops);
  }

  public synchronized boolean isDone() {
    return _currentLoop >= _totalLoops;
  }

  /**
   * Returns the total number of loops.
   */
  public synchronized int getTotalWork() {
    return _totalLoops;
  }
}
