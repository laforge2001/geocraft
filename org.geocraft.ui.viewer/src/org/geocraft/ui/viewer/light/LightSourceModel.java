/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.viewer.light;


import org.geocraft.core.model.Model;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.validation.IValidation;


public class LightSourceModel extends Model {

  public static final String AZIMUTH = "Azimuth";

  public static final String ELEVATION = "Elevation";

  private IntegerProperty _azimuth;

  private IntegerProperty _elevation;

  public LightSourceModel() {
    _azimuth = addIntegerProperty(AZIMUTH, 225);
    _elevation = addIntegerProperty(ELEVATION, 45);
  }

  @Override
  public void validate(IValidation results) {
    if (_azimuth.get() < 0 || _azimuth.get() > 360) {
      results.error(_azimuth, "Azimuth must be between 0-360.");
    }
    if (_elevation.get() < 0 || _elevation.get() > 90) {
      results.error(_elevation, "Elevation must be between 0-90.");
    }
  }

  public int getAzimuth() {
    return _azimuth.get();
  }

  public int getElevation() {
    return _elevation.get();
  }

  public void setAzimuth(final int azimuth) {
    _azimuth.set(azimuth);
  }

  public void setElevation(int elevation) {
    _elevation.set(elevation);
  }

}
