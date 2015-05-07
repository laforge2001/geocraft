/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */

package org.geocraft.core.model.aoi;


import org.geocraft.core.common.math.MathUtil;
import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.datatypes.SpatialExtent;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.InMemoryMapper;
import org.geocraft.core.model.seismic.SeismicSurvey2d;
import org.geocraft.core.model.seismic.SeismicSurvey3d;


/**
 * This class represents an area-of-interest (AOI) defines on a 3D seismic survey.
 * <p>
 * This 3D AOI consists of an inline range (start,end,delta) and an xline range (start,end,delta).
 * This AOI can be used to define a subset of data on which to operate.
 */
public class SeismicSurvey3dAOI extends AreaOfInterest {

  /** The epsilon used for validity checking. */
  private static final float EPSILON = 0.01f;

  /** The seismic survey on which the AOI is defined. */
  private SeismicSurvey3d _survey;

  /** The spatial extent (x,y) of the AOI. */
  private SpatialExtent _extent;

  /** The inline range (start,end,delta). */
  private FloatRange _inlineRange;

  /** The crossline range (start,end,delta). */
  private FloatRange _xlineRange;

  /** The minimum inline #. */
  private float _inlineMin;

  /** The maximum inline #. */
  private float _inlineMax;

  /** The minimum xline #. */
  private float _xlineMin;

  /** The maximum xline #. */
  private float _xlineMax;

  /** The decimated flag: <i>true</i> if any inline or xline is skipped, <i>false</i> if not. */
  private boolean _isDecimated;

  /**
   * Generates a default name for the AOI, based on the survey name and the
   * inline and xline ranges.
   * <p>
   * @param survey the 3D seismic survey.
   * @param inlineStart the starting inline.
   * @param inlineEnd the ending inline.
   * @param inlineDelta the delta inline.
   * @param xlineStart the starting xline.
   * @param xlineEnd the ending xline.
   * @param xlineDelta the delta xline.
   * @return the generated AOI name.
   */
  public static String generateName(final SeismicSurvey3d survey, final float inlineStart, final float inlineEnd,
      final float inlineDelta, final float xlineStart, final float xlineEnd, final float xlineDelta) {
    return survey.getDisplayName() + "_IL(" + inlineStart + "," + inlineEnd + "," + inlineDelta + ")" + "_XL("
        + xlineStart + "," + xlineEnd + "," + xlineDelta + ")";
  }

  /**
   * Constructs an in-memory AOI based on the given 3D seismic survey, with the given start/end inlines and xlines.
   * <p>
   * The resulting AOI is non-decimated, meaning that no inlines or xlines are skipped between the start and end values.
   * <p>
   * The resulting AOI is considered <i>loaded</i>.
   * 
   * @param name the name of the AOI; or an empty string to have a name auto-generated.
   * @param survey the 3D seismic survey on which the AOI is defined.
   * @param inlineStart the starting inline.
   * @param inlineEnd the ending inline.
   * @param xlineStart the starting xline.
   * @param xlineEnd the ending xline.
   */
  public SeismicSurvey3dAOI(final String name, final SeismicSurvey3d survey, final float inlineStart, final float inlineEnd, final float xlineStart, final float xlineEnd) {
    this(name, survey, inlineStart, inlineEnd, survey.getInlineDelta(), xlineStart, xlineEnd, survey.getXlineDelta(),
        new InMemoryMapper(SeismicSurvey3dAOI.class));
  }

  /**
   * Constructs an AOI based on the given 3D seismic survey, with the given start/end inlines and xlines.
   * <p>
   * The resulting AOI is non-decimated, meaning that no inlines or xlines are skipped between the start and end values.
   * <p>
   * The resulting AOI is considered <i>loaded</i>.
   * 
   * @param name the name of the AOI; or an empty string to have a name auto-generated.
   * @param survey the 3D seismic survey on which the AOI is defined.
   * @param inlineStart the starting inline.
   * @param inlineEnd the ending inline.
   * @param xlineStart the starting xline.
   * @param xlineEnd the ending xline.
   */
  public SeismicSurvey3dAOI(final String name, final SeismicSurvey3d survey, final float inlineStart, final float inlineEnd, final float xlineStart, final float xlineEnd, final IMapper aoiMapper) {
    this(name, survey, inlineStart, inlineEnd, survey.getInlineDelta(), xlineStart, xlineEnd, survey.getXlineDelta(),
        aoiMapper);
  }

  /**
   * Constructs an in-memory AOI based on the given 3D seismic survey, with the given inline and xline ranges (start,end,delta).
   * <p>
   * The resulting AOI is optionally decimated, meaning that inlines or xlines could be skipped. This occurs if the
   * given inline and xlines deltas are greater than the inline and xline deltas of the survey.
   * <p>
   * The resulting AOI is considered <i>loaded</i>.
   * 
   * @param name the name of the AOI; or an empty string to have a name auto-generated.
   * @param survey the 3D seismic survey on which the AOI is defined.
   * @param inlineStart the starting inline.
   * @param inlineEnd the ending inline.
   * @param inlineDelta the inline delta.
   * @param xlineStart the starting xline.
   * @param xlineEnd the ending xline.
   * @param xlineDelta the xline delta.
   */
  public SeismicSurvey3dAOI(final String name, final SeismicSurvey3d survey, final float inlineStart, final float inlineEnd, final float inlineDelta, final float xlineStart, final float xlineEnd, final float xlineDelta) {
    this(name, survey, inlineStart, inlineEnd, inlineDelta, xlineStart, xlineEnd, xlineDelta, new InMemoryMapper(
        SeismicSurvey3dAOI.class));
  }

  /**
   * Constructs an AOI based on the given 3D seismic survey, with the given inline and xline ranges (start,end,delta).
   * <p>
   * The resulting AOI is optionally decimated, meaning that inlines or xlines could be skipped. This occurs if the
   * given inline and xlines deltas are greater than the inline and xline deltas of the survey.
   * <p>
   * The resulting AOI is considered <i>loaded</i>.
   * 
   * @param name the name of the AOI; or an empty string to have a name auto-generated.
   * @param survey the 3D seismic survey on which the AOI is defined.
   * @param inlineStart the starting inline.
   * @param inlineEnd the ending inline.
   * @param inlineDelta the inline delta.
   * @param xlineStart the starting xline.
   * @param xlineEnd the ending xline.
   * @param xlineDelta the xline delta.
   */
  public SeismicSurvey3dAOI(final String name, final SeismicSurvey3d survey, final float inlineStart, final float inlineEnd, final float inlineDelta, final float xlineStart, final float xlineEnd, final float xlineDelta, final IMapper aoiMapper) {
    super(name, aoiMapper);
    int inlineDeltaFactor = Math.round(inlineDelta / survey.getInlineDelta());
    int xlineDeltaFactor = Math.round(xlineDelta / survey.getXlineDelta());
    float inlineDeltaAbs = Math.abs(inlineDeltaFactor * survey.getInlineDelta());
    float xlineDeltaAbs = Math.abs(xlineDeltaFactor * survey.getXlineDelta());
    int inlineStartIndex = Math.round((inlineStart - survey.getInlineStart()) / survey.getInlineDelta());
    int inlineEndIndex = Math.round((inlineEnd - survey.getInlineStart()) / survey.getInlineDelta());
    int xlineStartIndex = Math.round((xlineStart - survey.getXlineStart()) / survey.getXlineDelta());
    int xlineEndIndex = Math.round((xlineEnd - survey.getXlineStart()) / survey.getXlineDelta());
    float inlineStartTemp = survey.getInlineStart() + inlineStartIndex * survey.getInlineDelta();
    float inlineEndTemp = survey.getInlineStart() + inlineEndIndex * survey.getInlineDelta();
    float xlineStartTemp = survey.getXlineStart() + xlineStartIndex * survey.getXlineDelta();
    float xlineEndTemp = survey.getXlineStart() + xlineEndIndex * survey.getXlineDelta();
    float inlineDeltaTemp = 1;
    float xlineDeltaTemp = 1;
    if (inlineStartTemp > inlineEndTemp) {
      inlineDeltaTemp = -inlineDeltaAbs;
    } else {
      inlineDeltaTemp = inlineDeltaAbs;
    }
    if (xlineStartTemp > xlineEndTemp) {
      xlineDeltaTemp = -xlineDeltaAbs;
    } else {
      xlineDeltaTemp = xlineDeltaAbs;
    }
    _inlineRange = new FloatRange(inlineStartTemp, inlineEndTemp, inlineDeltaTemp);
    _xlineRange = new FloatRange(xlineStartTemp, xlineEndTemp, xlineDeltaTemp);

    // If no name is specified, default to an auto-generated one.
    if (name.length() == 0) {
      setDisplayName(generateName(survey, inlineStartTemp, inlineEndTemp, inlineDeltaTemp, xlineStartTemp,
          xlineEndTemp, xlineDeltaTemp));
    }

    FloatRange inlineRange = new FloatRange(survey.getInlineStart(), survey.getInlineEnd(), survey.getInlineDelta());
    FloatRange xlineRange = new FloatRange(survey.getXlineStart(), survey.getXlineEnd(), survey.getXlineDelta());
    _survey = new SeismicSurvey3d(name, inlineRange, xlineRange, survey.getCornerPoints(), survey.getOrientation());

    // Check if the AOI is decimated.
    _isDecimated = !MathUtil.isEqual(inlineDeltaTemp, survey.getInlineDelta())
        || !MathUtil.isEqual(xlineDeltaTemp, survey.getXlineDelta());

    calculateInlineXlineMinMax();

    calculateSpatialExtent();

    markLoaded();
  }

  /**
   * Constructs an AOI defined on a 3D seismic survey.
   * <p>
   * The resulting AOI will be lazy-loaded from its underlying datastore when needed.
   */
  public SeismicSurvey3dAOI(final String displayName, final IMapper mapper) {
    super(displayName, mapper);
  }

  /**
   * Calculates the minimum and maximum inline and xline values.
   * <p>
   * This are not to be confused with starting and ending inlines and xlines.
   */
  private void calculateInlineXlineMinMax() {
    _inlineMin = Math.min(_inlineRange.getStart(), _inlineRange.getEnd());
    _inlineMax = Math.max(_inlineRange.getStart(), _inlineRange.getEnd());
    _xlineMin = Math.min(_xlineRange.getStart(), _xlineRange.getEnd());
    _xlineMax = Math.max(_xlineRange.getStart(), _xlineRange.getEnd());
  }

  /**
   * Calculates the spatial extent of the AOI in terms of x,y coordinates.
   */
  private void calculateSpatialExtent() {
    float[] inlines = { _inlineRange.getStart(), _inlineRange.getStart(), _inlineRange.getEnd(), _inlineRange.getEnd() };
    float[] xlines = { _xlineRange.getStart(), _xlineRange.getEnd(), _xlineRange.getEnd(), _xlineRange.getStart() };
    CoordinateSeries coords = _survey.transformInlineXlineToXY(inlines, xlines);
    double[] xs = new double[coords.getNumPoints()];
    double[] ys = new double[coords.getNumPoints()];
    double[] zs = new double[coords.getNumPoints()];
    for (int i = 0; i < coords.getNumPoints(); i++) {
      Point3d point = coords.getPoint(i);
      xs[i] = point.getX();
      ys[i] = point.getY();
      zs[i] = point.getZ();
    }
    _extent = new SpatialExtent(xs, ys, zs, Domain.TIME);
  }

  @Override
  public boolean contains(final double x, final double y) {
    load();

    // Transform the x,y coordinates into inline,xline coordinates.
    float[] ixln = _survey.transformXYToInlineXline(x, y, true);
    float inline = Math.round(ixln[0]);
    float xline = Math.round(ixln[1]);
    // If the inline or xline are beyond the min or ,max, then return false.
    if (inline < _inlineMin || inline > _inlineMax || xline < _xlineMin || xline > _xlineMax) {
      return false;
    }
    // If the inline does not fall on a delta, then return false.
    if (Math.abs((inline - _inlineRange.getStart()) % _inlineRange.getDelta()) > EPSILON
        && Math.abs((inline - _inlineRange.getStart()) % _inlineRange.getDelta()) < Math.abs(_inlineRange.getDelta())
            - EPSILON) {
      return false;
    }
    // If the xline does not fall on a delta, then return false.
    if (Math.abs((xline - _xlineRange.getStart()) % _xlineRange.getDelta()) > EPSILON
        && Math.abs((xline - _xlineRange.getStart()) % _xlineRange.getDelta()) < Math.abs(_xlineRange.getDelta())
            - EPSILON) {
      return false;
    }
    // Passed all checks, so return true.
    return true;
  }

  @Override
  public boolean contains(final double x, final double y, final SeismicSurvey2d survey) {
    // Simply ignore the 2D seismic survey and check the x,y.
    return contains(x, y);
  }

  /**
   * Returns the spatial extent of the AOI, in terms of x,y coordinates.
   * 
   * @return the spatial extent of the AOI.
   */
  @Override
  public SpatialExtent getExtent() {
    load();
    return _extent;
  }

  /**
   * Gets the 3D seismic survey on which the AOI is defined..
   * 
   * @return the 3D seismic survey.
   */
  public SeismicSurvey3d getSurvey() {
    load();
    return _survey;
  }

  /**
   * Returns the number of inlines contained in the AOI.
   * <p>
   * This takes into account the possible decimation of inlines.
   * 
   * @return the number of inlines in the AOI.
   */
  public int getNumInlines() {
    load();
    return 1 + Math.round((_inlineMax - _inlineMin) / Math.abs(_inlineRange.getDelta()));
  }

  /**
   * Returns the number of xlines contained in the AOI.
   * <p>
   * This takes into account the possible decimation of xlines.
   * 
   * @return the number of xlines in the AOI.
   */
  public int getNumXlines() {
    load();
    return 1 + Math.round((_xlineMax - _xlineMin) / Math.abs(_xlineRange.getDelta()));
  }

  /**
   * Gets the inline range (start,end,delta) of the AOI.
   * 
   * @return the inline range of the AOI.
   */
  public FloatRange getInlineRange() {
    load();
    return _inlineRange;
  }

  /**
   * Gets the starting inline of the AOI.
   * 
   * @return the starting inline of the AOI.
   */
  public float getInlineStart() {
    load();
    return _inlineRange.getStart();
  }

  /**
   * Gets the ending inline of the AOI.
   * 
   * @return the ending inline of the AOI.
   */
  public float getInlineEnd() {
    load();
    return _inlineRange.getEnd();
  }

  /**
   * Gets the delta inline of the AOI.
   * 
   * @return the delta inline of the AOI.
   */
  public float getInlineDelta() {
    load();
    return _inlineRange.getDelta();
  }

  /**
   * Gets the xline range (start,end,delta) of the AOI.
   * 
   * @return the xline range of the AOI.
   */
  public FloatRange getXlineRange() {
    load();
    return _xlineRange;
  }

  /**
   * Gets the starting xline of the AOI.
   * 
   * @return the starting xline of the AOI.
   */
  public float getXlineStart() {
    load();
    return _xlineRange.getStart();
  }

  /**
   * Gets the ending xline of the AOI.
   * 
   * @return the ending xline of the AOI.
   */
  public float getXlineEnd() {
    load();
    return _xlineRange.getEnd();
  }

  /**
   * Gets the xline delta of the AOI.
   * 
   * @return the xline delta of the AOI.
   */
  public float getXlineDelta() {
    load();
    return _xlineRange.getDelta();
  }

  /**
   * Sets the inline and xline ranges (start,end,delta) for the AOI.
   * 
   * @param survey the survey on which the AOI is defined.
   * @param inlineRange the inline range.
   * @param xlineRange the xline range.
   */
  public void setRanges(final SeismicSurvey3d survey, final FloatRange inlineRange, final FloatRange xlineRange) {
    _survey = survey;
    _inlineRange = inlineRange;
    _xlineRange = xlineRange;
    _isDecimated = !MathUtil.isEqual(_inlineRange.getDelta(), _survey.getInlineDelta())
        || !MathUtil.isEqual(_xlineRange.getDelta(), _survey.getXlineDelta());
    calculateInlineXlineMinMax();
    calculateSpatialExtent();
    setDirty(true);
  }

  /**
   * Returns a flag indicating if the AOI is decimated, meaning that inlines and/or xlines are skipped.
   * 
   * @return <i>true</i> if inlines and/or xlines are decimated; <i>false</i> if not.
   */
  public boolean isDecimated() {
    load();
    return _isDecimated;
  }

}
