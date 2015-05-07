/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.util;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.aoi.ZRangeConstant;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.datatypes.TraceHeaderCatalog;
import org.geocraft.core.model.datatypes.Trace.Status;
import org.geocraft.core.model.seismic.PostStack2dLine;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PostStack3d.StorageOrder;


/**
 * A simple trace iterator that returns blocks of trace data from one or more
 * volumes. The data returned is based upon the preferred direction of the
 * primary input volume, as well as the specified area-of-interest. If no
 * area-of-interest is specified, then it is based solely on the preferred
 * direction of the primary input volume. Because it is possible that the
 * geometry of the secondary volumes may not match that of the primary volume,
 * it is necessary to specify a strategy for how to generate the traces from the
 * secondary volumes. One option is to take the x,y coordinates from the primary
 * traces and extract the <i>nearest</i> traces from the secondary volumes.
 * Another option is to take the x,y coordinates from the primary traces and
 * generate <i>interpolated</i> traces from the secondary volumes.
 */
public class MultiVolumeTraceIterator implements Iterator {

  /**
   * Enumeration for the strategy of obtaining traces from the secondary
   * volumes.
   */
  public enum SecondaryTraceStrategy {
    /** Return the traces nearest the x,y coordinates of the primary traces. */
    NearestTrace,
    /**
     * Return trace interpolated from the traces nearest the x,y coordinates of
     * the primary traces.
     */
    InterpolatedTrace
  }

  /** The trace iterator for the primary volume. */
  private final TraceIterator _primIterator;

  /** The trace retrieval strategies for the secondary volumes. */
  private final List<ITraceReadStrategy> _secnStrategies;

  /**
   * Constructs a multi-volume trace iterator for stacked 3D seismic, using the given area-of-interest.
   * <p>
   * The z-range of the primary volume is used.
   * 
   * @param primVolume the primary PostStack3d volume to iterate thru.
   * @param secnVolumes the optional list of secondary PostStack3d volumes.
   */
  public MultiVolumeTraceIterator(final PostStack3d primVolume, final PostStack3d... secnVolumes) {
    this(null, primVolume, secnVolumes);
  }

  /**
   * Constructs a multi-volume trace iterator for stacked 3D seismic, using the given area-of-interest.
   * <p>
   * If the given AOI is non-null and contains a z-range, then its z-range is used. Otherwise the
   * z-range of the primary volume is used.
   * 
   * @param aoi the area-of-interest to process over.
   * @param primVolume the primary PostStack3d volume to iterate thru.
   * @param secnVolumes the optional list of secondary PostStack3d volumes.
   */
  public MultiVolumeTraceIterator(final AreaOfInterest aoi, final PostStack3d primVolume, final PostStack3d... secnVolumes) {
    // Default the z range to that of the primary volume.
    float zStart = primVolume.getZStart();
    float zEnd = primVolume.getZEnd();
    if (aoi != null && aoi.hasZRange()) {
      // But if the AOI is defined and has a given z range, then use it.
      ZRangeConstant zRange = aoi.getZRange();
      zStart = zRange.getZStart();
      zEnd = zRange.getZEnd();
    }

    // Create the trace iterator for the primary volume.
    _primIterator = TraceIteratorFactory.create(primVolume, aoi, primVolume.getPreferredOrder(), zStart, zEnd);

    // Create the trace retrieval strategies for the secondary volumes.
    _secnStrategies = Collections.synchronizedList(new ArrayList<ITraceReadStrategy>());
    for (PostStack3d secnVolume : secnVolumes) {
      _secnStrategies.add(new MatchingTraceStrategy3d(secnVolume, zStart, zEnd));
    }
  }

  /**
   * Constructs a multi-volume trace iterator for stacked 3D seismic, using the given area-of-interest.
   * <p>
   * The given z-range is always used, regardless of whether or not the the given AOI is non-null and contains a z-range.
   * 
   * @param aoi the area-of-interest to process over.
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   * @param primVolume the primary PostStack3d volume to iterate thru.
   * @param secnVolumes the optional list of secondary PostStack3d volumes.
   */
  public MultiVolumeTraceIterator(final AreaOfInterest aoi, final float zStart, final float zEnd, final PostStack3d primVolume, final PostStack3d... secnVolumes) {
    // Create the trace iterator for the primary volume.
    _primIterator = TraceIteratorFactory.create(primVolume, aoi, primVolume.getPreferredOrder(), zStart, zEnd);

    // Create the trace retrieval strategies for the secondary volumes.
    _secnStrategies = Collections.synchronizedList(new ArrayList<ITraceReadStrategy>());
    for (PostStack3d secnVolume : secnVolumes) {
      _secnStrategies.add(new MatchingTraceStrategy3d(secnVolume, zStart, zEnd));
    }
  }

  /**
   * Constructs a multi-volume trace iterator for stacked 2D seismic, using the given area-of-interest.
   * <p>
   * The z-range of the primary volume is used.
   * 
   * @param primVolume the primary PostStack2d volume to iterate thru.
   * @param secnVolumes the optional list of secondary PostStack2d volumes.
   */
  public MultiVolumeTraceIterator(final PostStack2dLine primVolume, final PostStack2dLine... secnVolumes) {
    this(null, primVolume, secnVolumes);
  }

  /**
   * Constructs a multi-volume trace iterator for stacked 2D seismic, using the given area-of-interest.
   * <p>
   * If the given AOI is non-null and contains a z-range, then its z-range is used. Otherwise the
   * z-range of the primary volume is used.
   * 
   * @param aoi the area-of-interest to process over.
   * @param primVolume the primary PostStack2d volume to iterate thru.
   * @param secnVolumes the optional list of secondary PostStack2d volumes.
   */
  public MultiVolumeTraceIterator(final AreaOfInterest aoi, final PostStack2dLine primVolume, final PostStack2dLine... secnVolumes) {
    // Default the z range to that of the primary volume.
    float zStart = primVolume.getZStart();
    float zEnd = primVolume.getZEnd();
    if (aoi != null && aoi.hasZRange()) {
      // But if the AOI is defined and has a given z range, then use it.
      ZRangeConstant zRange = aoi.getZRange();
      zStart = zRange.getZStart();
      zEnd = zRange.getZEnd();
    }

    // Create the trace iterator for the primary volume.
    _primIterator = TraceIteratorFactory.create(primVolume, aoi, StorageOrder.INLINE_XLINE_Z, zStart, zEnd);

    // Create the trace retrieval strategies for the secondary volumes.
    _secnStrategies = Collections.synchronizedList(new ArrayList<ITraceReadStrategy>());
    for (PostStack2dLine secnVolume : secnVolumes) {
      _secnStrategies.add(new MatchingTraceStrategy2d(secnVolume, zStart, zEnd));
    }
  }

  /**
   * Constructs a multi-volume trace iterator for stacked 2D seismic, using the given area-of-interest.
   * <p>
   * The given z-range is always used, regardless of whether or not the the given AOI is non-null and contains a z-range.
   * 
   * @param aoi the area-of-interest to process over.
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   * @param primVolume the primary PostStack2d volume to iterate thru.
   * @param secnVolumes the optional list of secondary PostStack2d volumes.
   */
  public MultiVolumeTraceIterator(final AreaOfInterest aoi, final float zStart, final float zEnd, final PostStack2dLine primVolume, final PostStack2dLine... secnVolumes) {
    // Create the trace iterator for the primary volume.
    _primIterator = TraceIteratorFactory.create(primVolume, aoi, StorageOrder.INLINE_XLINE_Z, zStart, zEnd);

    // Create the trace retrieval strategies for the secondary volumes.
    _secnStrategies = Collections.synchronizedList(new ArrayList<ITraceReadStrategy>());
    for (PostStack2dLine secnVolume : secnVolumes) {
      _secnStrategies.add(new MatchingTraceStrategy2d(secnVolume, zStart, zEnd));
    }
  }

  public boolean hasNext() {
    return _primIterator.hasNext();
  }

  public TraceData[] next() {
    // Get the trace data from the primary volume.
    TraceData primTraceData = _primIterator.next();

    // Allocate the trace data array to return.
    TraceData[] allTraceData = new TraceData[1 + _secnStrategies.size()];
    allTraceData[0] = primTraceData;

    // Get the trace data from the secondary volumes.
    Trace[] primTraces = primTraceData.getTraces();
    for (int i = 0; i < _secnStrategies.size(); i++) {
      ITraceReadStrategy secnStrategy = _secnStrategies.get(i);
      Trace[] traces = secnStrategy.read(primTraces);
      allTraceData[i + 1] = traces != null ? new TraceData(traces) : null;
    }

    // Return the trace data array.
    return allTraceData;
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
    return _primIterator.getMessage();
  }

  /**
   * Gets the iterator completion (in the range 0-100).
   * 
   * @return the iterator completion (in the range 0-100).
   */
  public float getCompletion() {
    return _primIterator.getCompletion();
  }

  /**
   * The strategy for reading traces from a volume based on the traces nearest
   * the specified x,y coordinates other traces.
   */
  private class MatchingTraceStrategy3d implements ITraceReadStrategy {

    /** The PostStack3d volume to read. */
    private final PostStack3d _ps3d;

    /** The starting z value. */
    private float _zStart = 0;

    /** The ending z value. */
    private float _zEnd = 0;

    /**
     * The default constructor.
     */
    public MatchingTraceStrategy3d(final PostStack3d ps3d, final float zStart, final float zEnd) {
      _ps3d = ps3d;
      _zStart = zStart;
      _zEnd = zEnd;
    }

    public Trace[] read(final Trace[] tracesIn) {
      Trace[] result = null;

      if (_ps3d != null) {
        int numTraces = tracesIn.length;
        float[] inlines = new float[numTraces];
        float[] xlines = new float[numTraces];
        for (int i = 0; i < numTraces; i++) {
          inlines[i] = tracesIn[i].getHeader().getInteger(TraceHeaderCatalog.INLINE_NO);
          xlines[i] = tracesIn[i].getHeader().getInteger(TraceHeaderCatalog.XLINE_NO);
        }
        TraceData traceData = _ps3d.getTraces(inlines, xlines, _zStart, _zEnd);
        result = traceData.getTraces();
      }
      // Return the traces.
      return result;
    }
  }

  /**
   * The strategy for reading traces from a volume based on the traces nearest
   * the specified x,y coordinates other traces.
   */
  private class MatchingTraceStrategy2d implements ITraceReadStrategy {

    /** The PostStack2d volume to read. */
    private final PostStack2dLine _ps2d;

    /** The starting z value. */
    private float _zStart = 0;

    /** The ending z value. */
    private float _zEnd = 0;

    /**
     * The default constructor.
     */
    public MatchingTraceStrategy2d(final PostStack2dLine ps2d, final float zStart, final float zEnd) {
      _ps2d = ps2d;
      _zStart = zStart;
      _zEnd = zEnd;
    }

    public Trace[] read(final Trace[] tracesIn) {
      int numTraces = tracesIn.length;
      Trace[] traces = new Trace[numTraces];
      float[] cdps = { 0 };
      for (int i = 0; i < numTraces; i++) {
        double x = tracesIn[i].getX();
        double y = tracesIn[i].getY();
        cdps[0] = tracesIn[i].getHeader().getInteger(TraceHeaderCatalog.CDP_NO);

        // Read the traces.
        try {
          TraceData traceData = _ps2d.getTraces(cdps, _zStart, _zEnd);
          traces[i] = traceData.getTrace(0);
        } catch (RuntimeException e) {
          float[] zeroData = new float[_ps2d.getNumSamplesPerTrace()];
          traces[i] = new Trace(_ps2d.getZStart(), _ps2d.getZDelta(), _ps2d.getZUnit(), x, y, zeroData, Status.Missing);
        }
      }

      // Return the traces.
      return traces;
    }
  }

  //  /**
  //   * The strategy for reading traces from a volume based on the traces nearest
  //   * the specified x,y coordinates other traces.
  //   */
  //  private class NearestTraceStrategy implements ITraceReadStrategy {
  //
  //    /** The PostStack3d volume to read. */
  //    private final PostStack3d _ps3d;
  //
  //    /** The geometry of the PostStack3d volume to read. */
  //    private final SeismicSurvey3d _geometry;

  //    /**
  //     * The default constructor.
  //     * 
  //     * @param ps3d the PostStack3d volume to read.
  //     */
  //    public NearestTraceStrategy(final PostStack3d ps3d) {
  //      _ps3d = ps3d;
  //      _geometry = ps3d.getSurvey();
  //    }
  //
  //    public Trace[] read(final Trace[] tracesIn) {
  //      double[] xs = new double[tracesIn.length];
  //      double[] ys = new double[tracesIn.length];
  //      for (int i = 0; i < tracesIn.length; i++) {
  //        xs[i] = tracesIn[i].getX();
  //        ys[i] = tracesIn[i].getY();
  //      }
  //      return read(xs, ys);
  //    }
  //
  //    public Trace[] read(final double[] xs, final double[] ys) {
  //      if (xs.length != ys.length) {
  //        throw new IllegalArgumentException("The length of the x,y arrays do not match.");
  //      }
  //      // Transform the x,y coordinates into inline,xline coordinates.
  //      float[] inline = { 0 };
  //      float[] xline = { 0 };
  //      int numTraces = xs.length;
  //      Trace[] traces = new Trace[numTraces];
  //      for (int i = 0; i < numTraces; i++) {
  //        // Convert the x,y coordinates to the nearest inline,xline coordinates.
  //        float[] ixln = _geometry.transformXYToInlineXline(xs[i], ys[i], true);
  //        inline[0] = Math.round(ixln[0]);
  //        xline[0] = Math.round(ixln[1]);
  //        // Read the traces.
  //        try {
  //          TraceData traceData = _ps3d.getTraces(inline, xline, _ps3d.getZStart(), _ps3d.getZEnd());
  //          traces[i] = traceData.getTrace(0);
  //        } catch (RuntimeException e) {
  //          float[] zeroData = new float[_ps3d.getNumSamplesPerTrace()];
  //          traces[i] = new Trace(_ps3d.getZStart(), _ps3d.getZDelta(), _ps3d.getZUnit(), xs[i], ys[i], zeroData,
  //              Status.Missing);
  //        }
  //      }
  //
  //      // Return the traces.
  //      return traces;
  //    }
  //  }
  //
  //  /**
  //   * The strategy for reading traces from a volume based on the interpolation
  //   * of the traces nearest the specified x,y coordinates other traces.
  //   */
  //  private class InterpolatedTraceStrategy implements ITraceReadStrategy {
  //
  //    /** The PostStack3d volume to read. */
  //    private final PostStack3d _ps3d;
  //
  //    /** The geometry of the PostStack3d volume to read. */
  //    private final SeismicSurvey3d _geometry;
  //
  //    /**
  //     * The default constructor.
  //     * 
  //     * @param ps3d the PostStack3d volume to read.
  //     */
  //    public InterpolatedTraceStrategy(final PostStack3d ps3d) {
  //      _ps3d = ps3d;
  //      _geometry = ps3d.getSurvey();
  //    }
  //
  //    public Trace[] read(final Trace[] tracesIn) {
  //      double[] xs = new double[tracesIn.length];
  //      double[] ys = new double[tracesIn.length];
  //      for (int i = 0; i < tracesIn.length; i++) {
  //        xs[i] = tracesIn[i].getX();
  //        ys[i] = tracesIn[i].getY();
  //      }
  //      return read(xs, ys);
  //    }
  //
  //    public Trace[] read(final double[] xs, final double[] ys) {
  //      if (xs.length != ys.length) {
  //        throw new IllegalArgumentException("The length of the x,y arrays do not match.");
  //      }
  //      // Transform the x,y coordinates into inline,xline coordinates.
  //      float[] inline = { 0 };
  //      float[] xline = { 0 };
  //      int numTraces = xs.length;
  //      Trace[] traces = new Trace[numTraces];
  //      for (int i = 0; i < numTraces; i++) {
  //        // Convert the x,y coordinates to the nearest inline,xline coordinates.
  //        float[] ixln = _geometry.transformXYToInlineXline(xs[i], ys[i], true);
  //        inline[0] = Math.round(ixln[0]);
  //        xline[0] = Math.round(ixln[1]);
  //        // Read the traces.
  //        try {
  //          TraceData traceData = _ps3d.getTraces(inline, xline, _ps3d.getZStart(), _ps3d.getZEnd());
  //          traces[i] = traceData.getTrace(0);
  //        } catch (RuntimeException e) {
  //          float[] zeroData = new float[_ps3d.getNumSamplesPerTrace()];
  //          traces[i] = new Trace(_ps3d.getZStart(), _ps3d.getZDelta(), _ps3d.getZUnit(), xs[i], ys[i], zeroData,
  //              Status.Missing);
  //        }
  //      }
  //
  //      // Return the traces.
  //      return traces;
  //    }
  //  }
}
