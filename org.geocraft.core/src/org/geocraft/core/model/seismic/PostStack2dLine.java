/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.core.model.seismic;


import java.util.ArrayList;
import java.util.List;

import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.LoadStatus;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.Trace.Status;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.datatypes.TraceHeaderCatalog;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.IPostStack2dMapper;


/**
 * This class defines a 2D seismic dataset on a single 2D seismic line
 * in a 2D seismic survey.
 */
public class PostStack2dLine extends SeismicDataset {

  /** The 2D seismic survey on which the dataset is defined. */
  private final SeismicSurvey2d _seismicSurvey;

  /** The 2D seismic line on which the dataset is defined. */
  private final SeismicLine2d _seismicLine;

  /** The name of the 2D seismic line on which the dataset is defined. */
  private final String _lineName;

  /** The number of the 2D seismic line on which the dataset is defined. */
  private final int _lineNumber;

  /** The CDP range (start,end,delta) of the dataset. */
  private FloatRange _cdpRange;

  /** The shotpoint start value of the dataset. */
  private float _shotpointStart;

  /** The shotpoint end value of the dataset. */
  private float _shotpointEnd;

  /** The <code>PostStack2d</code> volume to which the dataset belongs. */
  private final PostStack2d _poststack;

  /**
   * Constructs a lazy-loaded <code>PostStack2dLine</code> entity.
   * 
   * @param name the name of the seismic dataset.
   * @param mapper the underlying mapper to use for datastore access.
   * @param seismicSurvey the 2D seismic survey on which the dataset is defined.
   * @param poststack the poststack volume to which the dataset belongs.
   */
  public PostStack2dLine(final String name, final IMapper mapper, final SeismicSurvey2d seismicSurvey, final String lineName, final int lineNumber, final PostStack2d poststack) {
    super(name, mapper);
    // Validate the seismic survey, line name and line number are specified.
    if (seismicSurvey == null) {
      throw new IllegalArgumentException("No seismic survey specified.");
    }
    if (lineName == null || lineName.isEmpty()) {
      throw new IllegalArgumentException("No line name specified.");
    }
    if (lineNumber < 1) {
      throw new IllegalArgumentException("Invalid line number: " + lineNumber);
    }
    if (poststack == null) {
      throw new IllegalArgumentException("No parent poststack specified.");
    }
    _seismicSurvey = seismicSurvey;
    _seismicLine = _seismicSurvey.getLineByNumber(lineNumber);
    _lineName = lineName;
    _lineNumber = lineNumber;
    _poststack = poststack;
  }

  /**
   * Gets the number of the 2D seismic line on which the dataset is defined.
   * 
   * @return the 2D seismic line number.
   */
  public int getLineNumber() {
    return _lineNumber;
  }

  /**
   * Gets the name of the 2D seismic line on which the dataset is defined.
   * 
   * @return the 2D seismic line name.
   */
  public String getLineName() {
    return _lineName;
  }

  /**
   * Returns the 2D poststack volume to which the dataset belongs.
   * 
   * @return the 2D poststack volume.
   */
  public PostStack2d getPostStack() {
    // This does not trigger a load, since the poststack volume is
    // specified in the constructor and cannot be changed.
    return _poststack;
  }

  /**
   * Gets the 2D seismic survey on which the dataset is defined.
   * 
   * @return the 2D seismic survey.
   */
  public SeismicSurvey2d getSurvey() {
    // This does not trigger a load, since the seismic survey is
    // specified in the constructor and cannot be changed.
    return _seismicSurvey;
  }

  /**
   * Returns the 2D seismic line geometry on which the dataset is defined.
   * 
   * @return the 2D seismic line.
   */
  public SeismicLine2d getSeismicLine() {
    // This does not trigger a load, since the seismic line is
    // specified in the constructor and cannot be changed.
    return _seismicLine;
  }

  /**
   * Returns the number of CDPs (i.e. # of traces) in the dataset.
   * 
   * @return the number of CDPs.
   */
  public int getNumCdps() {
    load();
    return _cdpRange.getNumSteps();
  }

  /**
   * Returns the CDP range (start,end,delta) of the dataset.
   * 
   * @return the CDP range.
   */
  public FloatRange getCdpRange() {
    load();
    return _cdpRange;
  }

  /**
   * Returns the starting CDP of the dataset.
   * 
   * @return the starting CDP.
   */
  public float getCdpStart() {
    load();
    return _cdpRange.getStart();
  }

  /**
   * Returns the ending CDP of the dataset.
   * 
   * @return the ending CDP.
   */
  public float getCdpEnd() {
    load();
    return _cdpRange.getEnd();
  }

  /**
   * Returns the CDP delta of the dataset.
   * 
   * @return the CDP delta.
   */
  public float getCdpDelta() {
    load();
    return _cdpRange.getDelta();
  }

  /**
   * Sets the CDP range (start,end,delta) of the dataset.
   * <p>
   * This also updates the shotpoint range, based on the associated seismic line.
   * 
   * @param cdpStart the starting CDP to set.
   * @param cdpEnd the ending CDP to set.
   * @param cdpDelta the delta CDP to set.
   */
  public void setCdpRange(final float cdpStart, final float cdpEnd, final float cdpDelta) {
    _cdpRange = new FloatRange(cdpStart, cdpEnd, cdpDelta);
    _shotpointStart = _seismicLine.transformCDPToShotpoint(cdpStart);
    _shotpointEnd = _seismicLine.transformCDPToShotpoint(cdpEnd);
    setDirty(true);
  }

  /**
   * Returns the starting shotpoint of the dataset.
   * 
   * @return the starting shotpoint.
   */
  public float getShotpointStart() {
    load();
    return _shotpointStart;
  }

  /**
   * Returns the ending shotpoint of the dataset.
   * 
   * @return the ending shotpoint.
   */
  public float getShotpointEnd() {
    load();
    return _shotpointEnd;
  }

  /**
   * Returns the format of the seismic dataset in the datastore.
   * 
   * @return the datastore storage format.
   */
  @Override
  public StorageFormat getStorageFormat() {
    load();
    return ((IPostStack2dMapper) _mapper).getStorageFormat();
  }

  /**
   * Gets the specified traces from the seismic dataset.
   * <p>
   * The traces will be read from the underlying datastore.
   * Any requested traces that fall outside the bounds of the dataset will be returned and flagged as missing.
   * 
   * @param cdps the array of CDP coordinates.
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   * @return the array of traces.
   */
  public TraceData getTraces(final float[] cdps, final float zStart, final float zEnd) {
    load();

    // Validate the requested CDPs.
    validateCdpArrays(cdps, true);

    // Validate the requested z range.
    validateZ(zStart);
    validateZ(zEnd);

    // Identify which CDPs are within the bounds of the seismic dataset.
    int leadingCdpIndex = -1;
    int trailingCdpIndex = -1;
    List<Trace> traces = new ArrayList<Trace>();
    for (int i = 0; i < cdps.length; i++) {
      float cdp = cdps[i];
      boolean cdpOk = cdp >= _cdpRange.getStart() && cdp <= _cdpRange.getEnd();
      if (cdpOk) {
        if (leadingCdpIndex == -1) {
          leadingCdpIndex = i;
        }
        trailingCdpIndex = i;
      }
    }
    float[] newCdps = new float[trailingCdpIndex - leadingCdpIndex + 1];
    for (int i = leadingCdpIndex; i <= trailingCdpIndex; i++) {
      newCdps[i - leadingCdpIndex] = cdps[i];
    }

    // Read the traces (only those within the bounds) from the datastore via the mapper.
    TraceData traceData = ((IPostStack2dMapper) _mapper).getTraces(this, newCdps, zStart, zEnd);
    Trace[] tracesRead = traceData.getTraces();

    // Add the leading and trailing traces and flag them as missing.
    for (int i = 0; i < leadingCdpIndex; i++) {
      Trace trace = new Trace(tracesRead[0], new float[tracesRead[0].getNumSamples()]);
      trace.setStatus(Status.Missing);
      traces.add(trace);
    }
    for (Trace element : tracesRead) {
      traces.add(element);
    }
    for (int i = trailingCdpIndex; i < cdps.length; i++) {
      Trace trace = new Trace(tracesRead[0], new float[tracesRead[0].getNumSamples()]);
      trace.setStatus(Status.Missing);
      traces.add(trace);
    }

    return new TraceData(traces.toArray(new Trace[0]));
  }

  /**
   * Puts the specified traces to the seismic dataset.
   * <p>
   * The traces will be written to the underlying datastore.
   * Any traces that fall outside the bounds of the dataset or flagged as missing will be ignored.
   * 
   * @param traceData the trace data object containing the traces to write.
   */
  public void putTraces(final TraceData traceData) {
    int numTraces = traceData.getNumTraces();

    // Validate the CDPs.
    float[] cdps = new float[numTraces];
    for (int i = 0; i < numTraces; i++) {
      Trace trace = traceData.getTrace(i);
      cdps[i] = trace.getHeader().getInteger(TraceHeaderCatalog.CDP_NO);
    }
    validateCdpArrays(cdps, false);

    // Validate the z range.
    validateZ(traceData.getStartZ());
    validateZ(traceData.getEndZ());

    // Write the traces to the datastore via the mapper.
    ((IPostStack2dMapper) _mapper).putTraces(this, traceData);
  }

  @Override
  public void close() {
    if (_mapper != null) {
      // Close the entry in the underlying datastore.
      ((IPostStack2dMapper) _mapper).close();
    }
    setDirty(false);

    // Reset the status to ghost so the dataset will be "reloaded" upon request.
    _status = LoadStatus.GHOST;
  }

  /**
   * Validates the specified CDP coordinates.
   * The value is checked to make sure it is a multiple of the CDP delta.
   * If <i>checkBounds</i> is <i>true<i>, it is also be checked to make sure
   * it falls within the start and end CDP bounds.
   * 
   * @param cdps the CDPs to validate.
   * @param checkBounds <i>true<i> to check against the start and end bounds; otherwise <i>false<i>.
   * @throws IllegalArgumentException if the CDP array is null or contains an invalid value.
   */
  private void validateCdpArrays(final float[] cdps, final boolean checkBounds) {
    if (cdps == null) {
      throw new IllegalArgumentException("Invalid CDP array: null.");
    }
    for (float cdp : cdps) {
      validateCdp(cdp, checkBounds);
    }
  }

  /**
   * Validates the specified CDP coordinate.
   * The value is checked to make sure it is a multiple of the CDP delta.
   * If <i>checkBounds</i> is <i>true<i>, it is also be checked to make sure
   * it falls within the start and end CDP bounds.
   * 
   * @param cdp the cdp to validate.
   * @param checkBounds <i>true<i> to check against the start and end bounds; otherwise <i>false<i>.
   * @throws IllegalArgumentException if the CDP represents an invalid value.
   */
  private void validateCdp(final float cdp, final boolean checkBounds) {
    validateRange("CDP", cdp, _cdpRange.getStart(), _cdpRange.getEnd(), _cdpRange.getDelta(), checkBounds);
  }
}
