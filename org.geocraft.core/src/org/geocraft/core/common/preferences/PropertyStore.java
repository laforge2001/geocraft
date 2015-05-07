/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.preferences;


import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.preferences.ScopedPreferenceStore;


public class PropertyStore extends PreferenceStore {

  public static final String USE_PROJECT_SETTINGS = "Use Project Settings";

  private final ScopedPreferenceStore _localStore;

  private final ScopedPreferenceStore _globalStore;

  private IPreferenceStore _activeStore;

  /**
   * This will create a preference store that can allow on the fly switching of scope. This is useful
   * for saving geocraft preferences
   * 
   * @param instanceStore store for session scoped preferences
   * @param workbenchStore preference store for global preferences
   */
  public PropertyStore(final IPreferenceStore instanceStore, final IPreferenceStore workbenchStore) {
    _localStore = (ScopedPreferenceStore) instanceStore;
    _globalStore = (ScopedPreferenceStore) workbenchStore;
    setProjectSettings(useProjectSettings());
  }

  /**
   * This will create a preference store that can allow on the fly switching of scope. This is useful
   * for saving geocraft preferences
   * 
   * @param instanceStore store for session scoped preferences
   * @param workbenchStore preference store for global preferences
   * @param useProjectSettings a flag to force the use or not of an instance scope (within a session)
   * or global scope (common among all sessions)
   */
  public PropertyStore(final IPreferenceStore instanceStore, final IPreferenceStore workbenchStore, final boolean useProjectSettings) {
    _localStore = (ScopedPreferenceStore) instanceStore;
    _globalStore = (ScopedPreferenceStore) workbenchStore;
    setProjectSettings(useProjectSettings);
  }

  public void setProjectSettings(final boolean flag) {
    if (flag) {
      _activeStore = _localStore;
      // Always save the project setting flag to the local store, so it will saved with sessions.
      _localStore.setValue(USE_PROJECT_SETTINGS, TRUE);
    } else {
      _activeStore = _globalStore;
      // Always save the project setting flag to the local store, so it will saved with sessions.
      _localStore.setValue(USE_PROJECT_SETTINGS, FALSE);
    }
  }

  public IPreferenceStore getLocalStore() {
    return _localStore;
  }

  public IPreferenceStore getActiveStore() {
    return _activeStore;
  }

  @Override
  public String getDefaultString(final String name) {
    return getActiveStore().getDefaultString(name);
  }

  @Override
  public String getString(final String name) {
    return getActiveStore().getString(name);
  }

  @Override
  public boolean contains(final String name) {
    return getActiveStore().contains(name);
  }

  @Override
  public boolean getBoolean(final String name) {
    return getActiveStore().getBoolean(name);
  }

  @Override
  public boolean getDefaultBoolean(final String name) {
    return getActiveStore().getDefaultBoolean(name);
  }

  @Override
  public double getDefaultDouble(final String name) {
    return getActiveStore().getDefaultDouble(name);
  }

  @Override
  public float getDefaultFloat(final String name) {
    return getActiveStore().getDefaultFloat(name);
  }

  @Override
  public int getDefaultInt(final String name) {
    return getActiveStore().getDefaultInt(name);
  }

  @Override
  public long getDefaultLong(final String name) {
    return getActiveStore().getDefaultLong(name);
  }

  @Override
  public double getDouble(final String name) {
    return getActiveStore().getDouble(name);
  }

  @Override
  public float getFloat(final String name) {
    return getActiveStore().getFloat(name);
  }

  @Override
  public int getInt(final String name) {
    return getActiveStore().getInt(name);
  }

  @Override
  public long getLong(final String name) {
    return getActiveStore().getLong(name);
  }

  @Override
  public boolean isDefault(final String name) {
    return getActiveStore().isDefault(name);

  }

  @Override
  public void save() {
    try {
      _globalStore.save();
      _localStore.save();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void setValue(final String key, final String value) {
    getActiveStore().setValue(key, value);
    super.setValue(key, value);
  }

  @Override
  public void addPropertyChangeListener(final IPropertyChangeListener listener) {
    _localStore.addPropertyChangeListener(listener);
    _globalStore.addPropertyChangeListener(listener);
  }

  @Override
  public void firePropertyChangeEvent(final String name, final Object oldValue, final Object newValue) {
    getActiveStore().firePropertyChangeEvent(name, oldValue, newValue);
  }

  @Override
  public boolean needsSaving() {
    return getActiveStore().needsSaving();
  }

  @Override
  public void removePropertyChangeListener(final IPropertyChangeListener listener) {
    _localStore.removePropertyChangeListener(listener);
    _globalStore.removePropertyChangeListener(listener);
  }

  @Override
  public void setDefault(final String name, final boolean value) {
    getActiveStore().setDefault(name, value);
  }

  @Override
  public void setDefault(final String name, final double value) {
    getActiveStore().setDefault(name, value);
  }

  @Override
  public void setDefault(final String name, final float value) {
    getActiveStore().setDefault(name, value);
  }

  @Override
  public void setDefault(final String name, final int value) {
    getActiveStore().setDefault(name, value);
  }

  @Override
  public void setDefault(final String name, final long value) {
    getActiveStore().setDefault(name, value);
  }

  @Override
  public void setDefault(final String name, final String value) {
    getActiveStore().setDefault(name, value);
  }

  @Override
  public void putValue(final String name, final String value) {
    getActiveStore().putValue(name, value);
  }

  @Override
  public void setValue(final String name, final boolean value) {
    getActiveStore().setValue(name, value);
  }

  @Override
  public void setValue(final String name, final double value) {
    getActiveStore().setValue(name, value);
  }

  @Override
  public void setValue(final String name, final float value) {
    getActiveStore().setValue(name, value);
  }

  @Override
  public void setValue(final String name, final int value) {
    getActiveStore().setValue(name, value);
  }

  @Override
  public void setValue(final String name, final long value) {
    getActiveStore().setValue(name, value);
  }

  @Override
  public void save(final OutputStream out, final String header) {
    try {
      _globalStore.save();
      _localStore.save();
      super.save();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void setToDefault(final String name) {
    setValue(name, getDefaultString(name));
  }

  public boolean useProjectSettings() {
    return TRUE.equals(_localStore.getString(USE_PROJECT_SETTINGS));

  }

  /**
   * Get the RGB color from its string representation
   * @param rgbColor RGB color. Format "RGB {int, int, int}" where each int is 0-255.
   * @return New RGB color instance or default color if bad data format or rgbColor null
   */
  public static RGB rgbValue(final String rgbColor) {
    RGB color = null;
    try {
      int idx1 = rgbColor.indexOf('{');
      int idx2 = rgbColor.indexOf('}');
      color = StringConverter.asRGB(rgbColor.substring(idx1 + 1, idx2), null);
    } catch (Exception dfe) {
      return PreferenceConverter.COLOR_DEFAULT_DEFAULT;
    }
    if (color == null) {
      return PreferenceConverter.COLOR_DEFAULT_DEFAULT;
    }
    return color;
  }
}
