/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.preferences;


import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;


//adapter class for simplifying use of preference service
public class GeocraftPreferenceService {

  private final PropertyStore _store;

  private final String _bundleId;

  public GeocraftPreferenceService(final String bundleId) {
    _bundleId = bundleId;
    _store = PropertyStoreFactory.getStore(_bundleId);
  }

  public String get(final String key, final String defaultValue) {
    setDefault(key, defaultValue);
    return _store.getString(key);
  }

  public boolean getBoolean(final String key, final boolean defaultValue) {
    setDefault(key, defaultValue);
    return _store.getBoolean(key);
  }

  public int getInt(final String key, final int defaultValue) {
    setDefault(key, defaultValue);
    return _store.getInt(key);
  }

  public void setDefault(final String key, final String defaultValue) {
    _store.setDefault(key, defaultValue);
  }

  public void setDefault(final String key, final int defaultValue) {
    _store.setDefault(key, defaultValue);
  }

  public void setDefault(final String key, final boolean defaultValue) {
    _store.setDefault(key, defaultValue);
  }

  public void setDefault(final String key, final RGB rgbValue) {
    PreferenceConverter.setDefault(_store, key, rgbValue);
  }

}
