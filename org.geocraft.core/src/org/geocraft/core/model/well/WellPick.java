/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.core.model.well;


import java.sql.Timestamp;

import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.datatypes.Point3d;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.InMemoryMapper;


/**
 * Defines a pick found in a well bore.
 */
public class WellPick extends Entity {

  /** The well that contains the pick. */
  private final Well _well;

  /** The level of confidence in the pick. */
  private String _confidenceFactor;

  /**
   * The angle (in degrees) the stratigraphic formation makes with horizontal at the pick's location.
   * TODO: 360 angle measured with respect to what?
   */
  private float _dipAngle;

  /**
   * The angle (in degrees) indicating the direction in which the surface is dipping.
   * TODO: 360 what is convention?
   */
  private float _dipAzimuth;

  /** The name of the interpreter that created the pick. */
  private String _interpreter;

  /** 
   * The observation number assigned to make the pick unique
   * if there are multiple cuts for the same surface.
   */
  private int _occurrence;

  /** The type of the qualified pick. */
  private String _qualifier;

  /** The RGB representation of the pick color. */
  private RGB _pickColor = new RGB(255, 0, 0);

  /** The creation date of the pick. */
  private Timestamp _pickDate;

  /** The source of the pick. TODO 360 explain */
  private String _pickSource;

  /** The type of the pick. Can be Strat_Marker, Fault_Marker, or Fluid_Marker. TODO 360 enum? */
  private String _pickType;

  /** The depth value of the pick (positive down). */
  private float _pickDepth;

  /** The domain of the pick depth(e.g. TVD, TVDSS, etc). */
  private WellDomain _pickDomain;

  public WellPick(final String name, final Well well) {
    this(name, new InMemoryMapper(WellPick.class), well);
  }

  /**
   * Parameterized constructor
   *
   * @param name the name of the pick.
   * @param wellBore the well bore that contains the pick.
   * @param measuredDepth the measured depth value of the pick.
   */
  public WellPick(final String name, final IMapper mapper, final Well well) {
    super(name, mapper);
    _well = well;
  }

  /**
   * Returns the well bore that contains the pick.
   */
  public final Well getWell() {
    load();
    return _well;
  }

  /**
   * Returns the level of confidence in the pick.
   */
  public final String getConfidenceFactor() {
    load();
    return _confidenceFactor;
  }

  /**
   * Returns the angle the stratigraphic formation makes with horizontal
   * at the pick's location.
   */
  public final float getDipAngle() {
    load();
    return _dipAngle;
  }

  /**
   * Returns the angle indicating the direction in which the surface is dipping.
   */
  public final float getDipAzimuth() {
    load();
    return _dipAzimuth;
  }

  /**
   * Returns the name of the interpreter that created the pick.
   */
  public final String getInterpreter() {
    load();
    return _interpreter;
  }

  /**
   * Returns the x,y,z coordinate of the pick for the given domain.
   */
  public final Point3d getLocation(final WellDomain domain) {
    load();
    float measuredDepth = getMeasuredDepth();
    return _well.getWellBore().getLocationFromMeasuredDepth(measuredDepth, domain);
  }

  /**
   * Returns the observation number assigned to make the pick unique
   * if there are multiple cuts for the same surface.
   */
  public final int getOccurrence() {
    load();
    return _occurrence;
  }

  /**
   * Returns the display color (RGB) of the pick.
   */
  public final RGB getPickColor() {
    load();
    return _pickColor;
  }

  /**
   * Returns the creation date of the pick.
   */
  public final Timestamp getPickDate() {
    load();
    return _pickDate;
  }

  /**
   * Returns the source of the pick.
   */
  public final String getPickSource() {
    load();
    return _pickSource;
  }

  /**
   * Returns the type of the pick.
   * e.g. Strat_Marker, Fault_Marker, or Fluid_Marker.
   */
  public final String getPickType() {
    load();
    return _pickType;
  }

  /**
   * Returns the pick value for the given domain (MD, TVD, etc).
   * 
   * @param domain the domain in which to get the pick value.
   * @return the pick value.
   */
  public final float getValue(final WellDomain domain) {
    load();
    switch (domain) {
      case MEASURED_DEPTH:
        return getMeasuredDepth();
      case ONE_WAY_TIME:
        return getTwoWayTime() / 2;
      case TRUE_VERTICAL_DEPTH:
        return getTrueVerticalDepth();
      case TRUE_VERTICAL_DEPTH_SUBSEA:
        return getTrueVerticalDepthSubsea();
      case TWO_WAY_TIME:
        return getTwoWayTime();
      default:
        throw new IllegalArgumentException("Invalid domain: " + domain);
    }
  }

  /**
   * Returns the measured depth (MD) value of the pick.
   */
  protected float getMeasuredDepth() {
    load();
    return _well.getWellBore().getMeasuredDepth(_pickDepth, _pickDomain);
  }

  /**
   * Returns the true-vertical-depth (TVD) value of the pick.
   */
  protected float getTrueVerticalDepth() {
    load();
    float measuredDepth = _well.getWellBore().getMeasuredDepth(_pickDepth, _pickDomain);
    float zTVD = Float.NaN;
    try {
      Point3d pickLoc = _well.getWellBore().getLocationFromMeasuredDepth(measuredDepth, WellDomain.TRUE_VERTICAL_DEPTH);
      if (pickLoc != null) {
        zTVD = (float) pickLoc.getZ();
      }
    } catch (Exception ex) {
      throw new RuntimeException(ex.getMessage());
    }
    return zTVD;
  }

  /**
   * Returns the true-vertical-depth (sub-sea) value of the pick.
   */
  protected float getTrueVerticalDepthSubsea() {
    load();
    float measuredDepth = _well.getWellBore().getMeasuredDepth(_pickDepth, _pickDomain);
    float zTVDSS = Float.NaN;
    try {
      Point3d pickLoc = _well.getWellBore().getLocationFromMeasuredDepth(measuredDepth,
          WellDomain.TRUE_VERTICAL_DEPTH_SUBSEA);
      if (pickLoc != null) {
        zTVDSS = (float) pickLoc.getZ();
      }
    } catch (Exception ex) {
      throw new RuntimeException(ex.getMessage());
    }
    return zTVDSS;
  }

  /**
   * Returns the two-way-time (TWT) value of the pick.
   */
  protected float getTwoWayTime() {
    load();
    float measuredDepth = _well.getWellBore().getMeasuredDepth(_pickDepth, _pickDomain);
    float zTWT = Float.NaN;
    try {
      zTWT = _well.getWellBore().getTwoWayTimeFromMeasuredDepth(measuredDepth);
    } catch (Exception ex) {
      throw new RuntimeException(ex.getMessage());
    }
    return zTWT;
  }

  /**
   * Returns the qualifier of the pick.
   */
  public final String getQualifier() {
    load();
    return _qualifier;
  }

  /**
   * Sets the level of confidence in the pick.
   *
   * @param confidenceFactor the confidence factor.
   */
  public final void setConfidenceFactor(final String confidenceFactor) {
    _confidenceFactor = confidenceFactor;
    setDirty(true);
  }

  /**
   * Sets the angle (in degrees) the stratigraphic formation makes with horizontal
   * at the pick's location.
   *
   * @param dipAngle the dip angle of the surface.
   */
  public final void setDipAngle(final float dipAngle) {
    _dipAngle = dipAngle;
    setDirty(true);
  }

  /**
   * Sets the angle (in degrees) indicating the direction in which the surface is dipping.
   *
   * @param dipAzimuth the dip azimuth of the surface.
   */
  public final void setDipAzimuth(final float dipAzimuth) {
    _dipAzimuth = dipAzimuth;
    setDirty(true);
  }

  /**
   * Sets the name of the interpreter that created the pick.
   *
   * @param interpreter the interpreter.
   */
  public final void setInterpreter(final String interpreter) {
    _interpreter = interpreter;
    setDirty(true);
  }

  /**
   * Sets the observation number assigned to make the pick unique
   * if there are multiple cuts for the same surface.
   *
   * @param occurrence the occurrence.
   */
  public final void setOccurrence(final int occurrence) {
    _occurrence = occurrence;
    setDirty(true);
  }

  /**
   * Sets the display color (RGB) of the pick.
   *
   * @param pickColor the display color (RGB).
   */
  public final void setPickColor(final RGB pickColor) {
    _pickColor = pickColor;
    setDirty(true);
  }

  /**
   * Sets the creation date of the pick.
   *
   * @param pickDate the creation date.
   */
  public final void setPickDate(final Timestamp pickDate) {
    _pickDate = pickDate;
    setDirty(true);
  }

  /**
   * Sets the source of the pick.
   *
   * @param pickSource the source.
   */
  public final void setPickSource(final String pickSource) {
    _pickSource = pickSource;
    setDirty(true);
  }

  /**
   * Sets the qualifier of the pick.
   *
   * @param qualifier the qualifier.
   */
  public final void setQualifier(final String qualifier) {
    _qualifier = qualifier;
    setDirty(true);
  }

  /**
   * Sets the type of the pick.
   * e.g. Strat_Marker, Fault_Marker, or Fluid_Marker.
   *
   * @param pickType the type of the.
   */
  public final void setPickType(final String pickType) {
    _pickType = pickType;
    setDirty(true);
  }

  /**
   * Sets the depth and domain (TVD, TVDSS, etc) of the pick.
   * Note: All depths are positive down.
   * 
   * @param depth the depth of the pick.
   * @param domain the domain of the pick.
   */
  public final void setPickDepth(final float depth, final WellDomain domain) {
    _pickDepth = depth;
    _pickDomain = domain;
  }

  public final float getPickDepth() {
    return _pickDepth;
  }

  public final WellDomain getPickDomain() {
    return _pickDomain;
  }
}
