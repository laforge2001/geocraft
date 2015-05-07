/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.util;


public class BoundedHorizonTraceIteratorStrategy {//extends AbstractTraceIteratorStrategy {

  //  private final Interpretation _horizon;
  //
  //  private final ValueObject _horizonGeometry;
  //
  //  private final int _startBound;
  //
  //  private final int _endBound;
  //
  //  /**
  //   * @param ps3d
  //   * @param aoi
  //   * @param startZ
  //   * @param endZ
  //   */
  //  public BoundedHorizonTraceIteratorStrategy(final PostStack3d ps, final AreaOfInterest aoi, final Grid horizon, final int startB, final int endB) {
  //    super(ps, aoi, 0.0f, 0.0f);
  //    _horizon = horizon;
  //    _horizonGeometry = ((Grid) _horizon).getGeometry();
  //    _startBound = startB;
  //    _endBound = endB;
  //  }
  //
  //  public BoundedHorizonTraceIteratorStrategy(final PostStack2d ps, final AreaOfInterest aoi, final Grid2d horizon, final int startB, final int endB) {
  //    super(ps, aoi, 0.0f, 0.0f);
  //    _horizon = horizon;
  //    _horizonGeometry = ((Grid2d) _horizon).getLineGeometry();
  //    _startBound = startB;
  //    _endBound = endB;
  //  }
  //
  //  /* (non-Javadoc)
  //   * @see org.geocraft.io.util.ITraceIteratorStrategy#getCompletion()
  //   */
  //  @Override
  //  public float getCompletion() {
  //    return 100f * _nextLoop / _numInlines;
  //  }
  //
  //  public Trace[] readNext() {
  //    if (_poststack2d != null) {
  //      return null; //readNext2d();
  //    } else if (_poststack3d != null) {
  //      return readNext3d();
  //    }
  //    return new Trace[0];
  //  }
  //
  //  //  /**
  //  //   * {@inheritDoc}
  //  //   */
  //  //  public Trace[] readNext2d() {
  //  //    //System.out.println("read next " + _nextLoop + " " + _numInlines);
  //  //    if (_nextLoop >= _numInlines) {
  //  //      _isDone = true;
  //  //      _message = "Done reading inlines...";
  //  //      return new Trace[0];
  //  //    }
  //  //    float inline = _inlineStart;
  //  //    _message = "Reading inline " + inline + ".";
  //  //
  //  //    // Before reading, create a coordinate series with the x,y coordinates
  //  //    // of all the potential traces to be read.
  //  //    float[] cdpsTemp = new float[_numXlines];
  //  //    for (int i = 0; i < _numXlines; i++) {
  //  //      cdpsTemp[i] = _xlineStart + i * _ps2d.getCdpDelta();
  //  //    }
  //  //    CoordinateSeries coords = _ps2d.getSeismicGeometry().transformCDPsToXYs(cdpsTemp);
  //  //
  //  //    // Keep only those points inside the AOI.
  //  //    int counter = 0;
  //  //    for (int i = 0; i < coords.getNumPoints(); i++) {
  //  //      Point3d point = coords.getPoint(i);
  //  //      if (_aoi == null || _aoi.contains(point.getX(), point.getY())) {
  //  //        cdpsTemp[counter] = cdpsTemp[i];
  //  //        counter++;
  //  //      }
  //  //    }
  //  //
  //  //    // Increment the loop index.
  //  //    _nextLoop++;
  //  //
  //  //    if (counter == 0) {
  //  //      return new Trace[0];
  //  //    }
  //  //
  //  //    float[] cdps = new float[counter];
  //  //    System.arraycopy(cdpsTemp, 0, cdps, 0, counter);
  //  //
  //  //    // Read the traces.
  //  //    TraceData traceData = _ps2d.getTraces(cdps, _startZ, _endZ);
  //  //
  //  //    // Return the traces.
  //  //    return traceData.getTraces();
  //  //  }
  //
  //  //  private float getZValue2d(final Point point) {
  //  //    return -1;
  //  //  }
  //
  //  private float getZValue3d(final Point3d point) {
  //    double[] rc = ((GridGeometry) _horizonGeometry).transformXYToRowCol(point.getX(), point.getY(), true);
  //    float zValue = ((Grid) _horizon).getValueAtRowCol((int) Math.round(rc[0]), (int) Math.round(rc[1]));
  //
  //    return zValue;
  //  }
  //
  //  //  /**
  //  //   * Validates the specified z value against the input volumes.
  //  //   * @param z the z value to validate.
  //  //   * @return the specified z value, or NaN if invalid.
  //  //   */
  //  //  private float validateZRangeAgainstVolumes(final float z) {
  //  //    if (z < _startZA || z > _endZA || z < _startZB || z > _endZB) {
  //  //      return Float.NaN;
  //  //    }
  //  //    return z;
  //  //  }
  //
  //  /**
  //   * Validates the specified z value against the input volumes.
  //   * @param z the z value to validate.
  //   * @return the specified z value, or NaN if invalid.
  //   */
  //  private float validateZRangeAgainstVolumes(final float z) {
  //    if (z < _poststack3d.getZStart() || z > _poststack3d.getZEnd()) {
  //      return Float.NaN;
  //    }
  //    return z;
  //  }
  //
  //  private float[] computeStartAndEndZ(final Point3d point) {
  //    float startZ = _startBound;
  //    float endZ = _endBound;
  //    if (_horizonGeometry instanceof GridGeometry) {
  //
  //      float zValue = getZValue3d(point);
  //      boolean validStartZ = !((Grid) _horizon).isNull(zValue);
  //      if (validStartZ) {
  //        startZ = zValue + _startBound;
  //        endZ = zValue + _endBound;
  //      } else {
  //        startZ = Float.NaN;
  //        endZ = Float.NaN;
  //        return null;
  //      }
  //      int index = Math.round(startZ / _poststack3d.getZDelta());
  //      startZ = index * _poststack3d.getZDelta();
  //      startZ = validateZRangeAgainstVolumes(startZ);
  //      index = Math.round(endZ / _poststack3d.getZDelta());
  //      endZ = index * _poststack3d.getZDelta();
  //      endZ = validateZRangeAgainstVolumes(endZ);
  //    }
  //
  //    return new float[] { startZ, endZ };
  //  }
  //
  //  /**
  //   * {@inheritDoc}
  //   */
  //  public Trace[] readNext3d() {
  //    //System.out.println("read next " + _nextLoop + " " + _numInlines);
  //    if (_nextLoop >= _numInlines) {
  //      _isDone = true;
  //      _message = "Done reading inlines...";
  //      return new Trace[0];
  //    }
  //    float inline = _inlineStart + _nextLoop * _poststack3d.getInlineDelta();
  //    _message = "Reading inline " + inline + ".";
  //
  //    // Before reading, create a coordinate series with the x,y coordinates
  //    // of all the potential traces to be read.
  //    float[] inlinesTemp = new float[_numXlines];
  //    float[] xlinesTemp = new float[_numXlines];
  //    float[] zStartTemp = new float[_numXlines];
  //    float[] zEndTemp = new float[_numXlines];
  //    for (int i = 0; i < _numXlines; i++) {
  //      inlinesTemp[i] = inline;
  //      xlinesTemp[i] = _xlineStart + i * _poststack3d.getXlineDelta();
  //    }
  //    CoordinateSeries coords = _poststack3d.getSeismicGeometry().transformInlineXlineToXY(inlinesTemp, xlinesTemp);
  //
  //    // Keep only those points inside the AOI.
  //    int counter = 0;
  //    for (int i = 0; i < coords.getNumPoints(); i++) {
  //      Point3d point = coords.getPoint(i);
  //      float[] zrange = computeStartAndEndZ(point);
  //      if (zrange != null && (_aoi == null || _aoi.contains(point.getX(), point.getY()))) {
  //        inlinesTemp[counter] = inlinesTemp[i];
  //        xlinesTemp[counter] = xlinesTemp[i];
  //        zStartTemp[counter] = zrange[0];
  //        zEndTemp[counter] = zrange[1];
  //        counter++;
  //      }
  //    }
  //
  //    // Increment the loop index.
  //    _nextLoop++;
  //
  //    if (counter == 0) {
  //      return new Trace[0];
  //    }
  //
  //    float[] inlines = new float[counter];
  //    float[] xlines = new float[counter];
  //    System.arraycopy(inlinesTemp, 0, inlines, 0, counter);
  //    System.arraycopy(xlinesTemp, 0, xlines, 0, counter);
  //
  //    //    TraceIterator iterator = TraceIteratorFactory.create(_ps3d, _aoi);
  //    //    while (iterator.hasNext()) {
  //    //      TraceData tdata = iterator.next();
  //    //      for (int t = 0; t < tdata.getNumTraces(); ++t) {
  //    //        Trace trace = tdata.getTrace(t);
  //    //
  //    //      }
  //    //    }
  //    float[] inlinesTemp2 = new float[1];
  //    float[] xlinesTemp2 = new float[1];
  //
  //    Trace[] resultTraces = new Trace[inlines.length];
  //    // Read the traces.
  //    for (int i = 0; i < inlines.length; ++i) {
  //      inlinesTemp2[0] = inlines[i];
  //      xlinesTemp2[0] = xlines[i];
  //
  //      TraceData traceData = _poststack3d.getTraces(inlinesTemp2, xlinesTemp2, zStartTemp[i], zEndTemp[i]);
  //      resultTraces[i] = traceData.getTrace(0);
  //    }
  //
  //    // Return the traces.
  //    return resultTraces;
  //  }

}
