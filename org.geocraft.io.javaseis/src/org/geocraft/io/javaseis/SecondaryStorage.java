/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.javaseis;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SecondaryStorage {

  private static List<String> _directoryList = Collections.synchronizedList(new ArrayList<String>());

  public static synchronized String[] get() {
    String[] locs = new String[_directoryList.size()];
    for (int i = 0; i < locs.length; i++) {
      locs[i] = _directoryList.get(i);
    }
    return locs;
  }

  public static synchronized void add(String directory) {
    if (!_directoryList.contains(directory)) {
      _directoryList.add(directory);
    }
  }

  public static synchronized void remove(String directory) {
    _directoryList.remove(directory);
  }
}
