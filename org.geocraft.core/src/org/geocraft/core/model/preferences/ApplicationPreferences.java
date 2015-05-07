/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.core.model.preferences;


import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.geocraft.core.common.preferences.PreferencesUtil;
import org.geocraft.core.model.datatypes.CoordinateSystem;
import org.geocraft.core.model.datatypes.Domain;


/**
 * Class holding the coordinate system, unit preferences
 */
public class ApplicationPreferences {

  private static final String PLUGIN_ID = "org.geocraft.core.model";

  private static ApplicationPreferences _singleton;

  private CoordinateSystem _timeCoordinateSystem = new CoordinateSystem("UTM15", Domain.TIME);

  private CoordinateSystem _depthCoordinateSystem = new CoordinateSystem("UTM15", Domain.DISTANCE);

  /** The preferences store. */
  private IEclipsePreferences _preferences;

  {
    try {
      _preferences = PreferencesUtil.getPreferencesStore(PLUGIN_ID);
    } catch (NullPointerException e) {
      _preferences = new NullEclipsePreferences();
    }
  }

  /** The Seismic Datum Elevation value. */
  private float _seismicDatumElevation = 0;

  /** The supported unit domains. */
  public static final Domain[] _unitDomains = new Domain[] { Domain.TIME, Domain.DISTANCE, Domain.DISTANCE };

  /** The supported unit sub domains. */
  public static final String[] _unitSubdomains = new String[] { "", "Horizontal", "Vertical" };

  // Force everybody to call the static method to retrieve the object
  private ApplicationPreferences() {
    updateAppPreferences();
  }

  /**
   * @return the ApplicationPreferences object.
   */
  public static synchronized ApplicationPreferences getInstance() {
    if (_singleton == null) {
      _singleton = new ApplicationPreferences();
    }
    return _singleton;
  }

  /**
   * Update the app preferences internally, whenever they might have changed.
   */
  public void updateAppPreferences() {
    CoordinateSystem c = CoordinateSystemService.getInstance().getSystemForName(
        _preferences.get("CoordinateSystem" + Domain.TIME.name(), _timeCoordinateSystem.getName()), Domain.TIME);
    setTimeCoordinateSystem(c);
    c = CoordinateSystemService.getInstance().getSystemForName(
        _preferences.get("CoordinateSystem" + Domain.DISTANCE.name(), _depthCoordinateSystem.getName()),
        Domain.DISTANCE);
    setDepthCoordinateSystem(c);
    _seismicDatumElevation = _preferences.getInt("seismicDatumElevation", (int) _seismicDatumElevation);
  }

  /**
   * @return the preferred time coordinate system.
   */
  public CoordinateSystem getTimeCoordinateSystem() {
    return _timeCoordinateSystem;
  }

  /**
   * @return the preferred depth coordinate system.
   */
  public CoordinateSystem getDepthCoordinateSystem() {
    return _depthCoordinateSystem;
  }

  /**
   * Return the Seismic Datum Elevation value.
   * 
   * @return the Seismic Datum Elevation value
   */
  public float getSeismicDatumElevation() {
    return _seismicDatumElevation;
  }

  /**
   * Sets the preferred time coordinate system.
   * 
   * @param cs
   *          the preferred time coordinate system
   */
  public void setTimeCoordinateSystem(final CoordinateSystem cs) {
    if (cs != null) {
      _timeCoordinateSystem = cs;
    }
  }

  /**
   * Sets the preferred depth coordinate system.
   * 
   * @param cs
   *          the preferred depth coordinate system
   */
  public void setDepthCoordinateSystem(final CoordinateSystem cs) {
    if (cs != null) {
      _depthCoordinateSystem = cs;
    }
  }

  /**
   * Set the Seismic Datum Elevation value.
   * 
   * @param seismicDatumElevation
   *          the Seismic Datum Elevation value
   */
  public void setSeismicDatumElevation(final float seismicDatumElevation) {
    _seismicDatumElevation = seismicDatumElevation;
  }

}
