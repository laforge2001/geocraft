/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.algorithm;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;


public class AlgorithmUsageManager {

  public static List<IAlgorithmUsageListener> _usageListeners = new ArrayList<IAlgorithmUsageListener>();

  public static void addListener(IAlgorithmUsageListener listener) {
    if (!_usageListeners.contains(listener)) {
      _usageListeners.add(listener);
    }
  }

  public static void removeListener(IAlgorithmUsageListener listener) {
    _usageListeners.remove(listener);
  }

  public static void algorithmStarted(Object key, IStandaloneAlgorithmDescription algorithm) {
    for (IAlgorithmUsageListener listener : _usageListeners.toArray(new IAlgorithmUsageListener[0])) {
      listener.algorithmStarted(key, algorithm);
    }
  }

  public static void algorithmEnded(Object key, IStatus status) {
    for (IAlgorithmUsageListener listener : _usageListeners.toArray(new IAlgorithmUsageListener[0])) {
      listener.algorithmEnded(key, status);
    }
  }
}
