/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.curvature.attribute;


import org.geocraft.core.common.math.QuadraticSurface;
import org.geocraft.geomath.algorithm.curvature.AbstractAttribute;


/**
 * Implementation of the mean curvature attribute.
 */
public class MeanCurvatureAttribute extends AbstractAttribute {

  public String getName() {
    return "Mean curvature";
  }

  public double calculate(final QuadraticSurface param) {
    return param.getMeanCurvature();

  }

  @Override
  public String getSymbol() {
    return "Km";
  }
}
