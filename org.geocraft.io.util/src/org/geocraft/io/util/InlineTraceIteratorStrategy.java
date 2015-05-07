/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */

package org.geocraft.io.util;


import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.seismic.PostStack3d;


/**
 * Defines the iterator strategy for reading traces from a <code>PostStack3d</code>
 * volume along the inline direction.
 */
public final class InlineTraceIteratorStrategy extends AbstractTraceIteratorStrategy {

  /** The volume over which to iterate. */
  protected final PostStack3d _poststack3d;

  /**
   * Constructs a PostStack3d iterator for reading in the inline direction, without an area-of-interest.
   * 
   * @param poststack3d the PostStack3d volume to read.
   */
  public InlineTraceIteratorStrategy(final PostStack3d poststack3d, final float startZ, final float endZ) {
    this(poststack3d, null, startZ, endZ);
  }

  /**
   * Constructs a PostStack3d iterator for reading in the inline direction, with an area-of-interest.
   * 
   * @param poststack3d the PostStack3d volume to read.
   * @param aoi the area-of-interest.
   */
  public InlineTraceIteratorStrategy(final PostStack3d poststack3d, final AreaOfInterest aoi, final float startZ, final float endZ) {
    super(poststack3d, aoi, startZ, endZ);
    _poststack3d = poststack3d;
  }

  public Trace[] readNext() {
    int numLoops = getNumLoops();
    if (_nextLoop >= numLoops) {
      _isDone = true;
      _message = "Done reading inlines...";
      return new Trace[0];
    }
    float inline = _inlineStart + _nextLoop * _poststack3d.getInlineDelta();
    _message = "Reading inline " + inline + ".";

    // Before reading, create a coordinate series with the x,y coordinates
    // of all the potential traces to be read.
    float[] inlinesTemp = new float[_numXlines];
    float[] xlinesTemp = new float[_numXlines];
    for (int i = 0; i < _numXlines; i++) {
      inlinesTemp[i] = inline;
      xlinesTemp[i] = _xlineStart + i * _poststack3d.getXlineDelta();
    }
    CoordinateSeries coords = _poststack3d.getSurvey().transformInlineXlineToXY(inlinesTemp, xlinesTemp);

    // Keep only those points inside the AOI.
    int counter = 0;
    for (int i = 0; i < coords.getNumPoints(); i++) {
      Point3d point = coords.getPoint(i);
      if (_aoi == null || _aoi.contains(point.getX(), point.getY())) {
        inlinesTemp[counter] = inlinesTemp[i];
        xlinesTemp[counter] = xlinesTemp[i];
        counter++;
      }
    }

    // Increment the loop index.
    _nextLoop++;

    if (counter == 0) {
      return new Trace[0];
    }

    float[] inlines = new float[counter];
    float[] xlines = new float[counter];
    System.arraycopy(inlinesTemp, 0, inlines, 0, counter);
    System.arraycopy(xlinesTemp, 0, xlines, 0, counter);

    // Read the traces.
    TraceData traceData = _poststack3d.getTraces(inlines, xlines, _startZ, _endZ);

    // Return the traces.
    return traceData.getTraces();
  }

  @Override
  protected int getNumLoops() {
    return _numInlines;
  }

}
