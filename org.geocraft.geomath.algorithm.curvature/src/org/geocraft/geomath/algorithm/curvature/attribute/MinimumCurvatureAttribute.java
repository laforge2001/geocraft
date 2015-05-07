/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.curvature.attribute;


import org.geocraft.core.common.math.QuadraticSurface;
import org.geocraft.geomath.algorithm.curvature.AbstractAttribute;


/**
 * Implementation of the minimum curvature attribute.
 */
public class MinimumCurvatureAttribute extends AbstractAttribute {

  public String getName() {
    return "Minimum curvature";
  }

  public double calculate(final QuadraticSurface param) {
    return param.getMinimumCurvature();
  }

  @Override
  public String getSymbol() {
    return "Kmin";
  }

}
