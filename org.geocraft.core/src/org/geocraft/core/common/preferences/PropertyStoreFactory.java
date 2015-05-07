/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.preferences;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;


public class PropertyStoreFactory {

  private final static Map<String, PropertyStore> _propStore = new HashMap<String, PropertyStore>();

  private PropertyStoreFactory() {
    // intentionally null
  }

  /**
   * This will retrieve the property store from an internal map of stores and keys. If it doesn't exist 
   * one will be created 
   * 
   * @param key the key of the preference store
   * @return the property store
   */
  public static PropertyStore getStore(final String key) {
    PropertyStore returnMe = _propStore.get(key);
    if (returnMe == null) {
      returnMe = new PropertyStore(new ScopedPreferenceStore(new InstanceScope(), key), new ScopedPreferenceStore(
          new ConfigurationScope(), key));
      _propStore.put(key, returnMe);
    }
    return returnMe;
  }

  /**
   * This will retrieve the property store from an internal map of stores and keys. If it doesn't exist 
   * one will be created 
   * 
   * @param key the key of the preference store
   * @param useProjectSettings flag to force the scope of the preference store - not used if the property store has already been created
   * @return the property store
   */
  public static PropertyStore getStore(final String key, final boolean useProjectSettings) {
    PropertyStore returnMe = _propStore.get(key);
    if (returnMe == null) {
      returnMe = new PropertyStore(new ScopedPreferenceStore(new InstanceScope(), key), new ScopedPreferenceStore(
          new ConfigurationScope(), key), useProjectSettings);
      _propStore.put(key, returnMe);
    }
    return returnMe;
  }

  public static void saveAll() {
    List<Entry> entries = new ArrayList<Entry>();
    synchronized (_propStore) {
      entries.addAll(_propStore.entrySet());
    }

    for (Entry entry : entries) {
      ((PropertyStore) entry.getValue()).save();
    }
  }
}
