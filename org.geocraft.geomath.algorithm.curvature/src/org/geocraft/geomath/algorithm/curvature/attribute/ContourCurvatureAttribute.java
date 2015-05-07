/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.curvature.attribute;


import org.geocraft.core.common.math.QuadraticSurface;
import org.geocraft.geomath.algorithm.curvature.AbstractAttribute;


/**
 * Implementation of the contour curvature attribute.
 */
public class ContourCurvatureAttribute extends AbstractAttribute {

  public String getName() {
    return "Contour curvature";
  }

  public double calculate(final QuadraticSurface param) {
    return param.getContourCurvature();
  }

  @Override
  public String getSymbol() {
    return "Kc";
  }

}
