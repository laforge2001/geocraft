/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.seismic;


/**
 * The interface for classes that transform seismic line coordinates.
 */
public interface ISeismicLineCoordinateTransform {

  /**
   * Transforms a CDP to a shotpoint.
   * 
   * @param cdp the CDP coordinate.
   * @return the shotpoint coordinate.
   * @throws IndexOutOfBoundsException thrown if the CDP is out-of-bounds.
   */
  float transformCDPToShotpoint(final float cdp) throws IndexOutOfBoundsException;

  /**
   * Transforms a shotpoint to a CDP.
   * 
   * @param shotpoint the shotpoint coordinate.
   * @return the CDP coordinate.
   * @throws IndexOutOfBoundsException thrown if the shotpoint is out-of-bounds.
   */
  float transformShotpointToCDP(final float shotpoint) throws IndexOutOfBoundsException;

  /**
   * Transforms an array of CDPs to an array of shotpoints.
   * 
   * @param cdps the array of CDP coordinates.
   * @return the array of shotpoint coordinates.
   * @throws IndexOutOfBoundsException thrown if any of the CDPs is out-of-bounds.
   */
  float[] transformCDPsToShotpoints(final float[] cdps) throws IndexOutOfBoundsException;

  /**
   * Transforms an array of shotpoints to an array of CDPs.
   * 
   * @param shotpoints the array of shotpoint coordinates.
   * @return the array of CDP coordinates.
   * @throws IndexOutOfBoundsException thrown if any of the shotpoints is out-of-bounds.
   */
  float[] transformShotpointsToCDPs(final float[] shotpoints) throws IndexOutOfBoundsException;
}
