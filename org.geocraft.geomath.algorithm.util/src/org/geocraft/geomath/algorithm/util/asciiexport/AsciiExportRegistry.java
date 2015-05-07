/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */

package org.geocraft.geomath.algorithm.util.asciiexport;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geocraft.core.service.ServiceProvider;


/**
 * Defines the registry for ASCII export strategies. Use of the registry allows for custom
 * ASCII export strategies to be added at startup and made available to any algorithms that
 * export trace data to ASCII files.
 */
public class AsciiExportRegistry {

  /** The singleton. */
  private static AsciiExportRegistry _instance;

  /** Collection of all the available ASCII export strategy classes. **/
  private final Map<String, Class> _map = new HashMap<String, Class>();

  private final List<String> _list = new ArrayList<String>();

  /**
   * Constructs the singleton.
   */
  private AsciiExportRegistry() {
    ServiceProvider.getLoggingService().getLogger(getClass()).info("Creating the ASCII export registry.");
  }

  /**
   * Returns the registry singleton.
   * @return the registry singleton.
   */
  public static AsciiExportRegistry getInstance() {
    if (_instance == null) {
      _instance = new AsciiExportRegistry();
    }
    return _instance;
  }

  /**
   * Registers the specified ASCII export strategy with the registry.
   * @param key the key by which the ASCII export strategy is registered.
   * @param exportStrategy the class of the ASCII export strategy to register.
   */
  public void register(final String key, final Class exportStrategy) {
    if (_map.get(key) == null) {
      _map.put(key, exportStrategy);
      _list.add(key);
    }
  }

  /**
   * Finds the ASCII export strategy registered by the specified key.
   * @param key the key by which to find the ASCII export strategy.
   * @return the class of the ASCII export strategy.
   */
  public Class find(final String key) {
    Class exportStrategy = _map.get(key);
    if (exportStrategy == null) {
      throw new IllegalArgumentException("No such export strategy exists '" + key + "'");
    }
    return exportStrategy;
  }

  /**
   * Returns an array of keys for the ASCII export strategies currently registered.
   * @return an array of keys for the ASCII export strategies currently registered.
   */
  public String[] getKeys() {
    return _list.toArray(new String[0]);
  }
}
