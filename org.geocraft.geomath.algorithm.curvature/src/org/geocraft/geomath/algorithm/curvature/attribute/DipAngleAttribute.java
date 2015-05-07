/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.curvature.attribute;


import org.geocraft.core.common.math.QuadraticSurface;
import org.geocraft.geomath.algorithm.curvature.AbstractAttribute;


/**
 * Implementation of the dip angle attribute.
 */
public class DipAngleAttribute extends AbstractAttribute {

  public String getName() {
    return "Dip angle";
  }

  public double calculate(final QuadraticSurface param) {
    return param.getDipAngle();
  }

  @Override
  public String getSymbol() {
    return "Da";
  }

}
