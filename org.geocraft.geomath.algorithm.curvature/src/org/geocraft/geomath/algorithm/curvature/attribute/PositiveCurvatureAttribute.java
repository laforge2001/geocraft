/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.curvature.attribute;


import org.geocraft.core.common.math.QuadraticSurface;
import org.geocraft.geomath.algorithm.curvature.AbstractAttribute;


/**
 * Implementation of the positive curvature attribute.
 */
public class PositiveCurvatureAttribute extends AbstractAttribute {

  public String getName() {
    return "Positive curvature";
  }

  public double calculate(final QuadraticSurface param) {
    return param.getPositiveCurvature();
  }

  @Override
  public String getSymbol() {
    return "Kpos";
  }

}
