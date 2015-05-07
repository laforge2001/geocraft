/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.curvature.attribute;


import org.geocraft.core.common.math.QuadraticSurface;
import org.geocraft.geomath.algorithm.curvature.AbstractAttribute;


/**
 * Implementation of the maximum curvature attribute.
 */
public class MaximumCurvatureAttribute extends AbstractAttribute {

  public String getName() {
    return "Maximum curvature";
  }

  public double calculate(final QuadraticSurface param) {
    return param.getMaximumCurvature();
  }

  @Override
  public String getSymbol() {
    return "Kmax";
  }

}
