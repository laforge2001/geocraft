/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.core.model.well;


import org.geocraft.core.common.util.Utilities;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.mapper.InMemoryMapper;


/**
 * The well checkshot class defines sets of time/depth values used to convert time-to-depth or
 * depth-to-time. For data increasing downward, if a given depth (time) value does not match an
 * exact data point in the time-depth class, linear interpolation will be used to acquire the proper
 * time (depth) value.
 * <p>
 * If a given depth (time) value is deeper than the deepest depth (time) in the time-depth class, a
 * constant interval velocity computed from the last two points in the time-depth class will be used
 * to extrapolate the time (depth) value.
 * <p>
 * If a given depth (time) value is shallower than the shallowest depth (time) in the time-depth
 * class, a constant interval velocity computed from the first two points in the time-depth class
 * will be used to extrapolate the time (depth) value.
 */
public class WellCheckShot extends Entity {

  /** The well that contains this check shot. */
  private final Well _well;

  /** Indicates if a check shot is measured. */
  private boolean _checkShot;

  /** The person, company or vendor that created the check shot. */
  private String _dataSource;

  /** 
   * The depth at which time is 0 in the check shot.
   * Both the depths and times are relative to this value.
   */
  private float _datum;

  /** The static shift value to apply to the time depth curve in display. */
  private float _displayShift;

  /**
   * The datum elevation with respect to MSL of the project seismic reference datum.
   * Note that this is NOT necessarily the datum of the check shot. If this differs from
   * the check shot datum, then one would typically need to shift the check shot TWTs by
   * (Datum-SeismicReferenceDatum)/VelocityToSeismicDatum in order to have the check shot
   * times referenced to the SeismicReferenceDatum.
   */
  private float _seismicReferenceDatum;

  /**
   * The true-vertical-depth (TVD) values of the check shot positive downward from the well datum.
   * The unit of measurement is the application vertical distance unit.
   */
  private float[] _zTVDs;

  /**
   * The two-way-time (TWT) values of the check shot.
   * The unit of measurement is the application time unit.
   */
  private float[] _zTWTs;

  /** The replacement velocity to use between the check shot datum and the seismic reference datum. */
  private float _velocityToSeismicDatum;

  public WellCheckShot(final String name, final Well well) {
    this(name, new InMemoryMapper(WellCheckShot.class), well);
  }

  public WellCheckShot(final String name, final IMapper mapper, final Well well) {
    super(name, mapper);
    _well = well;
    _zTVDs = new float[0];
    _zTWTs = new float[0];
  }

  /**
   * Calculates an array of true-vertical-depths (TVD) given an array of two-way-times (TWT).
   *
   * @param zTWTs the array of two-way-times.
   * @param tvdDatum the value for determining the two-way-time conversion factor.
   * @return the array of true-vertical-depths.
   */
  public float[] calculateTVDsFromTWTs(final float[] zTWTs, final float datumTVD) {
    load();

    // If any of the necessary arrays are undefined or zero-length, then simply return
    // a zero-length result.
    if (zTWTs == null || zTWTs.length == 0 || _zTVDs.length < 2 || _zTWTs.length < 2) {
      return new float[0];
    }

    // Allocate a TVD array and calculate each element.
    float[] zTVDs = new float[zTWTs.length];
    for (int i = 0; i < zTWTs.length; i++) {
      zTVDs[i] = caluclateTVDFromTWT(zTWTs[i], datumTVD);
    }
    return zTWTs;
  }

  /**
   * Calculates a true-vertical-depth (TVD) given a two-way-time (TWT).
   *
   * @param zTWT the two-way-time.
   * @param datumTVD the value for determining the two-way-time conversion factor.
   * @return the true-vertical-depth.
   */
  public float caluclateTVDFromTWT(final float zTWT, final float datumTVD) {
    load();

    // If the TWT and TVD arrays do not contain at least 2 points, then simply return NaN.
    if (_zTWTs.length < 2 || _zTVDs.length < 2) {
      return Float.NaN;
    }

    float zTVDconv = WellUtil.lookupValue(zTWT, _zTWTs, "TWT", _zTVDs, "TVD");

    // Adjust the TVD from the time datum.
    float datumConvFactor = _datum - datumTVD;
    return zTVDconv - datumConvFactor;
  }

  /**
   * Calculates an array of two-way-times (TWT) given an array of true-vertical-depths (TVD).
   *
   * @param zTVDs the array of true-vertical-depths.
   * @param datumTVD the value for determining the two-way-time conversion factor.
   * @return the array of two-way-times.
   */
  public float[] calculateTWTsFromTVDs(final float[] zTVDs, final float datumTVD) {
    load();

    // If any of the necessary arrays are undefined or zero-length, then simply return
    // a zero-length result.
    if (zTVDs == null || zTVDs.length == 0 || _zTVDs.length < 2 || _zTWTs.length < 2) {
      return new float[0];
    }

    // Allocate a TWT array and calculate each element.
    float[] twts = new float[zTVDs.length];
    for (int i = 0; i < twts.length; i++) {
      twts[i] = caluclateTWTFromTVD(zTVDs[i], datumTVD);
      //System.out.println("calcTWTsFromTVDs: " + i + " zTVD=" + zTVDs[i] + " twt=" + twts[i] + " datum=" + datumTVD);
    }
    return twts;
  }

  /**
   * Calculates a two-way-time (TWT) given a true-vertical-depth (TVD).
   *
   * @param zTVD the true-vertical-depth.
   * @param datumTVD the value for determining the two-way-time conversion factor.
   * @return the two-way-time.
   */
  public float caluclateTWTFromTVD(final float zTVD, final float datumTVD) {
    load();

    // If the TWT and TVD arrays do not contain at least 2 points, then simply return NaN.
    if (_zTWTs.length < 2 || _zTVDs.length < 2) {
      return Float.NaN;
    }

    // Adjust the TVD to the time datum.
    float datumConvFactor = _datum - datumTVD;
    float zTVDconv = zTVD + datumConvFactor;

    return WellUtil.lookupValue(zTVDconv, _zTVDs, "TVD", _zTWTs, "TWT");
  }

  /**
   * Returns the well that contains this check shot.
   * 
   * @return the well.
   */
  public Well getWell() {
    load();
    return _well;
  }

  /**
   * Indicates if a check shot is measured.
   *
   * @return <i>true</i> if a check shot is measure; <i>false</i> if not.
   */
  public boolean isCheckShot() {
    load();
    return _checkShot;
  }

  /**
   * Returns the person, company or vendor that created the velocity.
   */
  public String getDataSource() {
    load();
    return _dataSource;
  }

  /**
   * Returns the depth at which time is 0 in the check shot.
   * Both the depths and times are relative to this value.
   *
   * @return the datum
   */
  public float getDatum() {
    load();
    return _datum;
  }

  /**
   * The static shift value to apply to the time depth curve in display.
   *
   * @return the shift.
   */
  public float getDisplayShift() {
    load();
    return _displayShift;
  }

  /**
   * Returns the number of time and depth values.
   *
   * @return the number of samples.
   */
  public int getNumSamples() {
    load();
    return _zTVDs.length;
  }

  /**
   * Returns the seismic reference datum.
   *
   * @return the seismic reference datum.
   */
  public float getSeismicReferenceDatum() {
    load();
    return _seismicReferenceDatum;
  }

  /**
   * Returns the true-vertical-depth (TVD) values.
   */
  public float[] getTrueVerticalDepths() {
    load();
    return _zTVDs;
  }

  /**
   * Returns the two-way-time (TWT) values.
   */
  public float[] getTwoWayTimes() {
    load();
    return _zTWTs;
  }

  /**
   * Returns the replacement velocity to use between the check shot datum and the seismic reference datum.
   *
   * @return the replacement velocity.
   */
  public float getVelocityToSeismicDatum() {
    load();
    return _velocityToSeismicDatum;
  }

  /**
   * Sets the well bore that contains this check shot.
   *
   * @param wellBore
   */
  //public void setWellBore(final WellBore wellBore) {
  //  _wellBore = wellBore;
  //   setDirty(true);
  //}

  /**
   * Indicates if a checkshot is measured.
   *
   * @param checkShot
   */
  public void setCheckShot(final boolean checkShot) {
    _checkShot = checkShot;
    setDirty(true);
  }

  /**
   * The person, company or vendor that created the velocity.
   *
   * @param dataSource
   */
  public void setDataSource(final String dataSource) {
    _dataSource = dataSource;
    setDirty(true);
  }

  /**
   * The static shift value to apply to the time depth curve in display.
   *
   * @param displayShift
   */
  public void setDisplayShift(final float displayShift) {
    _displayShift = displayShift;
    setDirty(true);
  }

  /**
   * Sets the seismic reference datum.
   *
   * @param seismicReferenceDatum
   */
  public void setSeismicReferenceDatum(final float seismicReferenceDatum) {
    _seismicReferenceDatum = seismicReferenceDatum;
    setDirty(true);
  }

  /**
   * Sets the depths (TVD) and times (TWT) of the check shot.
   *
   * @param datum The depth at which time is 0 in the check shot (both the depths and times are relative to this value).
   * @param zTVDs the array of true-vertical-depth (TVD) values.
   * @param zTWTs the array of two-way-time (TWT) values.
   */
  public void setDepthsAndTimes(final float datum, final float[] zTVDs, final float[] zTWTs) {
    _datum = datum;
    _zTVDs = Utilities.copyFloatArray(zTVDs);
    _zTWTs = Utilities.copyFloatArray(zTWTs);
    setDirty(true);
  }

  /**
   * The replacement velocity to use between the WellCheckShot datum and the SeismicReferenceDatum.
   *
   * @param velocityToSeismicDatum
   */
  public void setVelocityToSeismicDatum(final float velocityToSeismicDatum) {
    _velocityToSeismicDatum = velocityToSeismicDatum;
    setDirty(true);
  }

}
