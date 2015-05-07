/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.util.subvolume;


import org.geocraft.core.model.datatypes.Trace;


/**
 * The interface for all sub-volume iterator read strategies.
 */
public interface ISubVolumeIteratorStrategy {

  /**
   * Reads the next sub-volume.
   * 
   * @return the next sub-volume.
   */
  Trace[][][] next();

  /**
   * Returns <i>true</i> if done reading; <i>false</i> if not.
   * 
   * @return <i>true</i> if done reading; <i>false</i> if not.
   */
  boolean isDone();

  /**
   * Returns the iterator status message.
   * 
   * @return the iterator status message.
   */
  String getMessage();

  /**
   * Returns the iterator completion status (in the range 0-100).
   * 
   * @return the iterator completion status (in the range 0-100).
   */
  float getCompletion();
}
