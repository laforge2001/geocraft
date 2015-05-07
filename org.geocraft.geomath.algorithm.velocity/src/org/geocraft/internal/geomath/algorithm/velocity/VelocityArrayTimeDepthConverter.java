/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.geomath.algorithm.velocity;


import java.util.EnumSet;

import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.preferences.UnitPreferences;


/**
 * Utility class for converting between time and depth, given an interval velocity array in time or depth.
 * Two methods are available:
 * Cell-Based assumes a step-wise interval velocity change at each sample.
 * Knee-Based assumes a lineary-varying interval velocity between samples.
 */
public class VelocityArrayTimeDepthConverter implements ITimeDepthConverter {

  public enum Method {
    CellBased("Cell-Based"),
    KneeBased("Knee-Based");

    private final String _displayName;

    Method(final String name) {
      _displayName = name;
    }

    @Override
    public String toString() {
      return _displayName;
    }

    public static Method getValue(final String name) throws IllegalStateException {
      for (Method c : EnumSet.allOf(Method.class)) {
        if (c.toString().equals(name)) {
          return c;
        }
      }
      throw new IllegalStateException();
    }
  }

  /** The internal array of time values. */
  private final float[] _times;

  /** The internal array of depth values. */
  private final float[] _depths;

  private int _timeIndex = 1;

  private int _depthIndex = 1;

  /**
   * Maps times vs. depths based on the specified interval velocity array and sample rate.
   * @param ivels the array of interval velocities.
   * @param sampleRate the sampling rate of the interval velocities.
   * @param velDomain the domain of the interval velocities samples (time or depth).
   * @param velUnits the interval velocity units (meters-per-second or feet-per-second).
   */
  public VelocityArrayTimeDepthConverter(final float[] ivels, final float sampleRate, final Domain velDomain, final Unit velUnits, final Method method) {
    UnitPreferences unitPrefs = UnitPreferences.getInstance();
    Unit timeUnits = unitPrefs.getTimeUnit();
    Unit depthUnits = unitPrefs.getVerticalDistanceUnit();

    // temporary default the units here
    if (timeUnits == Unit.UNDEFINED) {
      unitPrefs.setTimeUnit(Unit.MILLISECONDS);
      timeUnits = unitPrefs.getTimeUnit();
    }
    if (depthUnits == Unit.UNDEFINED) {
      unitPrefs.setVerticalDistanceUnit(Unit.FOOT);
      depthUnits = unitPrefs.getVerticalDistanceUnit();
    }

    int numSamples = ivels.length;
    double conversionFactor = 1.0;
    try {
      if (velUnits.equals(Unit.FEET_PER_SECOND)) {
        conversionFactor = Unit.convert(1.0, Unit.FOOT, depthUnits);
        conversionFactor /= Unit.convert(1.0, Unit.SECOND, timeUnits);
      } else if (velUnits.equals(Unit.METERS_PER_SECOND)) {
        conversionFactor = Unit.convert(1.0, Unit.METER, depthUnits);
        conversionFactor /= Unit.convert(1.0, Unit.SECOND, timeUnits);
      }
    } catch (Exception ex) {
      throw new RuntimeException("Invalid time or depth units.");
    }
    double[] vels = new double[ivels.length];
    for (int i = 0; i < vels.length; i++) {
      vels[i] = ivels[i] * conversionFactor;
    }
    _times = new float[numSamples];
    _depths = new float[numSamples];
    if (method.equals(Method.CellBased)) {
      if (velDomain.equals(Domain.TIME)) {
        _times[0] = 0;
        _depths[0] = 0;
        for (int i = 1; i < numSamples; i++) {
          _times[i] = i * sampleRate;
          _depths[i] = _depths[i - 1] + (float) (vels[i] * sampleRate / 2);
        }
      } else if (velDomain.equals(Domain.DISTANCE)) {
        _times[0] = 0;
        _depths[0] = 0;
        for (int i = 1; i < numSamples; i++) {
          _depths[i] = i * sampleRate;
          _times[i] = _times[i - 1] + (float) (sampleRate * 2 / vels[i]);
        }
      }
    } else if (method.equals(Method.KneeBased)) {
      if (velDomain.equals(Domain.TIME)) {
        _times[0] = 0;
        _depths[0] = 0;
        for (int i = 1; i < numSamples; i++) {
          _times[i] = i * sampleRate;
          _depths[i] = _depths[i - 1]
              + (float) (vels[i - 1] * sampleRate / 2 + (vels[i] - vels[i - 1]) * sampleRate / 2 / 2);
        }
      } else if (velDomain.equals(Domain.DISTANCE)) {
        _times[0] = 0;
        _depths[0] = 0;
        for (int i = 1; i < numSamples; i++) {
          _depths[i] = i * sampleRate;
          double k = (vels[i] - vels[i - 1]) / (sampleRate * 2);
          if (k == 0f) {
            _times[i] = _times[i - 1] + (float) (2 * sampleRate / vels[i]);
          } else {
            _times[i] = _times[i - 1] + (float) (Math.log(vels[i] / vels[i - 1]) / k);
          }
        }
      }
    } else {
      throw new IllegalArgumentException("Invalid time-depth conversion method.");
    }
  }

  /**
   * Maps times vs. depths based on the specified interval velocity array and sample rate.
   * (This is for a constant velocity
   * @param ivels the array of interval velocities.
   * @param sampleRate the sampling rate of the interval velocities.
   * @param velDomain the domain of the interval velocities samples (time or depth).
   * @param velUnits the interval velocity units (meters-per-second or feet-per-second).
   */
  public VelocityArrayTimeDepthConverter(final Double velocity, final int numSamples, final float sampleRate, final Domain velDomain, final Unit velUnits, final Method method) {
    UnitPreferences unitPrefs = UnitPreferences.getInstance();
    Unit timeUnits = unitPrefs.getTimeUnit();
    Unit depthUnits = unitPrefs.getVerticalDistanceUnit();

    // temporary default the units here
    if (timeUnits == Unit.UNDEFINED) {
      unitPrefs.setTimeUnit(Unit.MILLISECONDS);
      timeUnits = unitPrefs.getTimeUnit();
    }
    if (depthUnits == Unit.UNDEFINED) {
      unitPrefs.setVerticalDistanceUnit(Unit.FOOT);
      depthUnits = unitPrefs.getVerticalDistanceUnit();
    }

    double conversionFactor = 1;
    try {
      if (velUnits.equals(Unit.FEET_PER_SECOND)) {
        conversionFactor = Unit.convert(1, Unit.FOOT, depthUnits);
        conversionFactor /= Unit.convert(1, Unit.SECOND, timeUnits);
      } else if (velUnits.equals(Unit.METERS_PER_SECOND)) {
        conversionFactor = Unit.convert(1, Unit.METER, depthUnits);
        conversionFactor /= Unit.convert(1, Unit.SECOND, timeUnits);
      }
    } catch (Exception ex) {
      throw new RuntimeException("Invalid time or depth units.");
    }
    double[] vels = new double[numSamples];
    for (int i = 0; i < vels.length; i++) {
      vels[i] = velocity * conversionFactor;
    }
    _times = new float[numSamples];
    _depths = new float[numSamples];
    if (method.equals(Method.CellBased)) {
      if (velDomain.equals(Domain.TIME)) {
        _times[0] = 0;
        _depths[0] = 0;
        for (int i = 1; i < numSamples; i++) {
          _times[i] = i * sampleRate;
          _depths[i] = _depths[i - 1] + (float) (vels[i] * sampleRate / 2);
        }
      } else if (velDomain.equals(Domain.DISTANCE)) {
        _times[0] = 0;
        _depths[0] = 0;
        for (int i = 1; i < numSamples; i++) {
          _depths[i] = i * sampleRate;
          _times[i] = _times[i - 1] + (float) (sampleRate * 2 / vels[i]);
        }
      }
    } else if (method.equals(Method.KneeBased)) {
      if (velDomain.equals(Domain.TIME)) {
        _times[0] = 0;
        _depths[0] = 0;
        for (int i = 1; i < numSamples; i++) {
          _times[i] = i * sampleRate;
          _depths[i] = _depths[i - 1]
              + (float) (vels[i - 1] * sampleRate / 2 + (vels[i] - vels[i - 1]) * sampleRate / 2 / 2);
        }
      } else if (velDomain.equals(Domain.DISTANCE)) {
        _times[0] = 0;
        _depths[0] = 0;
        for (int i = 1; i < numSamples; i++) {
          _depths[i] = i * sampleRate;
          double k = (vels[i] - vels[i - 1]) / (sampleRate * 2);
          if (k == 0f) {
            _times[i] = _times[i - 1] + (float) (2 * sampleRate / vels[i]);
          } else {
            _times[i] = _times[i - 1] + (float) (Math.log(vels[i] / vels[i - 1]) / k);
          }
        }
      }
    } else {
      throw new IllegalArgumentException("Invalid time-depth conversion method.");
    }
  }

  public float[] getTimeArray() {
    return _times;
  }

  public float[] getDepthArray() {
    return _depths;
  }

  public float getDepth(final float time) {
    int numSamples = _times.length;
    for (int i = _timeIndex; i < numSamples; i++) {
      if (time >= _times[i - 1] && time <= _times[i]) {
        float percent = (time - _times[i - 1]) / (_times[i] - _times[i - 1]);
        _timeIndex = i;
        return _depths[i - 1] + percent * (_depths[i] - _depths[i - 1]);
      }
    }
    // Extrapolate if time is outside of the time-depth array
    if (time > _times[numSamples - 1]) {
      float percent = (_depths[numSamples - 1] - _depths[numSamples - 2])
          / (_times[numSamples - 1] - _times[numSamples - 2]);
      return _depths[numSamples - 1] + percent * (time - _times[numSamples - 1]);
    }
    _timeIndex = 1;
    return Float.NaN;
  }

  public float getTime(final float depth) {
    int numSamples = _depths.length;
    for (int i = _depthIndex; i < numSamples; i++) {
      if (depth >= _depths[i - 1] && depth <= _depths[i]) {
        float percent = (depth - _depths[i - 1]) / (_depths[i] - _depths[i - 1]);
        _depthIndex = i;
        return _times[i - 1] + percent * (_times[i] - _times[i - 1]);
      }
    }
    // Extrapolate if depth is outside of the time-depth array
    if (depth > _depths[numSamples - 1]) {
      float percent = (_times[numSamples - 1] - _times[numSamples - 2])
          / (_depths[numSamples - 1] - _depths[numSamples - 2]);
      return _times[numSamples - 1] + percent * (depth - _depths[numSamples - 1]);
    }
    _depthIndex = 1;
    return Float.NaN;
  }

  public float[] getDepths(final float[] times) {
    float[] depths = new float[times.length];
    for (int i = 0; i < times.length; i++) {
      depths[i] = getDepth(times[i]);
    }
    return depths;
  }

  public float[] getTimes(final float[] depths) {
    float[] times = new float[depths.length];
    for (int i = 0; i < depths.length; i++) {
      times[i] = getTime(depths[i]);
    }
    return times;
  }

}
