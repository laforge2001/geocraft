/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.util.volume;


import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PostStack3d.StorageOrder;


/**
 * This class defines an iterator for traversing thru one or more volumes
 * in either the inline or xline direction, block at a time.
 * <p>
 * The direction of process can either be specified or will be taken from
 * the primary volume.
 */
public final class PostStack3dBlockIterator extends PostStack3dIterator<TraceBlock3d> {

  /** The direction-based strategy for iterating thru the volume(s). */
  private IPostStack3dIteratorStrategy _iteratorStrategy;

  /**
   * Constructs a block iterator.
   * 
   * @param primVolume the primary PostStack3d volume.
   * @param preferredOrder the order of traversal.
   * @param aoi the area-of-interest (null for none).
   * @param numInlines the number of inlines in a block.
   * @param numXlines the number of xlines in a block.
   * @param inlineIncrement the inline increment.
   * @param xlineIncrement the xline increment.
   * @param zStart the start z value.
   * @param zEnd the ending z value.
   * @param secnVolumes the optional array of secondary PostStack3d volumes.
   */
  public PostStack3dBlockIterator(final PostStack3d primVolume, final StorageOrder preferredOrder, final AreaOfInterest aoi, final int numInlines, final int numXlines, final int inlineIncrement, final int xlineIncrement, final float zStart, final float zEnd, final PostStack3d... secnVolumes) {
    // Initialize the buffer.
    switch (preferredOrder) {
      case INLINE_XLINE_Z:
        // Create an inline iterator strategy.
        _iteratorStrategy = new PostStack3dInlineIteratorStrategy(primVolume, aoi, numInlines, numXlines,
            inlineIncrement, xlineIncrement, zStart, zEnd, secnVolumes);
        break;
      case XLINE_INLINE_Z:
        // Create an xline iterator strategy.
        _iteratorStrategy = new PostStack3dXlineIteratorStrategy(primVolume, aoi, numInlines, numXlines,
            inlineIncrement, xlineIncrement, zStart, zEnd, secnVolumes);
        break;
      default:
        throw new IllegalArgumentException("Not supported for z-slice iterations.");
    }
  }

  public boolean hasNext() {
    return !_iteratorStrategy.isDone();
  }

  public TraceBlock3d next() {
    return _iteratorStrategy.next();
  }

  @Override
  public int getCompletion() {
    return _iteratorStrategy.getCompletion();
  }

  @Override
  public String getMessage() {
    return _iteratorStrategy.getMessage();
  }

  @Override
  public int getTotalWork() {
    return _iteratorStrategy.getTotalWork();
  }

}
