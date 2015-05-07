/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.curvature.attribute;


import org.geocraft.core.common.math.QuadraticSurface;
import org.geocraft.geomath.algorithm.curvature.AbstractAttribute;


/**
 * Implementation of the negative curvature attribute.
 */
public class NegativeCurvatureAttribute extends AbstractAttribute {

  public String getName() {
    return "Negative curvature";
  }

  public double calculate(final QuadraticSurface param) {
    return param.getNegativeCurvature();
  }

  @Override
  public String getSymbol() {
    return "Kneg";
  }

}
