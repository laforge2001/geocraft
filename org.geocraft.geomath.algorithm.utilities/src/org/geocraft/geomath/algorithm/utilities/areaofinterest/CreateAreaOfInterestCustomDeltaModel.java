/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.utilities.areaofinterest;


import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.seismic.PostStack3d;


public class CreateAreaOfInterestCustomDeltaModel extends CreateAreaOfInterestAlgorithm {

  private IntegerProperty _numSamples;

  public static String NUM_SAMPLES = "numSamples";

  public CreateAreaOfInterestCustomDeltaModel() {
    super();
    _numSamples = addIntegerProperty(NUM_SAMPLES, 0);
  }

  /**
   * @return the numSamples
   */
  public int getNumSamples() {
    return _numSamples.get();
  }

  /**
   * @param numSamples
   *            the numSamples to set
   */
  public void setNumSamples(final int numSamples) {
    _numSamples.set(numSamples);
  }

  public void setInputVolume(final PostStack3d volume) {
    _referenceEntity.set(volume);
  }

  @Override
  public void propertyChanged(String key) {
    super.propertyChanged(key);

  }

}
