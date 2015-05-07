/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */

package org.geocraft.io.util;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.seismic.PostStack2dLine;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PreStack3d;
import org.geocraft.core.model.seismic.PostStack3d.StorageOrder;


/**
 * A simple trace iterator that returns blocks of trace data from a volume. The
 * data returned is based upon the preferred direction of the input volume, as
 * well as the specified area-of-interest. If no area-of-interest is specified,
 * then it is based solely on the preferred direction of the input volume.
 */
public class TraceIterator implements Iterator {

  /** The iterator read strategy. */
  private ITraceIteratorStrategy _readStrategy;

  /** The next trace data object. */
  private List<Trace> _nextTraces;

  /** The flag indicating if "missing" traces are to be omitted. */
  private boolean _omitMissingTraces;

  public TraceIterator(final ITraceIteratorStrategy strat) {
    _readStrategy = strat;
  }

  /**
   * Constructs a trace iterator for a PostStack3d volumes.
   * It allows the user to specify the area-of-interest, the PostStack3d volume
   * to process, the z range, as well as the direction to process (Inline, Xline, etc).
   * 
   * @param aoi the area-of-interest to process over.
   * @param ps3d the PostStack3d volume to iterate thru.
   * @param preferredOrder the processing order.
   * @param startZ the starting z.
   * @param endZ the ending z.
   */
  public TraceIterator(final AreaOfInterest aoi, final PostStack3d ps3d, final StorageOrder preferredOrder, final float startZ, final float endZ) {
    // Create the reading strategy, depending on the preferred order of the volume. 
    switch (preferredOrder) {
      case INLINE_XLINE_Z:
        _readStrategy = new InlineTraceIteratorStrategy(ps3d, aoi, startZ, endZ);
        break;
      case XLINE_INLINE_Z:
        _readStrategy = new XlineTraceIteratorStrategy(ps3d, aoi, startZ, endZ);
        break;
      default:
        throw new IllegalArgumentException("Invalid storage order: " + preferredOrder);
    }

    // Set the missing trace flag.
    _omitMissingTraces = true;
  }

  /**
   * Constructs a trace iterator for a PreStack3d volumes.
   * It allows the user to specify the area-of-interest, the PreStack3d volume
   * to process, the z range, as well as the direction to process (Inline, Xline, etc).
   * 
   * @param aoi the area-of-interest to process over.
   * @param ps3d the PreStack3d volume to iterate thru.
   * @param preferredOrder the processing order.
   * @param startZ the starting z.
   * @param endZ the ending z.
   */
  public TraceIterator(final AreaOfInterest aoi, final PreStack3d ps3d, final PreStack3d.StorageOrder preferredOrder, final float startZ, final float endZ) {
    // Create the reading strategy, depending on the preferred order of the volume. 
    _readStrategy = new InlineXlineTraceIteratorStrategy(ps3d, aoi, startZ, endZ);
    switch (preferredOrder) {
      case INLINE_XLINE_OFFSET_Z:
        _readStrategy = new InlineXlineTraceIteratorStrategy(ps3d, aoi, startZ, endZ);
        break;
      case INLINE_OFFSET_XLINE_Z:
        _readStrategy = new InlineOffsetTraceIteratorStrategy(ps3d, aoi, startZ, endZ);
        break;
      case XLINE_INLINE_OFFSET_Z:
        _readStrategy = new XlineInlineTraceIteratorStrategy(ps3d, aoi, startZ, endZ);
        break;
      case XLINE_OFFSET_INLINE_Z:
        _readStrategy = new XlineOffsetTraceIteratorStrategy(ps3d, aoi, startZ, endZ);
        break;
      case OFFSET_INLINE_XLINE_Z:
        _readStrategy = new OffsetInlineTraceIteratorStrategy(ps3d, aoi, startZ, endZ);
        break;
      case OFFSET_XLINE_INLINE_Z:
        _readStrategy = new OffsetXlineTraceIteratorStrategy(ps3d, aoi, startZ, endZ);
        break;
      default:
        throw new IllegalArgumentException("Invalid storage order: " + preferredOrder);

    }

    // Set the missing trace flag.
    _omitMissingTraces = true;
  }

  /**
   * Constructs a trace iterator for a PostStack2d volumes.
   * It allows the user to specify the area-of-interest, the PostStack2d volume and the z range.
   * 
   * @param aoi the area-of-interest to process over.
   * @param ps3d the PostStack3d volume to iterate thru.
   * @param preferredOrder the processing order.
   * @param startZ the starting z.
   * @param endZ the ending z.
   */
  public TraceIterator(final AreaOfInterest aoi, final PostStack2dLine ps2d, final float startZ, final float endZ) {
    _readStrategy = new LineTraceIteratorStrategy(ps2d, aoi, startZ, endZ);
    // Set the missing trace flag.
    _omitMissingTraces = true;
  }

  private void init() {
    // Initialize the next traces list.
    _nextTraces = Collections.synchronizedList(new ArrayList<Trace>());
    updateNextTraces();
  }

  public boolean hasNext() {
    if (_nextTraces == null) {
      init();
    }
    return _nextTraces.size() > 0;
  }

  public TraceData next() {
    if (hasNext()) {
      // Extract the traces from the next array.
      Trace[] traces = _nextTraces.toArray(new Trace[0]);
      // Update the next array.
      updateNextTraces();
      // Return the trace data.
      return new TraceData(traces);
    }
    throw new NoSuchElementException("No more traces.");
  }

  public void remove() {
    throw new UnsupportedOperationException("Cannot remove traces.");
  }

  /**
   * Gets the iterator status message.
   * 
   * @return the iterator status message.
   */
  public String getMessage() {
    return _readStrategy.getMessage();
  }

  /**
   * Gets the iterator completion (in the range 0-100).
   * 
   * @return the iterator completion (in the range 0-100).
   */
  public float getCompletion() {
    return _readStrategy.getCompletion();
  }

  /**
   * Updates the trace data that the iterator will return. If
   * 
   * @return true if beyond the last inline or xline loop; false if not.
   */
  private boolean updateNextTraces() {
    boolean isDone = updateList();
    while (!isDone && _nextTraces.size() == 0) {
      isDone = updateList();
    }
    return isDone;
  }

  /**
   * Gets the flag indicating if "missing" traces are to be omitted. Returns
   * <i>true</i> if "missing" traces are to be omitted. Returns <i>false</i>
   * if "missing" traces are to be retained.
   * 
   * @return the flag indicating if "missing" traces are to be omitted.
   */
  public boolean omitMissingTraces() {
    return _omitMissingTraces;
  }

  /**
   * Sets the flag indicating if "missing" traces are to be omitted. Set
   * <i>true</i> to omit "missing" traces. Set <i>false</i> to retain
   * "missing" traces.
   * 
   * @param omitMissingTraces
   *            the flag indicating if "missing" traces are to be omitted.
   */
  public void omitMissingTraces(final boolean omitMissingTraces) {
    _omitMissingTraces = omitMissingTraces;
  }

  /**
   * Updates the trace data that the iterator will return. Entire inlines or
   * xlines are read, and then only those within the area-of-interest are
   * added to the iterator.
   * 
   * @return true if beyond the last inline or xline loop; false if not.
   */
  private boolean updateList() {
    // Clear the current list of traces.
    _nextTraces.clear();
    Trace[] traces = new Trace[0];
    // Read the next inline or xline of traces, depending on the read strategy.
    traces = _readStrategy.readNext();
    if (traces.length == 0) {
      return _readStrategy.isDone();
    }
    // Add the traces to the list.
    for (int i = 0; i < traces.length; i++) {
      if (!_omitMissingTraces || !traces[i].isMissing()) {
        _nextTraces.add(traces[i]);
      }
    }
    return _nextTraces.size() > 0;
  }

}
