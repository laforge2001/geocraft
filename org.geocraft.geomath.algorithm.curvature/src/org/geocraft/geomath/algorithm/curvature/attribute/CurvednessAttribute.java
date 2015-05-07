/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.curvature.attribute;


import org.geocraft.core.common.math.QuadraticSurface;
import org.geocraft.geomath.algorithm.curvature.AbstractAttribute;


/**
 * Implementation of the curvedness attribute.
 */
public class CurvednessAttribute extends AbstractAttribute {

  public String getName() {
    return "Curvedness";
  }

  public double calculate(final QuadraticSurface param) {
    return param.getCurvedness();
  }

  @Override
  public String getSymbol() {
    return "Kn";
  }

}
