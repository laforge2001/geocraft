/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.curvature.attribute;


import org.geocraft.core.common.math.QuadraticSurface;
import org.geocraft.geomath.algorithm.curvature.AbstractAttribute;


/**
 * Implementation of the strkie curvature attribute.
 */
public class StrikeCurvatureAttribute extends AbstractAttribute {

  public String getName() {
    return "Strike curvature";
  }

  public double calculate(final QuadraticSurface param) {
    return param.getStrikeCurvature();
  }

  @Override
  public String getSymbol() {
    return "Ks";
  }

}
