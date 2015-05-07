/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.core.model.seismic;


import java.util.Arrays;

import org.geocraft.core.common.math.MathUtil;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.datatypes.CoordinateSeries;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.HeaderDefinition;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.ISeismicDatasetMapper;
import org.geocraft.core.model.preferences.UnitPreferences;


/**
 * This abstract class contains the attributes common to all seismic datasets
 * (2D and 3D, as well as PreStack and PostStack).
 */
public abstract class SeismicDataset extends Entity {

  /** Enumeration of the various storage format (32-bit float, 8-bit int, etc). */
  public enum StorageFormat {
    INTEGER_08("8-bit integer", "i8", "8i"),
    INTEGER_16("16-bit integer", "i16", "16i"),
    INTEGER_32("32-bit integer", "i32", "32i"),
    FLOAT_08("8-bit floating point", "f8", "8f"),
    FLOAT_16("16-bit floating point", "f16", "16f"),
    FLOAT_32("32-bit floating point", "f32", "32f");

    private String _text;

    private String[] _codes;

    StorageFormat(final String text, final String... codes) {
      _text = text;
      _codes = codes;
    }

    public String[] getCodes() {
      return Arrays.copyOf(_codes, _codes.length);
    }

    @Override
    public String toString() {
      return _text;
    }

    public static StorageFormat lookupByCode(final String code) {
      for (StorageFormat format : StorageFormat.values()) {
        for (String c : format.getCodes()) {
          if (c.equalsIgnoreCase(code)) {
            return format;
          }
        }
      }
      throw new IllegalArgumentException("Invalid storage format code: " + code);
    }
  }

  /** The epsilon value to use for float comparisons. */
  protected static final float EPSILON = 0.01f;

  // This is the get function that gets the domain
  public static final String DOMAIN = "domain";

  //This is the get function that gets the units
  public static final String DATA_UNIT = "dataUnit";

  //This is the get function that gets the domain of the Units
  public static final String UNIT_DOMAIN = "unitDomain";

  /**
   * Very similar to the Extent attribute except this is the bounding box where seismic data exists
   * in the dataset. Clients should always set the coordinate system when retrieving this attribute
   * so they know the exact coordinate system the points are returned in; otherwise, the points will
   * be returned in the default coordinate system and each vendor store might have a different
   * default. It is possible the dataset was created with a large extent but the dataset hasn't been
   * fully populated. This reflects the bounding box of existing traces. If the dataset is fully
   * populated, the Extent and ActualExtent columns will return identical results. The ActualExtent
   * and Extent will be identical if the vendor store doesn't have an easy way of identifying the
   * actual extent without reading all the seismic traces. In other words, the ActualExtent is not
   * guaranteed to be the minimum bounding box of the seismic data.
   */
  private CoordinateSeries _actualExtent;

  /**
   * The z-domain of this seismic dataset.
   * At present there are only 2 domain options: TIME and DEPTH.
   */
  private Domain _zDomain;

  /**
   * The trace header definition describes the header elements contained in the traces
   * returned from this seismic dataset.
   */
  private HeaderDefinition _traceHeaderDefinition;

  /**
   * this seismic dataset's bounding box. Clients should always set the coordinate system when
   * retrieving this attribute so they know the exact coordinate system the points are returned in;
   * otherwise, the points will be returned in the default coordinate system and each vendor store
   * might have a different default. The coordinate system has a big impact on the Z values. The Z
   * values are unit converted to match the specified coordinate system's third axis. Must pass in a
   * time-based coordinate system (third axis has a time Z unit) when this seismic dataset has a
   * DomainType of TIME and a depth-based coordinate system when the DomainType is DEPTH so the Z
   * unit can be successfully converted. The extent's Z range will not match the MinZ and MaxZ
   * attributes if the specified coordinate system's third axis is not the same as UnitsZ (e.g. one
   * could be milliseconds and the other seconds). All time-based coordinate systems have Z as the
   * 2-way travel time relative to a seismic reference datum. The Z values are positive downward in
   * these systems. For the SeismicBinGrid time and depth coordinate systems, the Z values are
   * positive downward and are relative to the seismic datum. For the MapDepth coordinate system,
   * the Z values are the elevation with respect to the projection system's geodetic datum. The Z
   * values are negative downward below the datum. This is the maximum possible extent which the
   * dataset is constrained (e.g. the dataset can not grow beyond this extent). Extent is returned
   * in the form of an array of points (total 8) where first four points lie in minimum (start)
   * time/depth plane and last four points lie in maximum (ending) time/depth plane. The points form
   * a rectangular solid bounding box. The points are first and last inclusive (for example, if the
   * volume was 1x1x1 then all eight points would be identical). For PostStack3d entities: The first
   * point in the sequence identifies the volume's origin. All other points are in relation to this
   * origin. The points are returned in the following order: 
   * (first Inline, first Crossline, minimum Z) - volume origin, 
   * (last Inline, first Crossline, minimum Z), 
   * (last Inline, last Crossline, minimum Z), 
   * (first Inline, last Crossline, minimum Z), 
   * (first Inline, first Crossline, maximum Z), 
   * (last Inline, first Crossline, maximum Z), 
   * (last Inline, last Crossline, maximum Z), 
   * (first Inline, last Crossline, maximum Z).
   */
  private CoordinateSeries _extent;

  /** The z-range (start, end, delta) of this seismic dataset. */
  protected FloatRange _zRange;

  /** The maximum possible z-range (start, end, delta) of this seismic dataset. */
  protected FloatRange _zRangeBounds;

  /** The unit of measurement for the seismic data sample values. */
  private Unit _dataUnit;

  /**
   * The elevation datum giving meaning to time = zero for this seismic dataset. This datum is used
   * when displaying well data at the proper elevation with respect to the seismic data.
   */
  private float _elevationDatum;

  /**
   * The reference point for the seismic elevation datum. Some possible values are "Mean Sea Level",
   * "Ocean Floor", etc.
   */
  private String _elevationReference;

  /**
   * Constructor for a lazy-loaded seismic dataset.
   * 
   * @param displayName the display name of this seismic dataset.
   * @param mapper the mapper to the underlying datastore.
   */
  public SeismicDataset(final String displayName, final IMapper mapper) {
    super(displayName, mapper);

    // Default the data unit to seismic amplitude.
    _dataUnit = Unit.SEISMIC_AMPLITUDE;
  }

  /**
   * Returns the bounding box of the maximum extents of this seismic dataset.
   * <p>
   * This will be greater than or equal to the actual extents of the dataset,
   * depending on whether the dataset is fully populated.
   * 
   * @return the bounding box of the maximum extents.
   */
  public final CoordinateSeries getExtent() {
    load();
    return _extent;
  }

  /**
   * Returns the bounding box of the actual extents of this seismic dataset.
   * <p>
   * This will be less than or equal to the maximum extents of the dataset,
   * depending on whether the dataset is fully populated.
   * 
   * @return the bounding box of the actual extents.
   */
  public final CoordinateSeries getActualExtent() {
    load();
    return _actualExtent;
  }

  /**
   * Returns the z domain of this seismic dataset.
   * <p>
   * This will usually be <code>TIME</code> or <code>DEPTH</code>.
   * 
   * @return the z domain.
   */
  public final Domain getZDomain() {
    load();
    return _zDomain;
  }

  /**
   * Returns the z unit of measurement for seismic dataset.
   * 
   * @return the z unit of measurement.
   */
  public final Unit getZUnit() {
    load();
    if (_zDomain.equals(Domain.TIME)) {
      return UnitPreferences.getInstance().getTimeUnit();
    } else if (_zDomain.equals(Domain.DISTANCE)) {
      return UnitPreferences.getInstance().getVerticalDistanceUnit();
    }
    throw new RuntimeException("Invalid domain (" + _zDomain + ").");
  }

  /**
   * Returns the z range (start,end,delta) for this seismic dataset.
   * 
   * @return the z range.
   */
  public final FloatRange getZRange() {
    load();
    return _zRange;
  }

  /**
   * Returns the maximum possible z range (start,end,delta) for this seismic dataset.
   * 
   * @return the z range.
   */
  public final FloatRange getZRangeBounds() {
    load();
    return _zRangeBounds;
  }

  /**
   * Returns the starting z value for the traces in this seismic dataset.
   * <p>
   * The starting z value is relative to the seismic reference datum, not the elevation datum (e.g. sea level),
   * so the z attribute will be positive. This starting z is the same for all traces in the dataset.
   * 
   * @return the starting z value.
   */
  public final float getZStart() {
    load();
    return _zRange.getStart();
  }

  /**
   * Returns the maximum possible starting z value for the traces in this seismic dataset.
   * <p>
   * The starting z value is relative to the seismic reference datum, not the elevation datum (e.g. sea level),
   * so the z attribute will be positive. This starting z is the same for all traces in the dataset.
   * 
   * @return the starting z value.
   */
  public final float getZMaxStart() {
    load();
    return _zRangeBounds.getStart();
  }

  /**
   * Returns the ending z value for the traces in this seismic dataset.
   * <p>
   * The ending z value is relative to the seismic reference datum, not the elevation datum (e.g. sea level),
   * so the z attribute will be positive. The ending z is the same for all traces in the dataset.
   * 
   * @return the ending z value.
   */
  public final float getZEnd() {
    load();
    return _zRange.getEnd();
  }

  /**
   * Returns the maximum possible ending z value for the traces in this seismic dataset.
   * <p>
   * The ending z value is relative to the seismic reference datum, not the elevation datum (e.g. sea level),
   * so the z attribute will be positive. The ending z is the same for all traces in the dataset.
   * 
   * @return the ending z value.
   */
  public final float getZMaxEnd() {
    load();
    return _zRangeBounds.getEnd();
  }

  /**
   * Returns the delta z value (i.e. sample rate) for the traces in this seismic dataset.
   * 
   * @return the delta z value (i.e. sample rate).
   */
  public final float getZDelta() {
    load();
    return _zRange.getDelta();
  }

  /**
   * Returns the maximum possible delta z value (i.e. sample rate) for the traces in this seismic dataset.
   * 
   * @return the delta z value (i.e. sample rate).
   */
  public final float getZMaxDelta() {
    load();
    return _zRangeBounds.getDelta();
  }

  /**
   * Returns the number of samples per trace in this seismic dataset.
   * <p>
   * The definition of a trace is independent of the volume's storage organization (i.e. TRACE, SLICE, BRICK).
   * It is related to the how the seismic was acquired which is in the vertical orientation.
   * 
   * @return the number of samples per traces.
   */
  public final int getNumSamplesPerTrace() {
    load();
    return _zRange.getNumSteps();
  }

  /**
   * Gets the elevation datum (i.e. the elevation where time = zero) of this seismic dataset.
   * <p>
   * This datum is used when displaying well data at the proper elevation with respect to the seismic data.
   * 
   * @return the seismic elevation datum.
   */
  public final float getElevationDatum() {
    load();
    return _elevationDatum;
  }

  /**
   * Sets the elevation datum (i.e. the elevation where time = zero) for this seismic dataset.
   * <p>
   * This datum is used when displaying well data at the proper elevation with respect to the seismic data.
   * 
   * @param elevationDatum the seismic elevation datum.
   */
  public final void setElevationDatum(final float elevationDatum) {
    _elevationDatum = elevationDatum;
    setDirty(true);
  }

  /**
   * Gets the elevation reference for the seismic elevation datum.
   * <p>
   * Some possible values are "Mean Sea Level", "Ocean Floor", etc.
   * 
   * @return the seismic elevation reference.
   */
  public final String getElevationReference() {
    load();
    return _elevationReference;
  }

  /**
   * Sets the elevation reference for this seismic dataset.
   * <p>
   * Some possible values are "Mean Sea Level", "Ocean Floor", etc.
   * 
   * @param elevationReference the seismic elevation reference.
   */
  public final synchronized void setElevationReferences(final String elevationReference) {
    _elevationReference = elevationReference;
    setDirty(true);
  }

  /**
   * Very similar to the Extent attribute except this is the bounding box where seismic data exists
   * in the dataset. Clients should always set the coordinate system when retrieving this attribute
   * so they know the exact coordinate system the points are returned in; otherwise, the points will
   * be returned in the default coordinate system and each vendor store might have a different
   * default. It is possible the dataset was created with a large extent but the dataset hasn't been
   * fully populated. This reflects the bounding box of existing traces. If the dataset is fully
   * populated, the Extent and ActualExtent columns will return identical results. The ActualExtent
   * and Extent will be identical if the vendor store doesn't have an easy way of identifying the
   * actual extent without reading all the seismic traces. In other words, the ActualExtent is not
   * guaranteed to be the minimum bounding box of the seismic data.
   * @param actualExtent
   */
  // TODO:  actual extent should be calculated, not set.
  public final synchronized void setActualExtent(final CoordinateSeries actualExtent) {
    _actualExtent = actualExtent;
    setDirty(true);
  }

  /**
   * Indicates if the seismic data is sampled in time or depth.
   * @param domainType
   */
  // TODO:  cannot be changed once the SeismicDataset is created.
  public final synchronized void setZDomain(final Domain zDomain) {
    _zDomain = zDomain;
    setDirty(true);
  }

  /**
   * this seismic dataset's bounding box. 
   * 
   * @param extent
   */
  // TODO:  cannot be changed once the SeismicDataset is created.
  public final synchronized void setExtent(final CoordinateSeries extent) {
    _extent = extent;
    setDirty(true);
  }

  /**
   * Sets the z range (start,end,delta) of this seismic dataset.
   * <p>
   * The z range values are assumed to be in the units of the application
   * preferences for the z domain.
   * 
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   * @param zDelta the delta z value (i.e. sample rate).
   */
  public final synchronized void setZRangeAndDelta(final float zStart, final float zEnd, final float zDelta) {
    validateZRange(zStart, zEnd, zDelta);
    _zRange = new FloatRange(zStart, zEnd, zDelta);
    setDirty(true);
  }

  /**
   * Sets the maximum possible z range (start,end,delta) of this seismic dataset.
   * <p>
   * The z range values are assumed to be in the units of the application
   * preferences for the z domain.
   * 
   * @param zStart the starting z value.
   * @param zEnd the ending z value.
   * @param zDelta the delta z value (i.e. sample rate).
   */
  public final synchronized void setZMaxRangeAndDelta(final float zStart, final float zEnd, final float zDelta) {
    validateZRange(zStart, zEnd, zDelta);
    _zRangeBounds = new FloatRange(zStart, zEnd, zDelta);
    setDirty(true);
  }

  /**
   * Gets the unit of measurement of the trace data in this seismic dataset.
   * 
   * @return the trace data unit of measurement.
   */
  public Unit getDataUnit() {
    load();
    return _dataUnit;
  }

  /**
   * Sets the unit of measurement of the trace data in this seismic dataset.
   * 
   * @param dataUnit the trace data unit of measurement.
   */
  public synchronized void setDataUnit(final Unit dataUnit) {
    _dataUnit = dataUnit;
  }

  public StorageFormat getStorageFormat() {
    load();
    return ((ISeismicDatasetMapper) _mapper).getStorageFormat();
  }

  /**
   * Returns the domain of the trace data this seismic dataset.
   * 
   * @return the trace data domain.
   */
  public Domain getDataDomain() {
    load();
    return _dataUnit.getDomain();
  }

  /**
   * Gets the trace header definition for this seismic dataset.
   * <p>
   * The trace header definition describes the header elements contained in the traces
   * returned from this seismic dataset.
   * 
   * @return the trace header definition.
   */
  public final HeaderDefinition getTraceHeaderDefinition() {
    load();
    return _traceHeaderDefinition;
  }

  /**
   * Sets the trace header definition for this seismic dataset.
   * <p>
   * The trace header definition describes the header elements contained in the traces
   * returned from this seismic dataset. The trace header definition is generally set
   * by the mapper when the entity refreshes itself from the datastore.
   * 
   * @param traceHeaderDefinition the trace header definition.
   */
  public final synchronized void setTraceHeaderDefinition(final HeaderDefinition traceHeaderDefinition) {
    _traceHeaderDefinition = traceHeaderDefinition;
    setDirty(true);
  }

  /**
   * Closes this seismic dataset file in the underlying datastore.
   * <p>
   * This method must be called at the end of a task to ensure that
   * the volume is closed property. Failure to do so could result
   * in "corrupt" files, depending on the datastore.
   */
  public abstract void close();

  /**
   * Validates the specified z value.
   * <p>
   * The value is checked to make sure it is a multiple of the z delta.
   * It is also be checked to make sure it falls within the start and end z bounds.
   * 
   * @param z the z to validate.
   * @param checkBounds <i>true<i> to check against the start and end bounds; otherwise <i>false<i>.
   */
  protected final void validateZ(final float z) {
    validateRange("z", z, _zRange.getStart(), _zRange.getEnd(), _zRange.getDelta(), true);
  }

  /**
   * Validates the specified z value.
   * <p>
   * The value is checked to make sure it is a multiple of the z delta.
   * It is also be checked to make sure it falls within the start and end z bounds.
   * 
   * @param z the z to validate.
   * @param checkBounds <i>true<i> to check against the start and end bounds; otherwise <i>false<i>.
   */
  protected final void validateZMax(final float z) {
    validateRange("z maximum", z, _zRangeBounds.getStart(), _zRangeBounds.getEnd(), _zRangeBounds.getDelta(), true);
  }

  /**
   * Validates the start, end and delta values of a z range.
   * <p>
   * This method checks the the end value is on an exact integer increment of the delta, relative to the start value.
   * 
   * @param start the start value.
   * @param end the end value.
   * @param delta the delta value.
   * @throws IllegalArgumentException thrown if the range values are inconsistent.
   */
  protected final void validateZRange(final float start, final float end, final float delta) {
    int numSamples = 1 + Math.round((end - start) / delta);
    float remainder = end - start - (numSamples - 1) * delta;
    if (remainder > 0.00001f) {
      throw new IllegalArgumentException("Inconsistent start, end, delta values: " + start + " " + end + " " + delta);
    }
  }

  /**
   * Validates that the given value is within the specified range (start, end, delta)
   * and falls on the specified delta (increment).
   * <p>
   * @param name the name of the property being validated (e.g. "Inline", "Xline", etc).
   * @param value the value.
   * @param start the starting value of the range.
   * @param end the ending value of the range.
   * @param delta the delta (increment) value of the range.
   * @param checkBounds flag indicating if the the value is to be checked against the start/end bounds.
   * @throws IllegalArgumentException if the value fails the validation.
   */
  protected final void validateRange(final String name, final float value, final float start, final float end,
      final float delta, final boolean checkBounds) {
    if (Math.abs((value - start) % delta) > EPSILON && Math.abs((value - start) % delta) < Math.abs(delta) - EPSILON) {
      throw new IllegalArgumentException(name + " " + value + " is not a mutiple of " + delta + ". (" + name + "-"
          + name + "Start)%" + name + "Delta=" + (value - start) % delta + " not within " + EPSILON);
    }
    if (checkBounds) {
      if (start < end && (value < start || value > end)) {
        throw new IllegalArgumentException(name + " " + value + " is not between " + start + " and " + end);
      }
      if (start > end && (value > start || value < end)) {
        throw new IllegalArgumentException(name + " " + value + " is not between " + end + " and " + start);
      }
    }
  }
}
