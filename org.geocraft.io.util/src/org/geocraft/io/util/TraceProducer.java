/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.util;


import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PostStack3d.StorageOrder;


/**
 * Defines a trace producer that will run in its own thread.
 * <p>
 * It is backed by a blocking queue to manage the synchronization
 * between the consumers.
 */
public final class TraceProducer implements Runnable {

  /** The internal trace iterator. */
  private final TraceIterator _iterator;

  /** Flag indicating if the iterator is done. */
  private boolean _iteratorIsDone;

  /** The queue used to store traces from the iterator. */
  private final BlockingQueue<Trace[]> _queue;

  /**
   * Constructs trace producer and starts it in a new thread.
   * <p>
   * The direction of reading will be the preferred order of the volume.
   * The z range (start,end) will be the full size of the volume.
   * 
   * @param volume the 3D volume.
   * @param aoi the area-of-interest (can be null).
   * @param omitMissingTraces <i>true</i> to omit missing trace; otherwise <i>false</i>.
   * @param capacity the capacity of the blocking queue.
   */
  public TraceProducer(final PostStack3d volume, final AreaOfInterest aoi, final boolean omitMissingTraces, final int capacity) {
    this(volume, aoi, volume.getPreferredOrder(), volume.getZStart(), volume.getZEnd(), omitMissingTraces, capacity);
  }

  /**
   * Constructs trace producer and starts it in a new thread.
   * <p>
   * The z range (start,end) will be the full size of the volume.
   * 
   * @param volume the 3D volume.
   * @param aoi the area-of-interest (can be null).
   * @param order the direction of reading.
   * @param omitMissingTraces <i>true</i> to omit missing trace; otherwise <i>false</i>.
   * @param capacity the capacity of the blocking queue.
   */
  public TraceProducer(final PostStack3d volume, final AreaOfInterest aoi, final StorageOrder order, final boolean omitMissingTraces, final int capacity) {
    this(volume, aoi, order, volume.getZStart(), volume.getZEnd(), omitMissingTraces, capacity);
  }

  /**
   * Constructs trace producer and starts it in a new thread.
   * <p>
   * The direction of reading will be the preferred order of the volume.
   * 
   * @param volume the 3D volume.
   * @param aoi the area-of-interest (can be null).
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   * @param omitMissingTraces <i>true</i> to omit missing trace; otherwise <i>false</i>.
   * @param capacity the capacity of the blocking queue.
   */
  public TraceProducer(final PostStack3d volume, final AreaOfInterest aoi, final float zStart, final float zEnd, final boolean omitMissingTraces, final int capacity) {
    this(volume, aoi, volume.getPreferredOrder(), zStart, zEnd, omitMissingTraces, capacity);
  }

  /**
   * Constructs trace producer and starts it in a new thread.
   * 
   * @param volume the 3D volume.
   * @param aoi the area-of-interest (can be null).
   * @param order the direction of reading.
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   * @param omitMissingTraces <i>true</i> to omit missing trace; otherwise <i>false</i>.
   * @param capacity the capacity of the blocking queue.
   */
  public TraceProducer(final PostStack3d volume, final AreaOfInterest aoi, final StorageOrder order, final float zStart, final float zEnd, final boolean omitMissingTraces, final int capacity) {
    // Initialize a block queue to buffer the trace data.
    _queue = new ArrayBlockingQueue<Trace[]>(Math.max(1, capacity));

    // Create an internal trace iterator.
    _iterator = new TraceIterator(aoi, volume, order, zStart, zEnd);
    _iterator.omitMissingTraces(omitMissingTraces);
    _iteratorIsDone = false;

    // Start the producer in a new thread.
    Thread thread = new Thread(this);
    thread.start();
  }

  public void run() {
    // Loop thru the iterator until done.
    while (_iterator.hasNext()) {
      TraceData traceData = _iterator.next();
      Trace[] traces = traceData.getTraces();
      if (traces != null && traces.length > 0) {
        try {
          // Store the iterator traces into the queue, which
          // blocks until space is available.
          _queue.put(traceData.getTraces());
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
          break;
        }
      }
    }
    // Set the flag indicating the iterator is done.
    _iteratorIsDone = true;
  }

  /**
   * Returns a flag indicating if the trace producer is completely done.
   * <p>
   * This means that the underlying iterator is done and all the available
   * traces have been taken from the blocking queue.
   * 
   * @return <i>true</i> if completely done; <i>false</i> if not.
   */
  public synchronized boolean isDone() {
    // The producer is done if the internal iterator is done
    // and the queue is empty.
    return _iteratorIsDone && _queue.size() == 0;
  }

  /**
   * Gets the next array of traces from the producer.
   * 
   * @return the next array of traces.
   */
  public synchronized Trace[] get() {
    // Get traces from the queue, blocking if necessary
    // until there are traces available.
    try {
      return _queue.take();
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
    return null;
  }

  /**
   * Gets the current message from the underlying trace iterator.
   * 
   * @return the current message.
   */
  public String getMessage() {
    return _iterator.getMessage();
  }
}
