/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.seismic;


import org.geocraft.core.model.Entity;
import org.geocraft.core.model.datatypes.FloatRange;
import org.geocraft.core.model.datatypes.SpatialExtent;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.mapper.IMapper;
import org.geocraft.core.model.preferences.UnitPreferences;


/**
 * Defines the wavelet class.
 */
public class Wavelet extends Entity {

  protected FloatRange _timeRange;

  protected float[] _values;

  protected String _dataType;

  protected String _phase;

  /**
   * Constructs an instance of Wavelet with specified name and mapper.
   */
  public Wavelet(final String name, final IMapper mapper) {
    super(name, mapper);
  }

  /**
   * Gets the number of samples in the wavelet.
   * 
   * @return the number of samples in the wavelet.
   */
  public int getNumSamples() {
    load();
    return _values.length;
  }

  /**
   * Gets the wavelet start time.
   * 
   * @return the wavelet start time.
   */
  public float getTimeStart() {
    load();
    return _timeRange.getStart();
  }

  /**
   * Gets the wavelet end time.
   * 
   * @return the wavelet end time.
   */
  public float getTimeEnd() {
    load();
    return _timeRange.getEnd();
  }

  /**
   * Gets the wavelet time interval.
   * 
   * @return the wavelet time interval.
   */
  public float getTimeInterval() {
    load();
    return _timeRange.getDelta();
  }

  /**
   * Gets the wavelet values.
   * 
   * @return the wavelet values.
   */
  public float[] getValues() {
    load();
    float[] values = new float[_values.length];
    System.arraycopy(_values, 0, values, 0, values.length);
    return values;
  }

  /**
   * Gets the wavelet minimum value.
   * 
   * @return the wavelet minimum value.
   */
  public float getMinimumValue() {
    load();
    float minimumValue = _values[0];
    for (int i = 1; i < _values.length; i++) {
      minimumValue = Math.min(_values[i], minimumValue);
    }
    return minimumValue;
  }

  /**
   * Gets the wavelet maximum value.
   * 
   * @return the wavelet maximum value.
   */
  public float getMaximumValue() {
    load();
    float maximumValue = _values[0];
    for (int i = 1; i < _values.length; i++) {
      maximumValue = Math.max(_values[i], maximumValue);
    }
    return maximumValue;
  }

  /**
   * Gets the wavelet data type.
   * 
   * @return the wavelet data type.
   */
  public String getDataType() {
    load();
    return _dataType;
  }

  /**
   * Gets the wavelet phase.
   * 
   * @return the wavelet phase.
   */
  public String getPhase() {
    load();
    return _phase;
  }

  /**
   * Sets the wavelet time range (start, end and delta).
   */
  public void setTimeRange(final float start, final float end, final float delta) {
    _timeRange = new FloatRange(start, end, delta);
  }

  /**
   * Sets the z units.
   * 
   * @param zUnits the z units.
   */
  public Unit setZUnit() {
    return UnitPreferences.getInstance().getTimeUnit();
  }

  /**
   * Sets the wavelet values.
   * 
   * @param values the wavelet values.
   */
  public void setValues(final float[] values) {
    if (_values == null || _values.length != values.length) {
      _values = new float[values.length];
    }
    System.arraycopy(values, 0, _values, 0, values.length);
  }

  /**
   * sets the wavelet data type.
   * 
   * @param dataType the wavelet data type.
   */
  public void setDataType(final String dataType) {
    _dataType = dataType;
  }

  /**
   * Sets the wavelet phase.
   * 
   * @param phase the wavelet phase.
   */
  public void setPhase(final String phase) {
    _phase = phase;
  }

  public SpatialExtent getSpatialExtent() {
    throw new UnsupportedOperationException("A wavelet does not have a spatial extent");
  }

}
