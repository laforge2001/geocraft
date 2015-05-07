/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.aoi;


import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.datatypes.ZDomain;
import org.geocraft.core.model.mapper.InMemoryMapper;
import org.geocraft.core.model.preferences.UnitPreferences;


/**
 * This class defines a simple region-of-interest (ROI), based on an area-of-interest (AOI) and
 * a z range (start,end).
 * <p>
 * The AOI is used to define the x,y bounds, and the z range is used to filter in the z direction.
 */
public final class SimpleROI extends RegionOfInterest {

  /** The area-of-interest portion of the ROI. */
  private final AreaOfInterest _aoi;

  /** The z-range of the ROI. */
  private ZRange _zRange;

  /**
   * Constructs a simple ROI based on an AOI and a z range.
   * <p>
   * This filters x,y coordinates using the AOI, and filters z coordinates using the z range.
   * 
   * @param name the name of the ROI.
   * @param aoi the AOI to use for x,y coordinates.
   * @param zStart the start of the z range.
   * @param zEnd the end of the z range.
   * @param zDomain the domain of the z range.
   */
  public SimpleROI(final String name, final AreaOfInterest aoi, final float zStart, final float zEnd, final ZDomain zDomain) {
    super(name, new InMemoryMapper(SimpleROI.class));
    // Validate the z domain is non-null and not UNDEFINED.
    if (zDomain == null) {
      throw new RuntimeException("Invalid z domain: " + zDomain);
    }
    _aoi = aoi;
    _zRange = new ZRangeConstant(zStart, zEnd, zDomain);
  }

  /**
   * Constructs a simple ROI based on an AOI and a z range.
   * <p>
   * This filters x,y coordinates using the AOI, and filters z coordinates using the z range.
   * 
   * @param name the name of the ROI.
   * @param aoi the AOI to use for x,y coordinates.
   * @param zRange the z range.
   */
  public SimpleROI(final String name, final AreaOfInterest aoi, final ZRange zRange) {
    super(name, new InMemoryMapper(SimpleROI.class));
    // Validate the z-range is non-null.
    if (zRange == null) {
      throw new IllegalArgumentException("Invalid z range: " + zRange);
    }
    _aoi = aoi;
    _zRange = zRange;
  }

  @Override
  public float[] getZStartAndEnd(final double x, final double y) {
    // Check if the x,y coordinates fall within the AOI portion of the ROI.
    if (_aoi == null || (_aoi != null && _aoi.contains(x, y))) {
      // If the z range is undefined, return an empty result.
      if (_zRange == null) {
        return new float[0];
      }
      // If so, then get the starting and ending z values from the z range.
      return _zRange.getZStartAndEnd(x, y);
    }
    // Otherwise, return an empty result.
    return new float[0];
  }

  @Override
  public boolean contains(final double x, final double y, final double z, final Unit zUnit) {
    // If the z range is undefined, return false.
    if (_zRange == null) {
      return false;
    }

    // Validate the specified z unit is in the same domain as the ROI.
    ZDomain zDomain = ZDomain.getFromDomain(zUnit.getDomain());
    ZDomain zDomainROI = _zRange.getDomain();
    if (zDomain == null || zDomain != zDomainROI) {
      throw new RuntimeException("Incompatible z domain: " + zDomain);
    }
    // Get the starting and ending z values for the x,y coordinates.
    float[] zValues = _zRange.getZStartAndEnd(x, y);
    // If zero-length, then simply return false.
    if (zValues.length == 0) {
      return false;
    }

    // Check if the x,y coordinates fall within the AOI portion of the ROI.
    if (_aoi == null || (_aoi != null && _aoi.contains(x, y))) {
      // Otherwise, convert the z value to the same unit of measurement as the ROI.
      Unit zUnitOfROI = UnitPreferences.getInstance().getTimeUnit();
      if (zDomainROI == ZDomain.DEPTH) {
        zUnitOfROI = UnitPreferences.getInstance().getVerticalDistanceUnit();
      }
      double zConverted = Unit.convert(z, zUnit, zUnitOfROI);
      float zStart = zValues[0];
      float zEnd = zValues[1];

      // Check that the converted z coordinate falls within the z range of the ROI.
      return zConverted >= zStart && z <= zEnd;
    }
    // The AOI check failed, so return false.
    return false;
  }

  /**
   * Returns the internal area-of-interest (AOI) on which this ROI is based.
   * 
   * @return the internal AOI.
   */
  public AreaOfInterest getAOI() {
    return _aoi;
  }

  public ZRange getZRange() {
    load();
    return _zRange;
  }

  public void setZRange(final ZRange zRange) {
    _zRange = zRange;
    setDirty(true);
  }

}
