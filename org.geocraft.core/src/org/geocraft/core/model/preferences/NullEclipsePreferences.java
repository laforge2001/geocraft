package org.geocraft.core.model.preferences;


import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferenceNodeVisitor;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;


/**
 * Meant to encapsulate an EclipsePreferences object when there's no access
 * to the eclipse preference stores. 
 * @author georde
 *
 */
public class NullEclipsePreferences implements IEclipsePreferences {

  @Override
  public void put(final String key, final String value) {
    // TODO Auto-generated method stub

  }

  @Override
  public String get(final String key, final String def) {
    return def;
  }

  @Override
  public void remove(final String key) {
    // TODO Auto-generated method stub

  }

  @Override
  public void clear() throws BackingStoreException {
    // TODO Auto-generated method stub

  }

  @Override
  public void putInt(final String key, final int value) {
    // TODO Auto-generated method stub

  }

  @Override
  public int getInt(final String key, final int def) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void putLong(final String key, final long value) {
    // TODO Auto-generated method stub

  }

  @Override
  public long getLong(final String key, final long def) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void putBoolean(final String key, final boolean value) {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean getBoolean(final String key, final boolean def) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void putFloat(final String key, final float value) {
    // TODO Auto-generated method stub

  }

  @Override
  public float getFloat(final String key, final float def) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void putDouble(final String key, final double value) {
    // TODO Auto-generated method stub

  }

  @Override
  public double getDouble(final String key, final double def) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void putByteArray(final String key, final byte[] value) {
    // TODO Auto-generated method stub

  }

  @Override
  public byte[] getByteArray(final String key, final byte[] def) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String[] keys() throws BackingStoreException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String[] childrenNames() throws BackingStoreException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Preferences parent() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean nodeExists(final String pathName) throws BackingStoreException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public String name() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String absolutePath() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void flush() throws BackingStoreException {
    // TODO Auto-generated method stub

  }

  @Override
  public void sync() throws BackingStoreException {
    // TODO Auto-generated method stub

  }

  @Override
  public void addNodeChangeListener(final INodeChangeListener listener) {
    // TODO Auto-generated method stub

  }

  @Override
  public void removeNodeChangeListener(final INodeChangeListener listener) {
    // TODO Auto-generated method stub

  }

  @Override
  public void addPreferenceChangeListener(final IPreferenceChangeListener listener) {
    // TODO Auto-generated method stub

  }

  @Override
  public void removePreferenceChangeListener(final IPreferenceChangeListener listener) {
    // TODO Auto-generated method stub

  }

  @Override
  public void removeNode() throws BackingStoreException {
    // TODO Auto-generated method stub

  }

  @Override
  public Preferences node(final String path) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void accept(final IPreferenceNodeVisitor visitor) throws BackingStoreException {
    // TODO Auto-generated method stub

  }

}
