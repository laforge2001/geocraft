/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.internal.geomath.algorithm.velocity;


/**
 * The interface for a time-to-depth (or depth-to-time) converter.
 */
public interface ITimeDepthConverter {

  /**
   * Gets the depth value corresponding to the specified time value.
   * @param time the time value.
   * @return the depth value.
   */
  float getDepth(float time);

  /**
   * Gets the time value corresponding to the specified depth value.
   * @param depth the depth value.
   * @return the time value.
   */
  float getTime(float depth);

  /**
   * Gets the depth values corresponding to the specified time values.
   * @param times the time values.
   * @return the depth values.
   */
  float[] getDepths(float[] times);

  /**
   * Gets the time values corresponding to the specified depth values.
   * @param depths the depth values.
   * @return the time values.
   */
  float[] getTimes(float[] depths);

  /**
   * Gets the internal array of time values.
   * @return the internal array of time values.
   */
  float[] getTimeArray();

  /**
   * Gets the internal array of depth values.
   * @return the internal array of depth values.
   */
  float[] getDepthArray();
}