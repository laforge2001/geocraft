/*
 * Copyright (C) ConocoPhillips 2006 - 2008 All Rights Reserved.
 */

package org.geocraft.core.model.datatypes;


import java.io.Serializable;
import java.util.Arrays;

import org.geocraft.core.common.math.MathUtil;


/**
 * This class defines a trace value object.
 * A trace is a 1-dimensional data array of samples with additional information
 * contained in an optional header.
 */
public class Trace implements Serializable {

  /** Enumeration for trace status. */
  public static enum Status {
    /** A "live" trace exists and has non-zero values. */
    Live,
    /** A "dead" trace exists but has all zero values. */
    Dead,
    /** A "missing" trace does not exists. */
    Missing,
    ToBeComputed
  }

  /** The z range (start, end and delta). */
  private final float _zStart;

  private final float _zEnd;

  private final float _zDelta;

  /** The trace z units. */
  private final Unit _unitOfZ;

  /** The trace data array. */
  private final float[] _data;

  /** The trace status (Live, Dead, etc). */
  private Status _status;

  /** The trace header. */
  private Header _header;

  /**
   * Constructs a trace with given start-z, delta-z, x, y and data array.
   * Upon construction, a check is made to see if the trace is 'live' or 'dead', and
   * its status will be set accordingly. Note: this constructor will never flag the
   * trace as 'missing'.
   * 
   * @param startZ the trace start time/depth.
   * @param deltaZ the trace sample rate.
   * @param zUnits trace trace z units.
   * @param x the trace x coordinate.
   * @param y the trace y coordinate.
   * @param data the trace sample data.
   */
  public Trace(final float startZ, final float deltaZ, final Unit zUnits, final double x, final double y, final float[] data) {
    _zStart = startZ;
    _zEnd = startZ + (data.length - 1) * deltaZ;
    _zDelta = deltaZ;
    _unitOfZ = zUnits;
    _data = data;
    _status = Status.ToBeComputed;
    _header = new Header(new HeaderDefinition(new HeaderEntry[] { TraceHeaderCatalog.X, TraceHeaderCatalog.Y }));
    _header.putDouble(TraceHeaderCatalog.X, x);
    _header.putDouble(TraceHeaderCatalog.Y, y);
  }

  /**
   * Constructs a trace with given start-z, delta-z, x, y and data array.
   * The trace will retain the specified status, regardless of the data values.
   * For example: if an array of non-zero values is passed in, but the traces
   * is flagged as 'missing', the trace will not set to 'live' but will instead
   * retain its 'missing' status.
   * 
   * @param startZ the trace start time/depth.
   * @param deltaZ the trace sample rate.
   * @param zUnits trace trace z units.
   * @param x the trace x coordinate.
   * @param y the trace y coordinate.
   * @param data the trace sample data.
   * @param status the trace status.
   */
  public Trace(final float startZ, final float deltaZ, final Unit zUnits, final double x, final double y, final float[] data, final Status status) {
    _zStart = startZ;
    _zEnd = startZ + (data.length - 1) * deltaZ;
    _zDelta = deltaZ;
    _unitOfZ = zUnits;
    _data = data;
    _status = status;
    _header = new Header(new HeaderDefinition(new HeaderEntry[] { TraceHeaderCatalog.X, TraceHeaderCatalog.Y }));
    _header.putDouble(TraceHeaderCatalog.X, x);
    _header.putDouble(TraceHeaderCatalog.Y, y);
  }

  /**
   * Constructs a trace with given start-z, delta-z, x, y and data array.
   * The trace will retain the specified status, regardless of the data values.
   * For example: if an array of non-zero values is passed in, but the traces
   * is flagged as 'missing', the trace will not set to 'live' but will instead
   * retain its 'missing' status.
   * 
   * @param startZ the trace start time/depth.
   * @param deltaZ the trace sample rate.
   * @param zUnits trace trace z units.
   * @param x the trace x coordinate.
   * @param y the trace y coordinate.
   * @param data the trace sample data.
   * @param status the trace status.
   * @param header the trace header.
   */
  public Trace(final float startZ, final float deltaZ, final Unit zUnits, final float[] data, final Status status, final Header header) {
    _zStart = startZ;
    _zEnd = startZ + (data.length - 1) * deltaZ;
    _zDelta = deltaZ;
    _unitOfZ = zUnits;
    _data = data;
    _status = status;
    _header = header;
  }

  /**
   * Constructs a trace that is a duplicate of the given trace.
   *
   * @param trace the trace to construct a duplicate of.
   */
  public Trace(final Trace trace) {

    // Copy the the z range.
    _zStart = trace.getZStart();
    _zEnd = trace.getZEnd();
    _zDelta = trace.getZDelta();

    _unitOfZ = trace.getUnitOfZ();

    // Copy the trace data array.
    _data = trace.getData();

    // Copy the trace status.
    _status = trace.getStatus();

    // Copy the trace header.
    _header = new Header(trace.getHeader());
  }

  /**
   * Constructs a trace that is a duplicate of the given trace,
   * except for the sample data. The status will be flagged as
   * either 'live' or 'dead', depending on the data values.
   *
   * @param trace the trace to construct a duplicate of.
   */
  public Trace(final Trace trace, final float[] data) {

    // Copy the the z range.
    _zStart = trace.getZStart();
    _zEnd = trace.getZEnd();
    _zDelta = trace.getZDelta();
    _unitOfZ = trace.getUnitOfZ();

    // Copy the trace data array.
    _data = Arrays.copyOf(data, data.length);

    // If the prototype trace status is 'Missing' then use it.
    // Otherwise set it to null so it will be computed on-demand as either 'Live' or 'Dead'.
    if (trace.getStatus().equals(Status.Missing)) {
      _status = Status.Missing;
    } else {
      _status = Status.ToBeComputed;
    }

    // Copy the trace header.
    _header = new Header(trace.getHeader());
  }

  /**
   * Returns the "dead" status of the given trace.
   * 
   * @return <i>true</i> if trace is "dead" (i.e. all zeros); otherwise <i>false</i>.
   */
  public static boolean isDead(final Trace trace) {
    return isDead(trace.getDataReference());
  }

  /**
   * Returns the "dead" status of the float array.
   * 
   * @return <i>true</i> if float array is "dead" (i.e. all zeros); otherwise false.
   */
  public static boolean isDead(final float[] data) {
    if (data == null) {
      throw new IllegalArgumentException("Trace data not allocated.");
    }
    for (float sample : data) {
      if (Float.compare(sample, 0f) != 0) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns the inline number contained in this trace's header.
   * 
   * @return the inline number.
   * @throws RuntimeException thrown if the header does not contain an inline number.
   */
  public float getInline() {
    synchronized (_header) {
      if (!_header.getHeaderDefinition().contains(TraceHeaderCatalog.INLINE_NO)) {
        throw new RuntimeException("The trace does not contain an inline # in its header.");
      }
      return _header.getInteger(TraceHeaderCatalog.INLINE_NO);
    }
  }

  /**
   * Returns the xline number contained in this trace's header.
   * 
   * @return the xline number.
   * @throws RuntimeException thrown if the header does not contain an xline number.
   */
  public float getXline() {
    synchronized (_header) {
      if (!_header.getHeaderDefinition().contains(TraceHeaderCatalog.XLINE_NO)) {
        throw new RuntimeException("The trace does not contain an xline # in its header.");
      }
      return _header.getInteger(TraceHeaderCatalog.XLINE_NO);
    }
  }

  /**
   * Returns the world x-coordinate contained in this trace's header.
   * 
   * @return the world x-coordinate number.
   */
  public double getX() {
    synchronized (_header) {
      return _header.getDouble(TraceHeaderCatalog.X);
    }
  }

  /**
   * Returns the world y-coordinate contained in this trace's header.
   * 
   * @return the world y-coordinate number.
   */
  public double getY() {
    synchronized (_header) {
      return _header.getDouble(TraceHeaderCatalog.Y);
    }
  }

  /**
   * Returns the number of samples in this trace's data array.
   * 
   * @return the number of samples.
   */
  public int getNumSamples() {
    synchronized (_data) {
      return _data.length;
    }
  }

  /**
   * Returns the z unit of measurement of this trace (e.g. milliseconds, feet, etc).
   * 
   * @return the z unit of measurement.
   */
  public Unit getUnitOfZ() {
    return _unitOfZ;
  }

  /**
   * Gets the starting z value (e.g. starting time/depth) of this trace.
   * 
   * @return the starting z value.
   */
  public float getZStart() {
    return _zStart;
  }

  /**
   * Gets the ending z value (e.g. ending time/depth) of this trace.
   * 
   * @return the ending z value.
   */
  public float getZEnd() {
    return _zEnd;
  }

  /**
   * Gets the delta z value (e.g. sample rate) of this trace.
   * 
   * @return the delta z value.
   */
  public float getZDelta() {
    return _zDelta;
  }

  /**
   * Returns a copy of the internal data array for this trace.
   * <p>
   * Changes to the copy will not be reflected in the trace.
   * 
   * @return a copy of the internal data array.
   */
  public float[] getData() {
    synchronized (_data) {
      return Arrays.copyOf(_data, _data.length);
    }
  }

  /**
   * Returns a reference to the internal data array for this trace.
   * <p>
   * <i>WARNING: CHANGES TO THE REFERENCE WILL BE REFLECTED IN THE TRACE, SO USE AT YOUR OWN RISK!</i>
   * 
   * @return a reference to the internal data array.
   */
  public final synchronized float[] getDataReference() {
    synchronized (_data) {
      return _data;
    }
  }

  /**
   * Gets the status of this trace.
   * <p>
   * Options include <i>Live</i>, <i>Dead</i> and <i>Missing</i>.
   * 
   * @return the trace status.
   */
  public final Status getStatus() {
    synchronized (_status) {
      // If the status is currently 'ToBeComputed', then set it to either 'Live' or 'Dead',
      // depending on the data samples. 'Missing' will never be set by this method, and
      // has to be explicity set via setStatus() or passed in thru a constructor.
      if (_status == Status.ToBeComputed) {
        _status = Status.Live;
        if (isDead(_data)) {
          _status = Status.Dead;
        }
      }
      return _status;
    }
  }

  /**
   * Sets the status of this trace.
   * <p>
   * Options include <i>Live</i>, <i>Dead</i> and <i>Missing</i>.
   * 
   * @param status the trace status.
   */
  public final void setStatus(final Status status) {
    synchronized (_status) {
      _status = status;
    }
  }

  /**
   * Returns <i>true</i> if the trace is flagged as <i>Live</i>, otherwise <i>false</i>.
   * <p>
   * A <i>Live</i> trace is a valid trace read from the input volume and contains at least
   * one non-zero values.
   * 
   * @return the <i>true</i> is the trace status is <i>Live</i>; otherwise <i>false</i>.
   */
  public boolean isLive() {
    synchronized (_status) {
      return getStatus().equals(Status.Live);
    }
  }

  /**
   * Returns <i>true</i> if the trace is flagged as <i>Dead</i>, otherwise <i>false</i>.
   * <p>
   * A <i>Dead</i> trace is a valid trace read from the input volume but contains all
   * zero values.
   * 
   * @return the <i>true</i> is the trace status is <i>Dead</i>; otherwise <i>false</i>.
   */
  public boolean isDead() {
    synchronized (_status) {
      return getStatus().equals(Status.Dead);
    }
  }

  /**
   * Returns <i>true</i> if the trace is flagged as <i>Missing</i>, otherwise <i>false</i>.
   * A <i>Missing</i> trace is one which did not exist in the input volume, and will not
   * be written to any output volume.
   * 
   * @return the <i>true</i> is the trace status is <i>Missing</i>; otherwise <i>false</i>.
   */
  public boolean isMissing() {
    synchronized (_status) {
      return getStatus().equals(Status.Missing);
    }
  }

  /**
   * Gets the header for this trace.
   * 
   * @return the trace header.
   */
  public Header getHeader() {
    synchronized (_header) {
      return _header;
    }
  }

  /**
   * Sets the header for this trace.
   * 
   * @param header the trace header.
   */
  public void setHeader(final Header header) {
    synchronized (_header) {
      _header = header;
    }
  }

  /**
   * Creates a new trace, resampled to half the sample rate of this trace.
   * 
   * @return the resampled trace.
   */
  public synchronized Trace resample() {
    float[] data = MathUtil.resample(getDataReference());
    Trace trace = new Trace(_zStart, _zDelta / 2, _unitOfZ, getX(), getY(), data, _status);
    trace.setHeader(new Header(getHeader()));
    return trace;
  }

  /**
   * Create a new trace, expanded trace to the given z range.
   * <i>
   * Values outside the original z range will be set the to <i>null</i> value.
   * 
   * @param zRange the new z range.
   * @param nullValue null value to use for expanded samples.
   * @return the expanded trace.
   */
  public synchronized Trace expandTrace(final FloatRange zRange, final float nullValue) {
    float[] data = new float[zRange.getNumSteps()];
    // Embed trace data in expanded trace.
    // Create a null trace.
    for (int i = 0; i < data.length; i++) {
      data[i] = nullValue;
    }
    // Determine where to place the data.
    int startIdx = Math.abs(Math.round((zRange.getStart() - _zStart) / zRange.getDelta()));
    System.arraycopy(_data, 0, data, startIdx, _data.length);

    Trace trace = new Trace(zRange.getStart(), zRange.getDelta(), _unitOfZ, getX(), getY(), data, _status);
    trace.setStatus(_status);
    trace.setHeader(new Header(getHeader()));
    return trace;
  }
}
