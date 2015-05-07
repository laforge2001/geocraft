/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */

package org.geocraft.io.util;


import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.seismic.PostStack2dLine;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PreStack3d;
import org.geocraft.core.model.seismic.SeismicDataset;
import org.geocraft.core.model.seismic.PostStack3d.StorageOrder;


/**
 * Defines a factory for creating the various trace iterators that may be
 * required by geophysical algorithm developers. Trace iterators can depend on a
 * number of parameters, including the following: the volume, an
 * area-of-interest, start and end z values, and processing order (INLINE_XLINE,
 * XLINE_INLINE, etc).
 */
public class TraceIteratorFactory {

  /**
   * Constructs a trace iterator for a PostStack3d volume. This iterator will
   * operate over the entire volume, in the volume storage order, from the
   * volume start z to the end z.
   * 
   * @param ps3d the PostStack3d volume to iterate thru.
   */
  public static TraceIterator create(final PostStack3d ps3d) {
    return create(ps3d, null, ps3d.getPreferredOrder());
  }

  /**
   * Constructs a trace iterator for a PreStack3d volume. This iterator will
   * operate over the entire volume, in the volume storage order, from the
   * volume start z to the end z.
   * 
   * @param ps3d the PreStack3d volume to iterate thru.
   */
  public static TraceIterator create(final PreStack3d ps3d) {
    return create(ps3d, null, ps3d.getPreferredOrder());
  }

  /**
   * Constructs a trace iterator for a PostStack2d volume. This iterator will
   * operate over the entire volume, in the volume storage order, from the
   * volume start z to the end z.
   * 
   * @param ps2d the PostStack2d volume to iterate thru.
   */
  public static TraceIterator create(final PostStack2dLine ps2d) {
    return create(ps2d, null, StorageOrder.INLINE_XLINE_Z);
  }

  /**
   * Constructs a trace iterator for a PostStack3d volume. This iterator will
   * operate over the specified area-of-interest, in the volume storage order,
   * from the volume start z to the end z.
   * 
   * @param aoi the area-of-interest.
   * @param ps3d the PostStack3d volume to iterate thru.
   */
  public static TraceIterator create(final PostStack3d ps3d, final AreaOfInterest aoi) {
    return create(ps3d, aoi, ps3d.getPreferredOrder());
  }

  /**
   * Constructs a trace iterator for a PostStack2d volume. This iterator will
   * operate over the specified area-of-interest, in the volume storage order,
   * from the volume start z to the end z.
   * 
   * @param aoi the area-of-interest.
   * @param ps2d the PostStack2d volume to iterate thru.
   */
  public static TraceIterator create(final PostStack2dLine ps2d, final AreaOfInterest aoi) {
    return create(ps2d, aoi, StorageOrder.INLINE_XLINE_Z);
  }

  /**
   * Constructs a trace iterator for a PostStack3d volume. This iterator will
   * operate over the entire volume, in the specified process order, from the
   * volume start z to the end z.
   * 
   * @param ps3d
   *            the PostStack3d volume to iterate thru.
   * @param processOrder
   *            the processing order to iterator.
   */
  public static TraceIterator create(final PostStack3d ps3d, final StorageOrder processOrder) {
    return create(ps3d, null, processOrder);
  }

  /**
   * Constructs a trace iterator for a seismic dataset. This iterator will
   * operate over the specified area-of-interest, in the specified process
   * order, from the volume start z to the end z.
   * 
   * @param aoi the area-of-interest.
   * @param dataset the seismic dataset to iterate thru.
   * @param processOrder the processing order.
   */
  public static TraceIterator create(final SeismicDataset dataset, final AreaOfInterest aoi,
      final StorageOrder processOrder) {
    return create(dataset, aoi, processOrder, dataset.getZStart(), dataset.getZEnd());
  }

  /**
   * Constructs a trace iterator for a prestack seismic dataset. This iterator will
   * operate over the specified area-of-interest, in the specified process
   * order, from the volume start z to the end z.
   * 
   * @param aoi the area-of-interest.
   * @param dataset the prestack seismic dataset to iterate thru.
   * @param processOrder the processing order.
   */
  public static TraceIterator create(final PreStack3d dataset, final AreaOfInterest aoi,
      final PreStack3d.StorageOrder processOrder) {
    return create(dataset, aoi, processOrder, dataset.getZStart(), dataset.getZEnd());
  }

  /**
   * Constructs a trace iterator for a seismic dataset volume. This iterator will
   * operate over the specified area-of-interest, in the specified process
   * order, from the specified start z to the end z.
   * 
   * @param aoi the area-of-interest.
   * @param dataset the seismic dataset to iterate thru.
   * @param processOrder the processing order.
   * @param startZ the starting z.
   * @param endZ the ending z.
   */
  public static TraceIterator create(final SeismicDataset dataset, final AreaOfInterest aoi,
      final StorageOrder processOrder, final float startZ, final float endZ) {
    if (dataset instanceof PostStack3d) {
      return new TraceIterator(aoi, (PostStack3d) dataset, processOrder, startZ, endZ);
    } else if (dataset instanceof PostStack2dLine) {
      return new TraceIterator(aoi, (PostStack2dLine) dataset, startZ, endZ);
    }
    throw new IllegalArgumentException("The seismic dataset must be either a PostStack2d or PostStack3d.");
  }

  /**
   * Constructs a trace iterator for a seismic dataset volume. This iterator will
   * operate over the specified area-of-interest, in the specified process
   * order, from the specified start z to the end z.
   * 
   * @param aoi the area-of-interest.
   * @param dataset the prestack seismic dataset to iterate thru.
   * @param processOrder the processing order.
   * @param startZ the starting z.
   * @param endZ the ending z.
   */
  private static TraceIterator create(final PreStack3d dataset, final AreaOfInterest aoi,
      final PreStack3d.StorageOrder processOrder, final float startZ, final float endZ) {
    return new TraceIterator(aoi, dataset, processOrder, startZ, endZ);
  }

  public static TraceIterator create(final ITraceIteratorStrategy strategy) {
    return new TraceIterator(strategy);
  }
}
