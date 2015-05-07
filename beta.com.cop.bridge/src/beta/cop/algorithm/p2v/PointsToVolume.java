/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package beta.cop.algorithm.p2v;


public class PointsToVolume {

  private IPointToVolumeStrategy _strategy;

  public PointsToVolume(IPointToVolumeStrategy strategy) {
    _strategy = strategy;
  }

  public void interpolate(String attributeOfInterest) {
    _strategy.interpolate(attributeOfInterest);
  }

  public float getPercentComplete() {
    return _strategy.getPercentComplete();
  }
}
