/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.preferences;


import junit.framework.TestCase;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;


public class GeocraftPreferenceServiceTest extends TestCase {

  private final static String ID = "testMe";

  private final GeocraftPreferenceService prefs = PreferencesUtil.getService(ID);

  private final IPreferenceStore _store = new PropertyStore(new ScopedPreferenceStore(new InstanceScope(), ID),
      new ScopedPreferenceStore(new ConfigurationScope(), ID));

  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    _store.setValue("testBoolean", true);
    _store.setValue("testString", "stringy");
    _store.setValue("testInt", 333);
  }

  /* (non-Javadoc)
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  /**
   * Test method for {@link org.geocraft.core.common.preferences.GeocraftPreferenceService#get(java.lang.String, java.lang.String)}.
   */
  public void testGet() {
    assertFalse("stringy".equals(prefs.get("testBoolean", "stringy")));
    assertTrue("stringy".equals(prefs.get("testString", "boop")));
  }

  /**
   * Test method for {@link org.geocraft.core.common.preferences.GeocraftPreferenceService#getBoolean(java.lang.String, boolean)}.
   */
  public void testGetBoolean() {
    assertEquals(true, prefs.getBoolean("testBoolean", false));
  }

  /**
   * Test method for {@link org.geocraft.core.common.preferences.GeocraftPreferenceService#getInt(java.lang.String, int)}.
   */
  public void testGetInt() {
    assertEquals(333, prefs.getInt("testInt", 444));
  }

  /**
   * Test method for {@link org.geocraft.core.common.preferences.GeocraftPreferenceService#setDefault(java.lang.String, java.lang.String)}.
   */
  public void testSetDefaultStringString() {
    assertEquals("default", prefs.get("testDefString", "default"));
    assertFalse("foo".equals(prefs.get("testDefString", "default")));
    assertTrue("foo".equals(prefs.get("randomString", "foo")));
  }

  /**
   * Test method for {@link org.geocraft.core.common.preferences.GeocraftPreferenceService#setDefault(java.lang.String, int)}.
   */
  public void testSetDefaultStringInt() {
    assertEquals(9, prefs.getInt("testDefInt", 9));
  }

  /**
   * Test method for {@link org.geocraft.core.common.preferences.GeocraftPreferenceService#setDefault(java.lang.String, boolean)}.
   */
  public void testSetDefaultStringBoolean() {
    assertEquals(false, prefs.getBoolean("testDefBool", false));
    assertEquals(true, prefs.getBoolean("testBoolean", false));
  }

}
