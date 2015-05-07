/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.asciigrid;


import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.preferences.UnitPreferences;


public class AsciiHorizonWriter extends AbstractAsciiHorizonWriter {

  /**
   * @param model
   */
  public AsciiHorizonWriter(AsciiHorizonMapperModel model) {
    super(model);
  }

  /* (non-Javadoc)
   * @see org.geocraft.io.asciigrid.AsciiHorizonWriter#convertUnit(double)
   */
  @Override
  public double convertUnit(double fromUnit) {
    return Unit.convert(fromUnit, UnitPreferences.getInstance().getHorizontalDistanceUnit(), _model.getXyUnits());
  }

}
