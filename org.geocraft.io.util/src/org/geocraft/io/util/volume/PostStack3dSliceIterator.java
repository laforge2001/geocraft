/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.util.volume;


import java.util.ArrayList;
import java.util.List;

import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PostStack3d.StorageOrder;


/**
 * This class defines an iterator for traversing thru one or more volumes
 * in either the inline or xline direction, slice at a time.
 * <p>
 * The direction of process can either be specified or will be taken from
 * the primary volume.
 */
public class PostStack3dSliceIterator extends PostStack3dIterator<TraceBlock2d> {

  /** The underlying block iterator. */
  private PostStack3dBlockIterator _blockIterator;

  /** The number of inlines in the slice. */
  private int _inlineBlockSize;

  /** The number of xlines in the slice. */
  private int _xlineBlockSize;

  /** The number of volumes. */
  private int _numVolumes;

  /** The number of traces in the slice. */
  private int _numTraces;

  /**
   * Creates an iterator for traversing a set of volumes the inline direction (inline=slowest, xline=fastest).
   * An entire inline of traces is returned each iteration.
   * 
   * @param primVolume the primary PostStack3d volume.
   * @param aoi the area-of-interest (null for none).
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   * @param secnVolumes the optional array of secondary PostStack3d volumes.
   * @return the iterator.
   */
  public static PostStack3dSliceIterator createInlineIterator(PostStack3d primVolume, AreaOfInterest aoi,
      int inlineIncrement, float zStart, float zEnd, PostStack3d... secnVolumes) {
    int xlineIncrement = 1;
    return new PostStack3dSliceIterator(primVolume, StorageOrder.INLINE_XLINE_Z, aoi, inlineIncrement, xlineIncrement,
        zStart, zEnd, secnVolumes);
  }

  /**
   * Creates an iterator for traversing a set of volumes the xline direction (inline=fastest, xline=slowest).
   * An entire xline of traces is returned each iteration.
   * 
   * @param primVolume the primary PostStack3d volume.
   * @param aoi the area-of-interest (null for none).
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   * @param secnVolumes the optional array of secondary PostStack3d volumes.
   * @return the iterator.
   */
  public static PostStack3dSliceIterator createXlineIterator(PostStack3d primVolume, AreaOfInterest aoi,
      int xlineIncrement, float zStart, float zEnd, PostStack3d... secnVolumes) {
    int inlineIncrement = 1;
    return new PostStack3dSliceIterator(primVolume, StorageOrder.XLINE_INLINE_Z, aoi, inlineIncrement, xlineIncrement,
        zStart, zEnd, secnVolumes);
  }

  /**
   * Creates an iterator for traversing a set of volumes in either the inline or xline direction.
   * An entire inline or xline of traces is returned each iteration.
   * The processing direction is based on the primary volume.
   * 
   * @param primVolume the primary PostStack3d volume.
   * @param aoi the area-of-interest (null for none).
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   * @param secnVolumes the optional array of secondary PostStack3d volumes.
   * @return the iterator.
   */
  public static PostStack3dSliceIterator createIterator(PostStack3d primVolume, AreaOfInterest aoi, float zStart,
      float zEnd, PostStack3d... secnVolumes) {
    int xlineIncrement = 1;
    int inlineIncrement = 1;
    return new PostStack3dSliceIterator(primVolume, primVolume.getPreferredOrder(), aoi, inlineIncrement,
        xlineIncrement, zStart, zEnd, secnVolumes);
  }

  /**
   * The basic constructor.
   * 
   * @param primVolume the primary PostStack3d volume.
   * @param preferredOrder the order of traversal.
   * @param aoi the area-of-interest (null for none).
   * @param inlineIncrement the inline increment.
   * @param xlineIncrement the xline increment.
   * @param zStart the start z value.
   * @param zEnd the ending z value.
   * @param secnVolumes the optional array of secondary PostStack3d volumes.
   */
  private PostStack3dSliceIterator(final PostStack3d primVolume, final StorageOrder preferredOrder, final AreaOfInterest aoi, final int inlineIncrement, final int xlineIncrement, final float zStart, final float zEnd, final PostStack3d... secnVolumes) {
    _numVolumes = 1 + secnVolumes.length;
    switch (preferredOrder) {
      case INLINE_XLINE_Z:
        _inlineBlockSize = 1;
        _xlineBlockSize = primVolume.getNumXlines();
        _numTraces = _xlineBlockSize;
        break;
      case XLINE_INLINE_Z:
        _xlineBlockSize = 1;
        _inlineBlockSize = primVolume.getNumInlines();
        _numTraces = _inlineBlockSize;
        break;
      default:
        throw new IllegalArgumentException("Storage order not currently supported: " + preferredOrder);
    }
    _blockIterator = new PostStack3dBlockIterator(primVolume, preferredOrder, aoi, _inlineBlockSize, _xlineBlockSize,
        inlineIncrement, xlineIncrement, zStart, zEnd, secnVolumes);
  }

  public boolean hasNext() {
    return _blockIterator.hasNext();
  }

  /**
   * Reads the next block (an inline or xline) of traces.
   * <p>
   * The 1st index is the volume index.
   * The 2nd index is the inline or xline index.
   * 
   * @return the next block of traces.
   */
  public TraceBlock2d next() {
    // Get the next 3D block of traces from the underlying iterator.
    Trace[][][] blockTraces = _blockIterator.next().getTraces();
    Trace[][] sliceTraces = new Trace[_numVolumes][];
    List<Trace> traceList = new ArrayList<Trace>();
    for (int volumeIndex = 0; volumeIndex < _numVolumes; volumeIndex++) {
      for (int inlineIndex = 0; inlineIndex < _inlineBlockSize; inlineIndex++) {
        for (int xlineIndex = 0; xlineIndex < _xlineBlockSize; xlineIndex++) {
          Trace trace = blockTraces[volumeIndex][inlineIndex][xlineIndex];
          traceList.add(trace);
        }
      }
      sliceTraces[volumeIndex] = traceList.toArray(new Trace[0]);
    }

    // Create and return a 2D block of traces.
    return new TraceBlock2d(_numVolumes, _numTraces, sliceTraces);
  }

  /**
   * Returns the iterator completion status (in the range 0-100).
   * 
   * @return the iterator completion status (in the range 0-100).
   */
  @Override
  public int getCompletion() {
    return _blockIterator.getCompletion();
  }

  /**
   * Returns the iterator status message.
   * 
   * @return the iterator status message.
   */
  @Override
  public String getMessage() {
    return _blockIterator.getMessage();
  }

  @Override
  public int getTotalWork() {
    return _blockIterator.getTotalWork();
  }
}
