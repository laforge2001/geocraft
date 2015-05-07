/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.algorithm;


import org.eclipse.core.runtime.IStatus;


public interface IAlgorithmUsageListener {

  void algorithmStarted(Object key, IStandaloneAlgorithmDescription algorithm);

  void algorithmEnded(Object key, IStatus status);
}
