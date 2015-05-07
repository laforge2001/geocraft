/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.segy;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class BinGridDefinitionStanza {

  private Map<BinGridStanzaEntry, String> _map;

  public BinGridDefinitionStanza() {
    _map = Collections.synchronizedMap(new HashMap<BinGridStanzaEntry, String>());
  }

  public void put(BinGridStanzaEntry key, String value) {
    String trimmedValue = value;
    if (trimmedValue == null) {
      trimmedValue = "";
    } else if (trimmedValue.length() > 80) {
      trimmedValue = trimmedValue.substring(0, 80);
    }
    _map.put(key, trimmedValue);
  }

  public String get(BinGridStanzaEntry key) {
    String value = _map.get(key);
    if (value == null) {
      value = "";
    }
    return value;
  }
}
