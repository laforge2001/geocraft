/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.util.subvolume;


import java.util.Iterator;
import java.util.NoSuchElementException;

import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.aoi.ZRangeConstant;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PostStack3d.StorageOrder;


/**
 * This class defines an iterator for reading sub-volumes from one or more PostStack3d volumes.
 */
public class SubVolumeIterator implements Iterator<Trace[][][]> {

  /** The iterator read strategy. */
  private ISubVolumeIteratorStrategy _readStrategy;

  public SubVolumeIterator(final PostStack3d ps3d, final StorageOrder preferredOrder, final float startZ, final float endZ, final int numInlinesInSubVolume, final int numXlinesInSubVolume) {
    this(null, ps3d, preferredOrder, startZ, endZ, numInlinesInSubVolume, numXlinesInSubVolume);

  }

  public SubVolumeIterator(final AreaOfInterest aoi, final PostStack3d ps3d, final StorageOrder preferredOrder, final int numInlinesInSubVolume, final int numXlinesInSubVolume) {
    // Default the z range to that of the primary volume.
    float zStart = ps3d.getZStart();
    float zEnd = ps3d.getZEnd();
    if (aoi != null && aoi.hasZRange()) {
      // But if the AOI is defined and has a given z range, then use it.
      ZRangeConstant zRange = aoi.getZRange();
      zStart = zRange.getZStart();
      zEnd = zRange.getZEnd();
    }

    // Create the reading strategy, depending on the preferred order of the volume. 
    switch (preferredOrder) {
      case INLINE_XLINE_Z:
        _readStrategy = new InlineSubVolumeIteratorStrategy(ps3d, aoi, numInlinesInSubVolume, numXlinesInSubVolume,
            zStart, zEnd);
        break;
      case XLINE_INLINE_Z:
        _readStrategy = new XlineSubVolumeIteratorStrategy(ps3d, aoi, numInlinesInSubVolume, numXlinesInSubVolume,
            zStart, zEnd);
        break;
      default:
        throw new IllegalArgumentException("Invalid storage order: " + preferredOrder);
    }
  }

  /**
   * Constructs a sub-volume iterator for a PostStack3d volume.
   * It allows the user to specify the area-of-interest, the PostStack3d volume
   * to process, the z range, as well as the direction to process (Inline, Xline, etc).
   * 
   * @param aoi the area-of-interest to process over.
   * @param primVolume the primary PostStack3d volume to iterate thru.
   * @param preferredOrder the processing order.
   * @param startZ the starting z.
   * @param endZ the ending z.
   * @param numInlines the number of inlines in each sub-volume.
   * @param numXlines the number of xlines in each sub-volume.
   */
  public SubVolumeIterator(final AreaOfInterest aoi, final PostStack3d ps3d, final StorageOrder preferredOrder, final float startZ, final float endZ, final int numInlinesInSubVolume, final int numXlinesInSubVolume) {

    // Create the reading strategy, depending on the preferred order of the volume. 
    switch (preferredOrder) {
      case INLINE_XLINE_Z:
        _readStrategy = new InlineSubVolumeIteratorStrategy(ps3d, aoi, numInlinesInSubVolume, numXlinesInSubVolume,
            startZ, endZ);
        break;
      case XLINE_INLINE_Z:
        _readStrategy = new XlineSubVolumeIteratorStrategy(ps3d, aoi, numInlinesInSubVolume, numXlinesInSubVolume,
            startZ, endZ);
        break;
      default:
        throw new IllegalArgumentException("Invalid storage order: " + preferredOrder);
    }
  }

  /**
   * Constructs a sub-volume iterator for a PostStack3d volume.
   * It allows the user to specify the area-of-interest, the PostStack3d volume
   * to process, the z range, as well as the direction to process (Inline, Xline, etc).
   * 
   * @param aoi the area-of-interest to process over.
   * @param primVolume the primary PostStack3d volume to iterate thru.
   * @param preferredOrder the processing order.
   * @param startZ the starting z.
   * @param endZ the ending z.
   * @param numInlines the number of inlines in each sub-volume.
   * @param numXlines the number of xlines in each sub-volume.
   */
  public SubVolumeIterator(final AreaOfInterest aoi, final PostStack3d ps3d, final StorageOrder preferredOrder, final float startZ, final float endZ, final int numInlinesInSubVolume, final int numXlinesInSubVolume, final int windowIncrement) {
    // Create the reading strategy, depending on the preferred order of the volume. 
    switch (preferredOrder) {
      case INLINE_XLINE_Z:
        _readStrategy = new InlineSubVolumeIteratorThreadStrategy(ps3d, aoi, numInlinesInSubVolume,
            numXlinesInSubVolume, startZ, endZ, windowIncrement);
        break;
      case XLINE_INLINE_Z:
        _readStrategy = new XlineSubVolumeIteratorThreadStrategy(ps3d, aoi, numInlinesInSubVolume,
            numXlinesInSubVolume, startZ, endZ, windowIncrement);
        break;
      default:
        throw new IllegalArgumentException("Invalid storage order: " + preferredOrder);
    }
  }

  /**
   * Constructs a sub-volume iterator for one or more PostStack3d volumes.
   * It allows the user to specify the area-of-interest, the PostStack3d volumes (primary and secondaries)
   * to process, the z range, as well as the direction to process (Inline, Xline, etc).
   * 
   * @param aoi the area-of-interest to process over.
   * @param primVolume the primary PostStack3d volume to iterate thru.
   * @param preferredOrder the processing order.
   * @param startZ the starting z.
   * @param endZ the ending z.
   * @param numInlines the number of inlines in each sub-volume.
   * @param numXlines the number of xlines in each sub-volume.
   * @param secnVolumes the secondary PostStack3d volumes to iterate thru.
   */
  public SubVolumeIterator(final AreaOfInterest aoi, final PostStack3d primVolume, final StorageOrder preferredOrder, final float startZ, final float endZ, final int numInlinesInSubVolume, final int numXlinesInSubVolume, final PostStack3d... secnVolumes) {
    // Create the reading strategy, depending on the preferred order of the volume. 
    switch (preferredOrder) {
      case INLINE_XLINE_Z:
        _readStrategy = new InlineSubVolumeIteratorStrategy(primVolume, aoi, numInlinesInSubVolume,
            numXlinesInSubVolume, startZ, endZ, secnVolumes);
        break;
      case XLINE_INLINE_Z:
        _readStrategy = new XlineSubVolumeIteratorStrategy(primVolume, aoi, numInlinesInSubVolume,
            numXlinesInSubVolume, startZ, endZ, secnVolumes);
        break;
      default:
        throw new IllegalArgumentException("Invalid storage order: " + preferredOrder);
    }
  }

  public SubVolumeIterator(final AreaOfInterest aoi, final PostStack3d ps3d, final StorageOrder preferredOrder, final float startZ, final float endZ, final int numInlinesInSubVolume, final int numXlinesInSubVolume, final int windowIncrement, final PostStack3d... secnVolumes) {
    this(aoi, ps3d, preferredOrder, startZ, endZ, numInlinesInSubVolume, numXlinesInSubVolume, windowIncrement,
        windowIncrement, secnVolumes);
  }

  /**
   * Constructs a sub-volume iterator for one or more PostStack3d volumes.
   * It allows the user to specify the area-of-interest, the PostStack3d volumes (primary and secondaries)
   * to process, the z range, as well as the direction to process (Inline, Xline, etc).
   * 
   * @param aoi the area-of-interest to process over.
   * @param primVolume the primary PostStack3d volume to iterate thru.
   * @param preferredOrder the processing order.
   * @param startZ the starting z.
   * @param endZ the ending z.
   * @param numInlines the number of inlines in each sub-volume.
   * @param numXlines the number of xlines in each sub-volume.
   * @param secnVolumes the secondary PostStack3d volumes to iterate thru.
   */
  public SubVolumeIterator(final AreaOfInterest aoi, final PostStack3d ps3d, final StorageOrder preferredOrder, final float startZ, final float endZ, final int numInlinesInSubVolume, final int numXlinesInSubVolume, final int inlineIncrement, final int xlineIncrement, final PostStack3d... secnVolumes) {
    // Create the reading strategy, depending on the preferred order of the volume. 
    switch (preferredOrder) {
      case INLINE_XLINE_Z:
        _readStrategy = new InlineSubVolumeIteratorThreadStrategy(ps3d, aoi, numInlinesInSubVolume,
            numXlinesInSubVolume, startZ, endZ, inlineIncrement, xlineIncrement, secnVolumes);
        break;
      case XLINE_INLINE_Z:
        _readStrategy = new XlineSubVolumeIteratorThreadStrategy(ps3d, aoi, numInlinesInSubVolume,
            numXlinesInSubVolume, startZ, endZ, inlineIncrement, xlineIncrement, secnVolumes);
        break;
      default:
        throw new IllegalArgumentException("Invalid storage order: " + preferredOrder);
    }
  }

  public synchronized boolean hasNext() {
    return !_readStrategy.isDone();
  }

  public synchronized Trace[][][] next() {
    if (hasNext()) {
      return _readStrategy.next();
    }
    throw new NoSuchElementException("No more traces.");
  }

  public synchronized void remove() {
    throw new UnsupportedOperationException("Cannot remove traces.");
  }

  /**
   * Gets the iterator status message.
   * 
   * @return the iterator status message.
   */
  public synchronized String getMessage() {
    return _readStrategy.getMessage();
  }

  /**
   * Gets the iterator completion (in the range 0-100).
   * 
   * @return the iterator completion (in the range 0-100).
   */
  public synchronized float getCompletion() {
    return _readStrategy.getCompletion();
  }

}
