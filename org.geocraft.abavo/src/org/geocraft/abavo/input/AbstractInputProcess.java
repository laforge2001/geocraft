/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.input;


import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.abavo.defs.ABavoDataMode;
import org.geocraft.abavo.defs.ABavoTimeMode;
import org.geocraft.abavo.process.NearFarToInterceptGradient;
import org.geocraft.abavo.process.ScaleFloatArray;
import org.geocraft.abavo.process.TraceAlignment;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.io.util.MultiVolumeTraceIterator;
import org.geocraft.math.wavelet.WaveletFilter;


public abstract class AbstractInputProcess {

  public static final int PRE_PROCESSES_A_TRACE = 0;

  public static final int PRE_PROCESSES_B_TRACE = 1;

  /** Enumeration for the processing direction: inline, crossline, etc. */
  public enum ProcessDirection {
    Inline,
    Crossline
  }

  /** Enumeration for process status. */
  public static enum Status {
    Idle,
    Running,
    Completed
  }

  /** The project dimension: 2=2D, 3=3D */
  protected int _projectDimension = 0;

  /** The processing direction (default is inline). */
  protected ProcessDirection _direction = ProcessDirection.Inline;

  /** The flag for converting near/far data to intercept/gradient. */
  protected boolean _convertNearFarToInterceptGradient;

  /** The input data mode (all data or peaks & troughs). */
  protected ABavoDataMode _dataMode;

  /** The input time bounds (between times, relative to 1 or 2 horizons). */
  protected ABavoTimeMode _timeMode;

  /** The flag for applying a wavelet filter to the data from volume A. */
  protected boolean _aPrepWaveletFilterFlag;

  /** The flag for applying a wavelet filter to the data from volume B. */
  protected boolean _bPrepWaveletFilterFlag;

  /** The flag for performing an auto-alignment between volume A and volume B. */
  protected boolean _bPrepAutoAlign;

  /** The flag for indicating is the primary direction is inline. */
  protected boolean _isPrimInline;

  /** The relative z-start to the reference horizons (msec). */
  protected float _relativeStartZ;

  /** The relative z-end to the reference horizons (msec). */
  protected float _relativeEndZ;

  /** The starting z value. */
  protected float _startZ;

  /** The ending z value. */
  protected float _endZ;

  /** The delta z value. */
  protected float _deltaZ;

  /** The area-of-interest for data input. */
  protected AreaOfInterest _aoi;

  /** The seismic area-of-interest. */
  protected AreaOfInterest _seismicAOI;

  /** The starting z value for the A volume. */
  protected float _startZA;

  /** The starting z value for the B volume. */
  protected float _startZB;

  /** The ending z value for the A volume. */
  protected float _endZA;

  /** The ending z value for the B volume. */
  protected float _endZB;

  /** The trace alignment process (used for auto-alignment of near/far). */
  protected TraceAlignment _traceAlignment;

  /** The trace scalar process for volume A. */
  protected ScaleFloatArray _traceScaleA;

  /** The trace scalar process for volume B. */
  protected ScaleFloatArray _traceScaleB;

  /** The trace wavelet filter for volume A. */
  protected WaveletFilter _traceFilterA;

  /** The trace wavelet filter for volume B. */
  protected WaveletFilter _traceFilterB;

  /** The transform process (used for conversion of near/far data to intercept/gradient data). */
  protected NearFarToInterceptGradient _xformNearFar;

  /** The trace iterator for the input volumes (2D only). */
  protected MultiVolumeTraceIterator _traceIterator;

  protected int _resampleCount = 0;

  protected Status _status;

  protected boolean _printPrimCoord = true;

  /** A trace index. */
  protected int _traceDataIndex;

  /** A buffer array for traces from volume A. */
  protected Trace[] _traceBufferA;

  /** A buffer array for traces from volume B. */
  protected Trace[] _traceBufferB;

  protected IProgressMonitor _monitor;

  public AbstractInputProcess(final int projectDimension) {
    _projectDimension = projectDimension;
  }

  public String getName() {
    return "ABAVO " + _projectDimension + "D Input Process";
  }

  public int getProgress() {
    return Math.max(1, (int) _traceIterator.getCompletion());
  }

  public void setProgressMonitor(final IProgressMonitor monitor) {
    _monitor = monitor;
  }

  /**
   * Returns the completion status of the input process.
   * @return <i>true</i> if done, <i>false</i> if not.
   */
  public boolean isDone() {
    return _status.equals(Status.Completed);
  }

  /**
   * Aligns the near/far traces.
   * @param traceA the near trace.
   * @param traceB the far trace.
   * @param firstIndexA the first index in the near trace.
   * @param lastIndexA the last index in the near trace.
   * @param firstIndexB the first index in the far trace.
   * @param lastIndexB the last index in the far trace.
   * @param numSamples the number of samples.
   * @return the array of aligned traces.
   */
  protected Trace[] alignTraces(final Trace traceA, final Trace traceB, final int firstIndexA, final int lastIndexA,
      final int firstIndexB, final int lastIndexB, final int numSamples) {
    // If data type is Near/Far and alignment is on, align the traces.
    if (_bPrepAutoAlign && _convertNearFarToInterceptGradient) {
      return _traceAlignment.process(numSamples, traceA, firstIndexA, lastIndexA, traceB, firstIndexB, lastIndexB);
    }
    return new Trace[] { traceA, traceB };
  }

  /**
   * Scales the data arrays from volume A and volume B.
   * @param dataA the array of data from volume A.
   * @param dataB the array of data from volume B.
   */
  protected void scaleTraceData(final float[] dataA, final float[] dataB) {
    int numSamples = dataA.length;
    int firstIndex = 0;
    int lastIndex = numSamples - 1;
    if (_convertNearFarToInterceptGradient) {
      _xformNearFar.process(numSamples, dataA, firstIndex, dataB, firstIndex);
    }
    _traceScaleA.process(dataA, firstIndex, lastIndex);
    _traceScaleB.process(dataB, firstIndex, lastIndex);
  }
}
