/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.seismic;


import org.geocraft.core.model.datatypes.FloatRange;


/**
 * Implementation of a simple coordinate transform for seismic lines.
 * <p>
 * This transform is passed a range or CDP values and a range of shotpoint values.
 * This ranges must contain the same number of values. Transforms consist of a simple
 * one-to-one mapping between the CDP and shotpoint values.
 */
public final class SimpleSeismicLineCoordinateTransform implements ISeismicLineCoordinateTransform {

  /** The range (start,end,delta) of CDP values. */
  private final FloatRange _cdpRange;

  /** The range (start,end,delta) of shotpoint values. */
  private final FloatRange _shotpointRange;

  public SimpleSeismicLineCoordinateTransform(final FloatRange cdpRange, final FloatRange shotpointRange) {
    // Validate the CDP and shotpoint ranges are non-null.
    if (cdpRange == null) {
      throw new RuntimeException("Invalid CDP range: " + cdpRange);
    }
    if (shotpointRange == null) {
      throw new RuntimeException("Invalid shotpoint range: " + shotpointRange);
    }

    // Validate the CDP and shotpoint ranges have the same # of values.
    if (cdpRange.getNumSteps() != shotpointRange.getNumSteps()) {
      throw new RuntimeException("The # of CDPs does not equal the # of shotpoints.");
    }

    _cdpRange = cdpRange;
    _shotpointRange = shotpointRange;
  }

  public float transformCDPToShotpoint(final float cdp) {
    float shotpoint = 0;
    int index = Math.round((cdp - _cdpRange.getStart()) / _cdpRange.getDelta());
    if (index >= 0 && index < _shotpointRange.getNumSteps()) {
      shotpoint = _shotpointRange.getValue(index);
    } else {
      throw new IndexOutOfBoundsException("Invalid CDP: " + cdp + ". Must be in the range " + _cdpRange.getStart()
          + " to " + _cdpRange.getEnd());
    }
    return shotpoint;
  }

  public float transformShotpointToCDP(final float shotpoint) {
    float cdp = 0;
    int index = Math.round((shotpoint - _shotpointRange.getStart()) / _shotpointRange.getDelta());
    if (index >= 0 && index < _cdpRange.getNumSteps()) {
      cdp = _cdpRange.getValue(index);
    } else {
      throw new IndexOutOfBoundsException("Invalid Shotpoint: " + shotpoint + ". Must be in the range "
          + _shotpointRange.getStart() + " to " + _shotpointRange.getEnd());
    }
    return cdp;
  }

  public float[] transformCDPsToShotpoints(final float[] cdps) {
    float[] shotpoints = new float[cdps.length];
    for (int i = 0; i < cdps.length; i++) {
      int index = Math.round((cdps[i] - _cdpRange.getStart()) / _cdpRange.getDelta());
      if (index >= 0 && index < _shotpointRange.getNumSteps()) {
        shotpoints[i] = _shotpointRange.getValue(index);
      } else {
        throw new IndexOutOfBoundsException("Invalid CDP: " + cdps[i] + ". Must be in the range "
            + _cdpRange.getStart() + " to " + _cdpRange.getEnd());
      }
    }
    return shotpoints;
  }

  public float[] transformShotpointsToCDPs(final float[] shotpoints) {
    float[] cdps = new float[shotpoints.length];
    for (int i = 0; i < shotpoints.length; i++) {
      int index = Math.round((shotpoints[i] - _shotpointRange.getStart()) / _shotpointRange.getDelta());
      if (index >= 0 && index < _cdpRange.getNumSteps()) {
        cdps[i] = _cdpRange.getValue(index);
      } else {
        throw new IndexOutOfBoundsException("Invalid Shotpoint: " + shotpoints[i] + ". Must be in the range "
            + _shotpointRange.getStart() + " to " + _shotpointRange.getEnd());
      }
    }
    return cdps;
  }
}
