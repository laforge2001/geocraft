/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.core.model.datatypes;


import java.io.Serializable;
import java.util.Arrays;


/**
 * A collection of seismic traces, where each trace contains the same number of samples.
 */
public final class TraceData implements Serializable {

  /** The 1-d array of data for all the traces. */
  private float[] _data;

  /** The array of traces. */
  private final Trace[] _traces;

  /** The number of samples per traces in the collection. */
  private final int _numSamples;

  /** The number of traces in the collection. */
  private final int _numTraces;

  /** The start z value for the traces. */
  private final float _zStart;

  /** The delta z value for the traces. */
  private final float _zDelta;

  /** The z unit for the traces. */
  private final Unit _zUnit;

  /**
   * This is the most 'object oriented' of the constructors.
   * @param traces the array of traces.
   */
  public TraceData(final Trace[] traces) {
    this(traces, true);
  }

  /**
   * This is the most 'object oriented' of the constructors.
   * @param traces the array of traces.
   */
  public TraceData(final Trace[] traces, final boolean validate) {
    // Validate that at least 1 trace is passed in.
    if (traces == null || traces.length == 0) {
      throw new IllegalArgumentException("No traces specified.");
    }

    // Store the traces.
    _traces = Arrays.copyOf(traces, traces.length);
    _numTraces = traces.length;
    _numSamples = traces[0].getNumSamples();
    _zStart = traces[0].getZStart();
    _zDelta = traces[0].getZDelta();
    _zUnit = traces[0].getUnitOfZ();

    // Perform optional validation to ensure that each of the traces contains
    // the sample # of samples, start and end z values, and z unit of measurement.
    if (validate) {
      for (int i = 0; i < _numTraces; i++) {
        if (_numSamples != traces[i].getNumSamples()) {
          throw new IllegalArgumentException("The Number of samples must be the same for each trace. Expected "
              + _numSamples + " but found " + traces[i].getNumSamples() + " samples");
        }
        if (_zStart != traces[i].getZStart()) {
          throw new IllegalArgumentException("The start Z value must be the same for each trace.");
        }
        if (_zDelta != traces[i].getZDelta()) {
          throw new IllegalArgumentException("The delta Z value must be the same for each trace.");
        }
        if (_zUnit != traces[i].getUnitOfZ()) {
          throw new IllegalArgumentException("The unit of z must be the same for each trace");
        }
      }
    }
  }

  /**
   * Returns the number of traces in this trace collection.
   * 
   * @return the number of samples.
   */
  public int getNumTraces() {
    return _numTraces;
  }

  /**
   * Returns the number of samples per trace in this trace collection.
   * 
   * @return the number of samples.
   */
  public int getNumSamples() {
    return _numSamples;
  }

  /**
   * Returns the specified trace in this trace collection.
   * 
   * @param index the index of the trace to get.
   * @return the specified trace.
   * @throws ArrayIndexOutOfBoundsException thrown if the index if out of bounds.
   */
  public Trace getTrace(final int index) {
    return _traces[index];
  }

  /**
   * Returns an array of traces in this trace collection.
   * <p>
   * A defensive copy of the internal trace array is allocated.
   * 
   * @return an array of the traces.
   */
  public Trace[] getTraces() {
    Trace[] traces = new Trace[_numTraces];
    for (int i = 0; i < _numTraces; i++) {
      traces[i] = getTrace(i);
    }
    return traces;
  }

  /**
   * Returns the starting z value of the traces in this collection.
   * 
   * @return the starting z value of the traces.
   */
  public float getStartZ() {
    return _zStart;
  }

  /**
   * Returns the ending z value of the traces of this collection.
   * 
   * @return the ending z value of the traces.
   */
  public float getEndZ() {
    return _zStart + (_numSamples - 1) * _zDelta;
  }

  /**
   * Returns the z unit of measurement of the traces in this collection.
   * 
   * @return the z unit of measurement of the traces.
   */
  public Unit getUnitOfZ() {
    return _zUnit;
  }

  /**
   * Returns the data of all the traces in this collection as a 1-dimensional array.
   * <p>
   * In the returned array, the sample index varying fastest.
   *
   * @return the data of all the traces as a 1-D array.
   */
  public float[] getData() {
    // If the 1-D data array has not yet been allocated, then allocate it.
    if (_data == null || _data.length != _numTraces * _numSamples) {
      _data = new float[_numTraces * _numSamples];
      // Copy the data from each of the traces into the 1-D array.
      for (int i = 0; i < _traces.length; i++) {
        System.arraycopy(_traces[i].getDataReference(), 0, _data, i * _numSamples, _numSamples);
      }
    }

    // Return the 1-D data array.
    return _data;
  }
}
