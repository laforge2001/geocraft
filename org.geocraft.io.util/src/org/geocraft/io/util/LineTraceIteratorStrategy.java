/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.util;


import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.aoi.SeismicSurvey2dAOI;
import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.seismic.PostStack2dLine;
import org.geocraft.core.model.seismic.SeismicLine2d;


/**
 * Defines the iterator strategy for reading traces from a <code>PostStack2d</code> volume.
 */
public class LineTraceIteratorStrategy extends AbstractTraceIteratorStrategy {

  /** The volume over which to iterate. */
  protected PostStack2dLine _poststack2d;

  /**
   * Constructs a PostStack2d iterator without an area-of-interest.
   * 
   * @param poststack2d the PostStack2d volume to read.
   */
  public LineTraceIteratorStrategy(final PostStack2dLine poststack2d, final float startZ, final float endZ) {
    this(poststack2d, null, startZ, endZ);
  }

  /**
   * Constructs a PostStack2d iterator with an area-of-interest.
   * 
   * @param poststack2d the PostStack2d volume to read.
   * @param aoi the area-of-interest.
   */
  public LineTraceIteratorStrategy(final PostStack2dLine poststack2d, final AreaOfInterest aoi, final float startZ, final float endZ) {
    super(poststack2d, aoi, startZ, endZ);
    _poststack2d = poststack2d;
  }

  public Trace[] readNextOld() {
    //System.out.println("read next " + _nextLoop + " " + _numInlines);
    int numLoops = getNumLoops();
    if (_nextLoop >= numLoops) {
      _isDone = true;
      _message = "Done reading line...";
      return new Trace[0];
    }
    float inline = _inlineStart;
    _message = "Reading line " + inline + ".";

    // Before reading, create a coordinate series with the x,y coordinates
    // of all the potential traces to be read.
    float[] cdpsTemp = new float[1];
    float[] spsTemp = new float[1];
    SeismicLine2d seismicLine = _poststack2d.getSeismicLine();
    cdpsTemp[0] = _xlineStart + _nextLoop * _poststack2d.getCdpDelta();
    spsTemp[0] = seismicLine.transformCDPToShotpoint(cdpsTemp[0]);
    CoordinateSeries coords = seismicLine.transformCDPsToXYs(cdpsTemp);

    // Keep only those points inside the AOI.
    int counter = 0;
    for (int i = 0; i < coords.getNumPoints(); i++) {
      Point3d point = coords.getPoint(i);
      if (_aoi != null && _aoi instanceof SeismicSurvey2dAOI) {
        SeismicSurvey2dAOI aoi2d = (SeismicSurvey2dAOI) _aoi;
        if (aoi2d.contains(seismicLine.getDisplayName(), spsTemp[i], _poststack2d.getSurvey())) {
          cdpsTemp[counter] = cdpsTemp[i];
          counter++;
        }
      } else if (_aoi == null || _aoi.contains(point.getX(), point.getY(), _poststack2d.getSurvey())) {
        cdpsTemp[counter] = cdpsTemp[i];
        counter++;
      }
    }

    // Increment the loop index.
    _nextLoop++;

    if (counter == 0) {
      return new Trace[0];
    }

    float[] cdps = new float[counter];
    System.arraycopy(cdpsTemp, 0, cdps, 0, counter);

    // Read the traces.
    TraceData traceData = _poststack2d.getTraces(cdps, _startZ, _endZ);

    // Return the traces.
    return traceData.getTraces();
  }

  public Trace[] readNext() {
    //System.out.println("read next " + _nextLoop + " " + _numInlines);
    int numLoops = getNumLoops();
    if (_nextLoop >= numLoops) {
      _isDone = true;
      _message = "Done reading line...";
      return new Trace[0];
    }
    float inline = _inlineStart;
    _message = "Reading line " + inline + ".";

    // Before reading, create a coordinate series with the x,y coordinates
    // of all the potential traces to be read.
    SeismicLine2d seismicLine = _poststack2d.getSeismicLine();
    float[] cdpsTemp = new float[_numXlines];
    float[] spsTemp = new float[_numXlines];
    for (int i = 0; i < _numXlines; i++) {
      cdpsTemp[i] = _xlineStart + i * _poststack2d.getCdpDelta();
      spsTemp[i] = seismicLine.transformCDPToShotpoint(cdpsTemp[i]);
    }
    CoordinateSeries coords = seismicLine.transformCDPsToXYs(cdpsTemp);

    // Keep only those points inside the AOI.
    int counter = 0;
    for (int i = 0; i < coords.getNumPoints(); i++) {
      Point3d point = coords.getPoint(i);
      if (_aoi != null && _aoi instanceof SeismicSurvey2dAOI) {
        SeismicSurvey2dAOI aoi2d = (SeismicSurvey2dAOI) _aoi;
        if (aoi2d.contains(seismicLine.getDisplayName(), spsTemp[i], _poststack2d.getSurvey())) {
          cdpsTemp[counter] = cdpsTemp[i];
          counter++;
        }
      } else if (_aoi == null || _aoi.contains(point.getX(), point.getY())) {
        cdpsTemp[counter] = cdpsTemp[i];
        counter++;
      }
    }

    // Increment the loop index.
    _nextLoop++;

    if (counter == 0) {
      return new Trace[0];
    }

    float[] cdps = new float[counter];
    System.arraycopy(cdpsTemp, 0, cdps, 0, counter);

    // Read the traces.
    TraceData traceData = _poststack2d.getTraces(cdps, _startZ, _endZ);

    // Return the traces.
    return traceData.getTraces();
  }

  @Override
  protected int getNumLoops() {
    //return _numXlines;
    return 1;
  }

}
