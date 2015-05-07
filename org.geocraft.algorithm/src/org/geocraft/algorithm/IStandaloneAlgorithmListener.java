/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.algorithm;


/**
 * The interface for algorithm listeners.
 * A algorithm listener will be notified when a algorithm has been updated.
 */
public interface IStandaloneAlgorithmListener {

  /**
   * Invoked when the algorithm is updated.
   * @param algorithm the algorithm that was updated.
   */
  void algorithmUpdated(StandaloneAlgorithm algorithm);
}
