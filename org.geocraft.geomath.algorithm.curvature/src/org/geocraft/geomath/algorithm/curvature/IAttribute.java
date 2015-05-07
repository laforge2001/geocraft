/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.curvature;


import org.geocraft.core.common.math.QuadraticSurface;


/**
 * Interface to be implemented by the linear attributes.
 */
public interface IAttribute {

  /**
   * Return the name of the attribute.
   * @return the attribute name
   */
  String getName();

  /**
   * Return the name of the attribute.
   * @return the attribute name
   */
  String toString();

  /**
   * Calculate the attribute value.
   * @param param the attribute parameters
   * @return the calculated value
   */
  double calculate(QuadraticSurface param);

  /**
   * Return the symbol of the attribute.
   * @return the symbol
   */
  String getSymbol();
}
