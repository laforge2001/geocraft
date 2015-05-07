/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package beta.cop.algorithm.p2v;


public interface IPointToVolumeStrategy {

  /**
   * handles the interpolation specific to the strategy
   */
  public void interpolate(String attributeOfInterest);

  /**
   * gets the progress of operation 
   * @return percentage complete as a float (0 - 100)
   */
  public float getPercentComplete();

}
