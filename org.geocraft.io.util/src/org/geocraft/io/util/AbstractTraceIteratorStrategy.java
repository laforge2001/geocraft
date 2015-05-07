/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */

package org.geocraft.io.util;


import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.aoi.SeismicSurvey2dAOI;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.SpatialExtent;
import org.geocraft.core.model.seismic.PostStack2dLine;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PreStack3d;
import org.geocraft.core.model.seismic.SeismicLine2d;
import org.geocraft.core.model.seismic.SeismicSurvey3d;


/**
 * Defines an abstract read strategy for a poststack trace iterator. The main
 * feature of this base class is to determine the AOI-limited inline,xline
 * ranges that are to be read. This class can be sub-classes to create
 * implementations for iterating thru a volume in the inline, xline or other
 * custom directions.
 */
public abstract class AbstractTraceIteratorStrategy implements ITraceIteratorStrategy {

  /** The prestack3d volume to read. */
  //protected PreStack3d _prestack3d;
  /** The poststack 3d volume to read. */
  //protected PostStack3d _poststack3d;
  /** The poststack 2d volume to read. */
  //protected PostStack2d _poststack2d;
  //protected SeismicDataset _seismicDataset;
  /** The area-of-interest. */
  protected AreaOfInterest _aoi;

  /** The starting z value. */
  protected float _startZ;

  /** The ending z value. */
  protected float _endZ;

  /** The status message. */
  protected String _message;

  /** The iterator done flag. */
  protected boolean _isDone;

  /** The number of inlines to read. */
  protected int _numInlines;

  /** The number of xlines to read. */
  protected int _numXlines;

  /** The number of offsets to read. */
  protected int _numOffsets;

  /** The AOI-limited starting inline. */
  protected float _inlineStart;

  /** The AOI-limited ending inline. */
  protected float _inlineEnd;

  /** The AOI-limited starting inline. */
  protected float _xlineStart;

  /** The AOI-limited ending inline. */
  protected float _xlineEnd;

  /** The starting offset. */
  protected float _offsetStart;

  /** The ending offset. */
  protected float _offsetEnd;

  /** The current loop index. */
  protected int _nextLoop;

  /**
   * The default constructor with an area-of-interest.
   * 
   * @param ps3d the poststack 3d volume to read.
   * @param aoi the area-of-interest.
   */
  public AbstractTraceIteratorStrategy(final PostStack3d ps3d, final AreaOfInterest aoi, final float startZ, final float endZ) {
    _aoi = aoi;
    _startZ = startZ;
    _endZ = endZ;
    float[] results = getInlineAndXlineRange(ps3d, aoi);
    _inlineStart = results[0];
    _inlineEnd = results[1];
    _xlineStart = results[2];
    _xlineEnd = results[3];
    _numInlines = 1 + Math.round((_inlineEnd - _inlineStart) / ps3d.getInlineDelta());
    _numInlines = Math.max(_numInlines, 0);
    _numXlines = 1 + Math.round((_xlineEnd - _xlineStart) / ps3d.getXlineDelta());
    _numXlines = Math.max(_numXlines, 0);
    _isDone = false;
    _nextLoop = 0;
  }

  /**
   * The default constructor with an area-of-interest.
   * 
   * @param ps3d the prestack 3d volume to read.
   * @param aoi the area-of-interest.
   */
  public AbstractTraceIteratorStrategy(final PreStack3d ps3d, final AreaOfInterest aoi, final float startZ, final float endZ) {
    _aoi = aoi;
    _startZ = startZ;
    _endZ = endZ;
    float[] results = getInlineAndXlineRange(ps3d, aoi);
    _inlineStart = results[0];
    _inlineEnd = results[1];
    _xlineStart = results[2];
    _xlineEnd = results[3];
    _numInlines = 1 + Math.round((_inlineEnd - _inlineStart) / ps3d.getInlineDelta());
    _numInlines = Math.max(_numInlines, 0);
    _numXlines = 1 + Math.round((_xlineEnd - _xlineStart) / ps3d.getXlineDelta());
    _numXlines = Math.max(_numXlines, 0);
    _offsetStart = ps3d.getOffsetStart();
    _offsetEnd = ps3d.getOffsetEnd();
    _numOffsets = ps3d.getNumOffsets();
    _isDone = false;
    _nextLoop = 0;
  }

  /**
   * The default constructor with an area-of-interest.
   * 
   * @param ps2d the poststack 2d volume to read.
   * @param aoi the area-of-interest.
   */
  public AbstractTraceIteratorStrategy(final PostStack2dLine ps2d, final AreaOfInterest aoi, final float startZ, final float endZ) {
    _aoi = aoi;
    _startZ = startZ;
    _endZ = endZ;
    float[] results = get2dRange(ps2d, aoi);
    float cdpStart = results[0];
    float cdpEnd = results[1];
    _numInlines = 1;
    _inlineStart = ps2d.getLineNumber();
    _inlineEnd = ps2d.getLineNumber();
    _xlineStart = cdpStart;
    _xlineEnd = cdpEnd;
    _numXlines = 1 + Math.round((_xlineEnd - _xlineStart) / ps2d.getCdpDelta());
    _numXlines = Math.max(_numXlines, 0);
    _isDone = false;
    _nextLoop = 0;
  }

  public boolean isDone() {
    return _isDone;
  }

  public String getMessage() {
    return _message;
  }

  /**
   * Gets the AOI-limited range for inlines and xlines to read. If the AOI
   * specified is null, then the ranges returned are simple that of the
   * poststack volume.
   * 
   * @param ps3d the poststack volume.
   * @param aoi the area-of-interest.
   * @return an array containing the starting inline, ending inline, starting
   *         xline, ending xline.
   */
  private float[] getInlineAndXlineRange(final PostStack3d ps3d, final AreaOfInterest aoi) {
    // If no AOI specified, then simple return the full poststack3d inline,xline
    // ranges.
    if (aoi == null) {
      return new float[] { ps3d.getInlineStart(), ps3d.getInlineEnd(), ps3d.getXlineStart(), ps3d.getXlineEnd() };
    }
    // Convert the corners of the AOI spatial extent to inline,xline
    // coordinates.
    // Then establish the inline,xline ranges.
    SpatialExtent extent = aoi.getExtent();
    SeismicSurvey3d geometry = ps3d.getSurvey();
    double[] xs = { extent.getMinX(), extent.getMinX(), extent.getMaxX(), extent.getMaxX() };
    double[] ys = { extent.getMinY(), extent.getMaxY(), extent.getMaxY(), extent.getMinY() };
    float inlineMin = Float.MAX_VALUE;
    float inlineMax = -Float.MAX_VALUE;
    float xlineMin = Float.MAX_VALUE;
    float xlineMax = -Float.MAX_VALUE;
    for (int i = 0; i < 4; i++) {
      float[] ixln = geometry.transformXYToInlineXline(xs[i], ys[i], true);
      inlineMin = Math.min(inlineMin, ixln[0]);
      inlineMax = Math.max(inlineMax, ixln[0]);
      xlineMin = Math.min(xlineMin, ixln[1]);
      xlineMax = Math.max(xlineMax, ixln[1]);
    }
    inlineMin = Math.max(inlineMin, Math.min(ps3d.getInlineStart(), ps3d.getInlineEnd()));
    inlineMax = Math.min(inlineMax, Math.max(ps3d.getInlineStart(), ps3d.getInlineEnd()));
    xlineMin = Math.max(xlineMin, Math.min(ps3d.getXlineStart(), ps3d.getXlineEnd()));
    xlineMax = Math.min(xlineMax, Math.max(ps3d.getXlineStart(), ps3d.getXlineEnd()));
    float inlineStart = inlineMin;
    float inlineEnd = inlineMax;
    float xlineStart = xlineMin;
    float xlineEnd = xlineMax;
    if (ps3d.getInlineDelta() < 0) {
      float temp = inlineStart;
      inlineStart = inlineEnd;
      inlineEnd = temp;
    }
    if (ps3d.getXlineDelta() < 0) {
      float temp = xlineStart;
      xlineStart = xlineEnd;
      xlineEnd = temp;
    }
    return new float[] { inlineStart, inlineEnd, xlineStart, xlineEnd };
  }

  /**
   * Gets the AOI-limited range for inlines and xlines to read. If the AOI
   * specified is null, then the ranges returned are simple that of the
   * prestack volume.
   * 
   * @param ps3d the prestack volume.
   * @param aoi the area-of-interest.
   * @return an array containing the starting inline, ending inline, starting
   *         xline, ending xline.
   */
  private float[] getInlineAndXlineRange(final PreStack3d ps3d, final AreaOfInterest aoi) {
    // If no AOI specified, then simple return the full poststack3d inline,xline
    // ranges.
    if (aoi == null) {
      return new float[] { ps3d.getInlineStart(), ps3d.getInlineEnd(), ps3d.getXlineStart(), ps3d.getXlineEnd() };
    }
    // Convert the corners of the AOI spatial extent to inline,xline
    // coordinates.
    // Then establish the inline,xline ranges.
    SpatialExtent extent = aoi.getExtent();
    SeismicSurvey3d geometry = ps3d.getSurvey();
    double[] xs = { extent.getMinX(), extent.getMinX(), extent.getMaxX(), extent.getMaxX() };
    double[] ys = { extent.getMinY(), extent.getMaxY(), extent.getMaxY(), extent.getMinY() };
    float inlineMin = Float.MAX_VALUE;
    float inlineMax = -Float.MAX_VALUE;
    float xlineMin = Float.MAX_VALUE;
    float xlineMax = -Float.MAX_VALUE;
    for (int i = 0; i < 4; i++) {
      float[] ixln = geometry.transformXYToInlineXline(xs[i], ys[i], true);
      inlineMin = Math.min(inlineMin, ixln[0]);
      inlineMax = Math.max(inlineMax, ixln[0]);
      xlineMin = Math.min(xlineMin, ixln[1]);
      xlineMax = Math.max(xlineMax, ixln[1]);
    }
    inlineMin = Math.max(inlineMin, Math.min(ps3d.getInlineStart(), ps3d.getInlineEnd()));
    inlineMax = Math.min(inlineMax, Math.max(ps3d.getInlineStart(), ps3d.getInlineEnd()));
    xlineMin = Math.max(xlineMin, Math.min(ps3d.getXlineStart(), ps3d.getXlineEnd()));
    xlineMax = Math.min(xlineMax, Math.max(ps3d.getXlineStart(), ps3d.getXlineEnd()));
    float inlineStart = inlineMin;
    float inlineEnd = inlineMax;
    float xlineStart = xlineMin;
    float xlineEnd = xlineMax;
    if (ps3d.getInlineDelta() < 0) {
      float temp = inlineStart;
      inlineStart = inlineEnd;
      inlineEnd = temp;
    }
    if (ps3d.getXlineDelta() < 0) {
      float temp = xlineStart;
      xlineStart = xlineEnd;
      xlineEnd = temp;
    }
    return new float[] { inlineStart, inlineEnd, xlineStart, xlineEnd };
  }

  /**
   * Gets the AOI-limited range for shot points and CDPs to read. If the AOI
   * specified is null, then the ranges returned are simple that of the
   * poststack volume.
   * 
   * @param ps2d the poststack volume.
   * @param aoi the area-of-interest.
   * @return an array containing the starting shot point, ending shot point, starting
   *         cdp, ending cdp.
   */
  private float[] get2dRange(final PostStack2dLine ps2d, final AreaOfInterest aoi) {
    // If no AOI specified, then simple return the full poststack3d inline,xline
    // ranges.
    if (aoi == null) {
      return new float[] { ps2d.getCdpStart(), ps2d.getCdpEnd(), ps2d.getShotpointStart(), ps2d.getShotpointEnd() };
    }

    // Determine the shot point range.
    SeismicLine2d seismicLine = ps2d.getSeismicLine();
    String lineName = seismicLine.getDisplayName();
    FloatRange cdpRange = null;
    if (aoi instanceof SeismicSurvey2dAOI) {
      SeismicSurvey2dAOI aoi2d = (SeismicSurvey2dAOI) aoi;
      cdpRange = aoi2d.getCdpRange(lineName);
    }
    if (cdpRange == null) {
      return new float[] { ps2d.getCdpStart(), ps2d.getCdpEnd(), ps2d.getShotpointStart(), ps2d.getShotpointEnd() };
    }
    // Determine the cdp range.
    float cdpStart = cdpRange.getStart();
    float cdpEnd = cdpRange.getEnd();
    float cdpMin = Math.min(cdpStart, cdpEnd);
    float cdpMax = Math.max(cdpStart, cdpEnd);

    // Determine the shotpoint range.
    float shotpointStart = seismicLine.transformCDPToShotpoint(cdpStart);
    float shotpointEnd = seismicLine.transformCDPToShotpoint(cdpEnd);
    float shotpointMin = Math.min(shotpointStart, shotpointEnd);
    float shotpointMax = Math.max(shotpointStart, shotpointEnd);

    shotpointMin = Math.max(shotpointMin, Math.min(ps2d.getShotpointStart(), ps2d.getShotpointEnd()));
    shotpointMax = Math.min(shotpointMax, Math.max(ps2d.getShotpointStart(), ps2d.getShotpointEnd()));
    cdpMin = Math.max(cdpMin, Math.min(ps2d.getCdpStart(), ps2d.getCdpEnd()));
    cdpMax = Math.min(cdpMax, Math.max(ps2d.getCdpStart(), ps2d.getCdpEnd()));

    shotpointStart = shotpointMin;
    shotpointEnd = shotpointMax;
    cdpStart = cdpMin;
    cdpEnd = cdpMax;
    float shotpointDiff = ps2d.getShotpointEnd() - ps2d.getShotpointStart();
    if (shotpointDiff < 0) {
      float temp = shotpointStart;
      shotpointStart = shotpointEnd;
      shotpointEnd = temp;
    }
    if (ps2d.getCdpDelta() < 0) {
      float temp = cdpStart;
      cdpStart = cdpEnd;
      cdpEnd = temp;
    }
    return new float[] { cdpStart, cdpEnd, shotpointStart, shotpointEnd };
  }

  public float getCompletion() {
    return 100f * _nextLoop / getNumLoops();
  }

  protected abstract int getNumLoops();
}
