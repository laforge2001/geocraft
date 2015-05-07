/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.curvature.attribute;


import org.geocraft.core.common.math.QuadraticSurface;
import org.geocraft.geomath.algorithm.curvature.AbstractAttribute;


/**
 * Implementation of the Gaussian curvature algorithm.
 */
public class GaussianCurvatureAttribute extends AbstractAttribute {

  public String getName() {
    return "Gaussian curvature";
  }

  public double calculate(final QuadraticSurface param) {
    return param.getGaussianCurvature();
  }

  @Override
  public String getSymbol() {
    return "Kg";
  }
}
