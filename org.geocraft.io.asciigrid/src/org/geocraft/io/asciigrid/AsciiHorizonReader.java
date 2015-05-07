/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.asciigrid;


import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.model.datatypes.CoordinateSystem;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.preferences.ApplicationPreferences;
import org.geocraft.core.model.preferences.UnitPreferences;


/**
 * This ascii horizon reader implementation uses Geocraft-specific units 
 * and eclipse unit preferences stuff
 *  
 */
public class AsciiHorizonReader extends AbstractAsciiHorizonReader {

  /**
   * @param model
   */
  public AsciiHorizonReader(AsciiHorizonMapperModel model) {
    super(model);
  }

  /** The application preferences. */
  private static final UnitPreferences UNIT_PREFS = UnitPreferences.getInstance();

  /* (non-Javadoc)
   * @see org.geocraft.io.asciigrid.AsciiHorizonReader#setUnitPreferences()
   */
  @Override
  public void setUnitPreferences() {
    Unit appUnitsXY = UNIT_PREFS.getHorizontalDistanceUnit();
    if (appUnitsXY == Unit.UNDEFINED) {
      UNIT_PREFS.setHorizontalDistanceUnit(_model.getXyUnits());
      appUnitsXY = _model.getXyUnits();
    }
  }

  /* (non-Javadoc)
   * @see org.geocraft.io.asciigrid.AsciiHorizonReader#getDataUnits()
   */
  @Override
  public Unit getDataUnits(Domain domain) {
    // Determine the application data unit.
    Unit appDataUnits = Unit.UNDEFINED;
    if (domain == Domain.TIME) { // If the domain is time, then get the application time units.
      // If the application time units are undefined, set them based on this mapper.
      if (UNIT_PREFS.getTimeUnit() == Unit.UNDEFINED) {
        UNIT_PREFS.setTimeUnit(_model.getDataUnits());
      }
      appDataUnits = UNIT_PREFS.getTimeUnit();
    } else if (domain == Domain.DISTANCE) { // If the domain is depth, then get the application depth units.
      // If the application depth units are undefined, set them based on this mapper.
      if (UNIT_PREFS.getVerticalDistanceUnit() == Unit.UNDEFINED) {
        UNIT_PREFS.setVerticalDistanceUnit(_model.getDataUnits());
      }
      appDataUnits = UNIT_PREFS.getVerticalDistanceUnit();
    }
    return appDataUnits;
  }

  /* (non-Javadoc)
   * @see org.geocraft.io.asciigrid.AsciiHorizonReader#getCoordinateSystem()
   */
  @Override
  public CoordinateSystem getCoordinateSystem(Domain zDomain) {
    CoordinateSystem coordSys = null;
    if (zDomain.equals(Domain.TIME)) {
      coordSys = ApplicationPreferences.getInstance().getTimeCoordinateSystem();
    } else if (zDomain.equals(Domain.DISTANCE)) {
      coordSys = ApplicationPreferences.getInstance().getDepthCoordinateSystem();
    }
    return coordSys;
  }

  /* (non-Javadoc)
   * @see org.geocraft.io.asciigrid.AbstractAsciiHorizonReader#setDisplayColor(org.geocraft.core.model.grid.Grid3d)
   */
  @Override
  public void setDisplayColor(Grid3d grid) {
    grid.setDisplayColor(new RGB(255, 0, 0));
  }

}
