/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.curvature.attribute;


import org.geocraft.core.common.math.QuadraticSurface;
import org.geocraft.geomath.algorithm.curvature.AbstractAttribute;


/**
 * Implementation of the dip curvature attribute.
 */
public class DipCurvatureAttribute extends AbstractAttribute {

  public String getName() {
    return "Dip curvature";
  }

  public double calculate(final QuadraticSurface param) {

    return param.getDipCurvature();
  }

  @Override
  public String getSymbol() {
    return "Kd";
  }

}
