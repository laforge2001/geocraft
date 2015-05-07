/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.math.regression;


/**
 * The interface for the regression method service.
 */
public interface IRegressionMethodService {

  /**
   * Returns an array of the regression methods currently registered.
   */
  RegressionMethodDescription[] getRegressionMethods();

  /**
   * Computes the regression statistics for the specified method, type and data.
   * @param method the desired regression method.
   * @param type the regression type (Origin or Offset).
   * @param data the regression data.
   */
  RegressionStatistics compute(RegressionMethodDescription method, RegressionType type, RegressionData data);

  /**
   * Computes the regression statistics for the specified method, type and data.
   * @param acronym the acronym of the desired regression method.
   * @param type the regression type (Origin or Offset).
   * @param data the regression data.
   */
  RegressionStatistics compute(String acronym, RegressionType type, RegressionData data);
}
