/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.classbkg;


/**
 * The interface for regions classifiers.
 * The method of importance in this interface is <code>processAB</code>,
 * which, given values for A and B, returns a classification value.
 * For values that do not fall into a valid region, <code>NaN</code>
 * should be returned.
 */
public interface IRegionsClassifier {

  /**
   * Returns the name of the classification process.
   */
  String getName();

  /**
   * Processes an A,B coordinate.
   * @param a the A coordinate.
   * @param b the B coordinate.
   * @return the classification value (or NaN if not valid).
   */
  double processAB(double a, double b);
}
