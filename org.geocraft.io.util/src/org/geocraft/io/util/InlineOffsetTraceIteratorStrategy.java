/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.util;


import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.seismic.PreStack3d;


/**
 * Defines the iterator strategy for reading traces from a <code>PreStack3d</code>
 * volume along the inline primary direction and offset secondary direction.
 */
public class InlineOffsetTraceIteratorStrategy extends AbstractTraceIteratorStrategy {

  /** The volume over which to iterate. */
  protected PreStack3d _prestack3d;

  /**
   * Constructs a PreStack3d iterator for reading in the inline/offset direction, without an area-of-interest.
   * 
   * @param prestack3d the PreStack3d volume to read.
   */
  public InlineOffsetTraceIteratorStrategy(final PreStack3d prestack3d, final float startZ, final float endZ) {
    this(prestack3d, null, startZ, endZ);
  }

  /**
   * Constructs a PreStack3d iterator for reading in the inline/offset direction, with an area-of-interest.
   * 
   * @param prestack3d the PreStack3d volume to read.
   * @param aoi the area-of-interest.
   */
  public InlineOffsetTraceIteratorStrategy(final PreStack3d prestack3d, final AreaOfInterest aoi, final float startZ, final float endZ) {
    super(prestack3d, aoi, startZ, endZ);
    _prestack3d = prestack3d;
  }

  public Trace[] readNext() {
    int numLoops = getNumLoops();
    if (_nextLoop >= numLoops) {
      _isDone = true;
      _message = "Done reading inlines,offsets...";
      return new Trace[0];
    }
    int inlineIndex = _nextLoop / _numOffsets;
    int offsetIndex = _nextLoop % _numOffsets;
    float inline = _inlineStart + inlineIndex * _prestack3d.getInlineDelta();
    float offset = _offsetStart + offsetIndex * _prestack3d.getOffsetDelta();
    _message = "Reading inline " + inline + ", offset " + offset + ".";

    // Before reading, create a coordinate series with the x,y coordinates
    // of all the potential traces to be read.
    float[] inlinesTemp = new float[_numXlines];
    float[] xlinesTemp = new float[_numXlines];
    float[] offsetsTemp = new float[_numXlines];
    for (int i = 0; i < _numXlines; i++) {
      inlinesTemp[i] = inline;
      xlinesTemp[i] = _xlineStart + i * _prestack3d.getXlineDelta();
      offsetsTemp[i] = offset;
    }
    CoordinateSeries coords = _prestack3d.getSurvey().transformInlineXlineToXY(inlinesTemp, xlinesTemp);

    // Keep only those points inside the AOI.
    int counter = 0;
    for (int i = 0; i < coords.getNumPoints(); i++) {
      Point3d point = coords.getPoint(i);
      if (_aoi == null || _aoi.contains(point.getX(), point.getY())) {
        inlinesTemp[counter] = inlinesTemp[i];
        xlinesTemp[counter] = xlinesTemp[i];
        offsetsTemp[counter] = offsetsTemp[i];
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
    float[] offsets = new float[counter];
    System.arraycopy(inlinesTemp, 0, inlines, 0, counter);
    System.arraycopy(xlinesTemp, 0, xlines, 0, counter);
    System.arraycopy(offsetsTemp, 0, offsets, 0, counter);

    // Read the traces.
    TraceData traceData = _prestack3d.getTraces(inlines, xlines, offsets, _startZ, _endZ);

    // Return the traces.
    return traceData.getTraces();
  }

  @Override
  protected int getNumLoops() {
    return _numInlines * _numOffsets;
  }
}
