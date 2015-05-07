/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.preferences;


import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.geocraft.core.common.preferences.PreferencesUtil;
import org.geocraft.core.model.datatypes.Unit;


public class UnitPreferences {

  private static final String PLUGIN_ID = "org.geocraft.core.model";

  /** The preferences store. */
  private static IEclipsePreferences _preferences = null;

  static {
    try {
      _preferences = PreferencesUtil.getPreferencesStore(PLUGIN_ID);
    } catch (NullPointerException e) {
      _preferences = new NullEclipsePreferences();
    }
  }

  private static UnitPreferences _singleton;

  public static final String PREFERRED_TIME_UNIT_DEPRECATED = "timeUnits";

  public static final String PREFERRED_DEPTH_UNIT_DEPRECATED = "verticalUnits";

  public static final String PREFERRED_HORIZONTAL_DISTANCE_UNIT_DEPRECATED = "horizontalUnits";

  public static final String TIME_UNIT = "timeUnit";

  public static final String DEPTH_UNIT = "depthUnit";

  public static final String HORIZONTAL_UNIT = "horizontalDistanceUnit";

  private Unit _timeUnit = Unit.lookupByName(_preferences.get(PREFERRED_TIME_UNIT_DEPRECATED,
      Unit.MILLISECONDS.toString()));

  private Unit _verticalDistanceUnit = Unit.lookupByName(_preferences.get(PREFERRED_DEPTH_UNIT_DEPRECATED,
      Unit.METER.toString()));

  private Unit _horizontalDistanceUnit = Unit.lookupByName(_preferences.get(
      PREFERRED_HORIZONTAL_DISTANCE_UNIT_DEPRECATED, Unit.METER.toString()));

  /**
   * Private constructor to force everyone to call the static method to retrieve
   * the instance.
   */
  private UnitPreferences() {
    updateUnitPreferences();
  }

  /**
   * @return the ApplicationPreferences object.
   */
  public static synchronized UnitPreferences getInstance() {
    if (_singleton == null) {
      _singleton = new UnitPreferences();
    }
    return _singleton;
  }

  public void updateUnitPreferences() {
    Unit unit = Unit.lookupByName(_preferences.get(TIME_UNIT, _timeUnit.getName()));
    setTimeUnit(unit);
    unit = Unit.lookupByName(_preferences.get(DEPTH_UNIT, _verticalDistanceUnit.getName()));
    setVerticalDistanceUnit(unit);
    unit = Unit.lookupByName(_preferences.get(HORIZONTAL_UNIT, _horizontalDistanceUnit.getName()));
    setHorizontalDistanceUnit(unit);
  }

  /**
   * Gets the preferred time unit.
   * 
   * @return the preferred time unit.
   */
  public Unit getTimeUnit() {
    return _timeUnit;
  }

  /**
   * Gets the preferred vertical distance (depth) unit.
   * 
   * @return the preferred depth unit.
   */
  public Unit getVerticalDistanceUnit() {
    return _verticalDistanceUnit;
  }

  /**
   * Gets the preferred horizontal distance unit.
   * 
   * @return the preferred horizontal distance unit.
   */
  public Unit getHorizontalDistanceUnit() {
    return _horizontalDistanceUnit;
  }

  /**
   * Sets the preferred time unit.
   * 
   * @param unit
   *          the preferred time unit
   */
  public void setTimeUnit(final Unit unit) {
    if (unit != null) {
      _timeUnit = unit;
    }
  }

  /**
   * Sets the preferred vertical distance (depth) unit.
   * 
   * @param unit
   *          the preferred depth unit
   */
  public void setVerticalDistanceUnit(final Unit unit) {
    if (unit != null) {
      _verticalDistanceUnit = unit;
    }
  }

  /**
   * Sets the preferred horizontal distance unit.
   * 
   * @param unit
   *          the preferred horizontal distance unit
   */
  public void setHorizontalDistanceUnit(final Unit unit) {
    if (unit != null) {
      _horizontalDistanceUnit = unit;
    }
  }
}
