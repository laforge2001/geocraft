/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.abavo;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.geocraft.abavo.crossplot.IABavoCrossplot;


public class ABavoCrossplotRegistry {

  private static ABavoCrossplotRegistry _collection;

  private final Map<String, IABavoCrossplot> _crossplots;

  public static ABavoCrossplotRegistry get() {
    if (_collection == null) {
      _collection = new ABavoCrossplotRegistry();
    }
    return _collection;
  }

  private ABavoCrossplotRegistry() {
    _crossplots = Collections.synchronizedMap(new HashMap<String, IABavoCrossplot>());
  }

  public String[] getCrossplotKeys() {
    return _crossplots.keySet().toArray(new String[0]);
  }

  //  public IABavoCrossplot getCrossplot(final String key) {
  //    return _crossplots.get(key);
  //  }

  public IABavoCrossplot[] getCrossplots() {
    return _crossplots.values().toArray(new IABavoCrossplot[0]);
  }

  public void addCrossplot(final String key, final IABavoCrossplot crossplot) {
    _crossplots.put(key, crossplot);
  }

  public boolean containsKey(final String key) {
    return _crossplots.containsKey(key);
  }
}
