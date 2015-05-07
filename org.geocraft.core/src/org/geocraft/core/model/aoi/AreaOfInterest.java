/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */

package org.geocraft.core.model.aoi;


import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.SpatialExtent;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.datatypes.ZDomain;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.seismic.SeismicSurvey2d;


/**
 * This class defines an abstract base class for area-of-interest (AOI) entities.
 */
public abstract class AreaOfInterest extends RegionOfInterest {

  /** The optional z-range for the AOI. This is a temporary design feature until the model can be refined. */
  private ZRangeConstant _zRange = null;

  /**
   * The default area-of-interest (AOI) constructor.
   * 
   * @param name the name of the AOI.
   * @param mapper the mapper to the underlying datastore.
   */
  public AreaOfInterest(final String name, final IMapper mapper) {
    super(name, mapper);
  }

  /**
   * Gets the spatial extent of the AOI.
   * 
   * @return the spatial extent of the AOI.
   */
  public abstract SpatialExtent getExtent();

  /**
   * Returns true if the specified x,y coordinate is inside the area-of-interest (AOI).
   * 
   * @param x the x coordinate.
   * @param y the y coordinate.
   * @return <i>true</i> if the specified x,y coordinate is inside the AOI; </i>false</i> if not.
   */
  public abstract boolean contains(double x, double y);

  /**
   * Returns true if the specified x,y coordinate is inside the area-of-interest (AOI).
   * <p>
   * The given survey is used for coordinate transforms between to/from x,y space.
   * 
   * @param x the x coordinate.
   * @param y the y coordinate.
   * @param survey the 2D seismic survey.
   * @return <i>true</i> if the specified x,y coordinate is inside the AOI; </i>false</i> if not.
   */
  public abstract boolean contains(double x, double y, SeismicSurvey2d survey);

  @Override
  public final boolean contains(final double x, final double y, final double z, final Unit zUnit) {
    load();

    // If a z-range is not defined, then simply return false.
    if (_zRange == null) {
      return false;
    }
    // Otherwise, first check if the AOI contains the x,y coordinate.
    if (contains(x, y)) {
      // If so, then get the start and end values of the z-range.
      float[] zs = _zRange.getZStartAndEnd(x, y);
      // Check if the z coordinate is within the z-range.
      if (zs.length == 2) {
        return z >= zs[0] && z <= zs[1];
      }
    }
    return false;
  }

  @Override
  public final float[] getZStartAndEnd(final double x, final double y) {
    load();

    if (_zRange != null && contains(x, y)) {
      return _zRange.getZStartAndEnd(x, y);
    }
    return new float[0];
  }

  /**
   * Sets the optional z-range for the AOI.
   * <p>
   * The z-range can be unset by calling <code>unsetZRange</code>.
   * 
   * @param zStart the starting z.
   * @param zEnd the ending z.
   * @param zDomain the z domain.
   */
  protected final void setZRange(final float zStart, final float zEnd, final ZDomain zDomain) {
    _zRange = new ZRangeConstant(zStart, zEnd, zDomain);
  }

  /**
   * Sets the optional z-range for the AOI.
   * <p>
   * The z-range can be unset by calling <code>unsetZRange</code>.
   * 
   * @param zStart the starting z.
   * @param zEnd the ending z.
   * @param zUnit the z unit.
   * @throws IllegalArgumentException thrown if the z unit is not in the time or depth domain.
   */
  public void setZRange(final float zStart, final float zEnd, final Unit zUnit) {
    Domain zDomain = zUnit.getDomain();
    if (zDomain == Domain.TIME) {
      Unit zUnitApp = UnitPreferences.getInstance().getTimeUnit();
      float zStartCnv = Unit.convert(zStart, zUnit, zUnitApp);
      float zEndCnv = Unit.convert(zEnd, zUnit, zUnitApp);
      setZRange(zStartCnv, zEndCnv, ZDomain.TIME);
    } else if (zDomain == Domain.DISTANCE) {
      Unit zUnitApp = UnitPreferences.getInstance().getVerticalDistanceUnit();
      float zStartCnv = Unit.convert(zStart, zUnit, zUnitApp);
      float zEndCnv = Unit.convert(zEnd, zUnit, zUnitApp);
      setZRange(zStartCnv, zEndCnv, ZDomain.DEPTH);
    } else {
      throw new IllegalArgumentException("Z unit must be in time or depth (distance) domain.");
    }
  }

  /**
   * Unsets the optional z-range for the AOI.
   * <p>
   * The z-range can be set by calling <code>setZRange</code>.
   */
  public final void unsetZRange() {
    _zRange = null;
  }

  public final ZRangeConstant getZRange() {
    load();
    return _zRange;
  }

  /**
   * Returns a flag indicating if the AOI has an associated z-range.
   * 
   * @return <i>true</i> if a z-range exists; <i>false</i> if not.
   * @return
   */
  public final boolean hasZRange() {
    load();
    return _zRange != null;
  }
}
