/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.aoi;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.SpatialExtent;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.InMemoryMapper;
import org.geocraft.core.model.seismic.SeismicLine2d;
import org.geocraft.core.model.seismic.SeismicSurvey2d;


/**
 * This class represents an area-of-interest (AOI) defined on a 2D seismic survey.
 * <p>
 * This 2D AOI consists of a collection of shotpoint ranges (start,end,delta) for a set of lines in a 2D survey.
 * Each shotpoint range is mapped by the name of the seismic line on which it is defined.
 * This AOI can be used to define a subset of data on which to operate.
 */
public class SeismicSurvey2dAOI extends AreaOfInterest {

  /** The epsilon used for validity checking. */
  private static final float EPSILON = 0.01f;

  /** The collection of CDP ranges, mapped by the line on which they are defined. */
  private final Map<String, FloatRange> _cdpRanges;

  /**
   * Constructs an AOI defined on a 2D seismic survey.
   * <p>
   * The resulting AOI will be lazy-loaded from its underlying datastore when needed.
   */
  public SeismicSurvey2dAOI(final String displayName, final IMapper mapper) {
    super(displayName, mapper);
    _cdpRanges = Collections.synchronizedMap(new HashMap<String, FloatRange>());
  }

  /**
   * Constructs an in-memory AOI defined on a 2D seismic survey.
   * <p>
   * The resulting AOI is considered <i>loaded</i>.
   * 
   * @param displayName the name of the AOI.
   * @param mapper the mapper to the underlying datastore.
   * @param cdpRanges the collection of shotpoint ranges, mapped by line name.
   */
  public SeismicSurvey2dAOI(final String displayName, final Map<String, FloatRange> cdpRanges) {
    this(displayName, new InMemoryMapper(SeismicSurvey2dAOI.class), cdpRanges);
  }

  /**
   * Constructs an AOI defined on a 2D seismic survey.
   * <p>
   * The resulting AOI is considered <i>loaded</i>.
   * 
   * @param displayName the name of the AOI.
   * @param mapper the mapper to the underlying datastore.
   * @param cdpRanges the collection of shotpoint ranges, mapped by line name.
   */
  public SeismicSurvey2dAOI(final String displayName, final IMapper mapper, final Map<String, FloatRange> cdpRanges) {
    super(displayName, mapper);

    // Store a copy of the shotpoint ranges.
    _cdpRanges = Collections.synchronizedMap(new HashMap<String, FloatRange>());
    for (String lineName : cdpRanges.keySet()) {
      _cdpRanges.put(lineName, cdpRanges.get(lineName));
    }
    markLoaded();
  }

  /**
   * Returns a flag indicating of the given line/cdp coordinate is contained in the line/trace range.
   * 
   * @param lineNamethe line name.
   * @param cdp the CDP #.
   * @return <i>true</i> if contained; <i>false</i> of not.
   */
  public boolean contains(final int lineName, final float cdp) {
    load();

    // Check if a shotpoint range exists for the given line name.
    if (_cdpRanges.containsKey(lineName)) {
      // If so, check that the given shotpoint falls within the range.
      FloatRange cdpRange = _cdpRanges.get(lineName);
      float findex = (cdp - cdpRange.getStart()) / cdpRange.getDelta();
      int index = Math.round(findex);
      if (index >= 0 && index < cdpRange.getNumSteps() && Math.abs(findex - index) < EPSILON) {
        return true;
      }
    }

    return false;
  }

  /**
   * Gets the CDP range mapped by the given line name.
   * 
   * @param lineName the line name.
   * @return the CDP range; or <i>null</i> if none mapped to the line name.
   */
  public FloatRange getCdpRange(final String lineName) {
    load();
    return _cdpRanges.get(lineName);
  }

  /**
   * Gets a collection of the CDP ranges contained in the AOI.
   * <p>
   * Note: A defensive copy of the internal collection is returned.
   * 
   * @return the collection of CDP ranges, mapped by line name.
   */
  public Map<String, FloatRange> getCdpRanges() {
    load();
    Map<String, FloatRange> cdpRanges = Collections.synchronizedMap(new HashMap<String, FloatRange>());
    for (String lineName : _cdpRanges.keySet()) {
      cdpRanges.put(lineName, _cdpRanges.get(lineName));
    }
    return cdpRanges;
  }

  /**
   * Sets the collection of CDP ranges contained in the AOI.
   * <p>
   * Note: This completely replaces the contents of the internal collection, so any existing shotpoint ranges currently contained in the AOI will be lost.
   * 
   * @param cdpRanges the collection of CDP ranges, mapped by line name.
   */
  public void setCdpRanges(final Map<String, FloatRange> cdpRanges) {
    _cdpRanges.clear();
    for (String lineName : cdpRanges.keySet()) {
      _cdpRanges.put(lineName, cdpRanges.get(lineName));
    }
    setDirty(true);
  }

  @Override
  public boolean contains(final double x, final double y) {
    load();
    return false;
  }

  @Override
  public SpatialExtent getExtent() {
    load();
    // TODO Auto-generated method stub
    return null;
  }

  public boolean contains(final String lineName, final float shotpoint, final SeismicSurvey2d survey) {
    if (!survey.containsLine(lineName)) {
      return false;
    }
    if (!_cdpRanges.containsKey(lineName)) {
      return false;
    }
    SeismicLine2d seismicLine = survey.getLineByName(lineName);
    FloatRange cdpRange = _cdpRanges.get(lineName);
    float cdp = Math.round(seismicLine.transformShotpointToCDP(shotpoint));
    return cdpRange.contains(cdp);
  }

  @Override
  public boolean contains(final double x, final double y, final SeismicSurvey2d survey) {
    load();

    // Transform the x,y coordinate to line/CDP using the given survey.
    float[] lncdp = survey.transformXYToLineCDP(x, y);
    int lineNumber = Math.round(lncdp[0]);
    float cdp = lncdp[1];

    // Check if the AOI contains a shotpoint range for the line.
    SeismicLine2d seismicLine = survey.getLineByNumber(lineNumber);
    String lineName = seismicLine.getDisplayName();
    if (!_cdpRanges.containsKey(lineName)) {
      // If none found, then simply return false.
      return false;
    }

    // Check if the CDP falls within the CDP range for the line.
    FloatRange cdpRange = _cdpRanges.get(lineName);
    float indexF = (cdp - cdpRange.getStart()) / cdpRange.getDelta();
    int index = Math.round(indexF);
    float cdpCalc = cdpRange.getStart() + index * cdpRange.getDelta();
    if (index < 0 || index >= cdpRange.getNumSteps() || Math.abs(cdp - cdpCalc) > EPSILON) {
      return false;
    }
    return true;
  }

}
