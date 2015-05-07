/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.curvature.attribute;


import org.geocraft.core.common.math.QuadraticSurface;
import org.geocraft.geomath.algorithm.curvature.AbstractAttribute;


/**
 * Implementation of the azimuth attribute.
 */
public class AzimuthAttribute extends AbstractAttribute {

  public String getName() {
    return "Azimuth";
  }

  public double calculate(final QuadraticSurface param) {
    return param.getDipAzimuth();
  }

  @Override
  public String getSymbol() {
    return "Az";
  }

}
