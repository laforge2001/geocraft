/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.curvature.attribute;


import org.geocraft.core.common.math.QuadraticSurface;
import org.geocraft.geomath.algorithm.curvature.AbstractAttribute;


/**
 * Implementation of the shape index attribute.
 */
public class ShapeIndexAttribute extends AbstractAttribute {

  public String getName() {
    return "Shape index";
  }

  public double calculate(final QuadraticSurface param) {
    return param.getShapeIndex();
  }

  @Override
  public String getSymbol() {
    return "Si";
  }

}
