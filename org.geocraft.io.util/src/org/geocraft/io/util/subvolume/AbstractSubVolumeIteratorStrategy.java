/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.util.subvolume;


import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.datatypes.SpatialExtent;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PreStack3d;
import org.geocraft.core.model.seismic.SeismicSurvey3d;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;


public abstract class AbstractSubVolumeIteratorStrategy implements ISubVolumeIteratorStrategy {

  /** The area-of-interest. */
  protected AreaOfInterest _aoi;

  /** The starting z value. */
  protected float _startZ;

  /** The ending z value. */
  protected float _endZ;

  /** The status message. */
  protected String _message;

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

  protected final static ILogger LOGGER = ServiceProvider.getLoggingService().getLogger(
      AbstractSubVolumeIteratorStrategy.class);

  /**
   * The default constructor with an area-of-interest.
   * 
   * @param ps3d the poststack 3d volume to read.
   * @param aoi the area-of-interest.
   */
  public AbstractSubVolumeIteratorStrategy(final PostStack3d ps3d, final AreaOfInterest aoi, final float startZ, final float endZ) {
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
    _nextLoop = 0;
  }

  public boolean isDone() {
    return _nextLoop >= getNumLoops();
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

  public float getCompletion() {
    return 100f * _nextLoop / getNumLoops();
  }

  protected void incrementNextLoop() {
    _nextLoop++;
  }

  protected abstract int getNumLoops();
}
