/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.util.volume;


/**
 * The interface for all PostStack3d iterator strategies.
 */
public interface IPostStack3dIteratorStrategy {

  /**
   * Reads the next block of traces.
   * <p>
   * The 1st index is the volume index.<br>
   * The 2nd index is the inline index.<br>
   * The 3rd index is the xline index.<br>
   * 
   * @return the next sub-volume.
   */
  TraceBlock3d next();

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
  int getCompletion();

  int getTotalWork();
}
