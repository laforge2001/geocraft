/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.preferences;


import junit.framework.TestCase;


public class PropertyStoreTest extends TestCase {

  private final PropertyStore _testStore = PropertyStoreFactory.getStore("test_store");

  private static final String DEF_STRING = "defaultString";

  private static final int DEF_INT = 999;

  private static final long DEF_LONG = 123456789;

  private static final float DEF_FLOAT = 100.1f;

  private static final double DEF_DOUBLE = 100.111d;

  private static final boolean DEF_BOOLEAN = true;

  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    _testStore.setProjectSettings(true);
    _testStore.setDefault("testString", DEF_STRING);
    _testStore.setDefault("testInt", DEF_INT);
    _testStore.setDefault("testBool", DEF_BOOLEAN);
    _testStore.setDefault("testFloat", DEF_FLOAT);
    _testStore.setDefault("testDouble", DEF_DOUBLE);
    _testStore.setDefault("testLong", DEF_LONG);
  }

  /* (non-Javadoc)
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  /**
   * Test method for {@link org.geocraft.core.common.preferences.PropertyStore#setProjectSettings(boolean)}.
   */
  public void testSetProjectSettings() {
    _testStore.setProjectSettings(true);
    assertTrue(_testStore.useProjectSettings());

    _testStore.setProjectSettings(false);
    assertFalse(_testStore.useProjectSettings());
  }

  /**
   * Test method for {@link org.geocraft.core.common.preferences.PropertyStore#getDefaultString(java.lang.String)}.
   */
  public void testGetDefaultStringString() {
    _testStore.setProjectSettings(true);
    _testStore.setDefault("testString", DEF_STRING);
    assertEquals(DEF_STRING, _testStore.getDefaultString("testString"));

    _testStore.setProjectSettings(false);
    assertEquals(DEF_STRING, _testStore.getDefaultString("testString"));
  }

  /**
   * Test method for {@link org.geocraft.core.common.preferences.PropertyStore#getString(java.lang.String)}.
   */
  public void testGetStringString() {
    _testStore.setProjectSettings(true);
    _testStore.setValue("testString", "dooWop");
    assertEquals("dooWop", _testStore.getString("testString"));

    _testStore.setProjectSettings(false);
    assertEquals(DEF_STRING, _testStore.getString("testString"));
  }

  /**
   * Test method for {@link org.geocraft.core.common.preferences.PropertyStore#contains(java.lang.String)}.
   */
  public void testContainsString() {
    assertTrue(_testStore.contains("testString"));
    assertFalse(_testStore.contains(DEF_STRING));
  }

  /**
   * Test method for {@link org.geocraft.core.common.preferences.PropertyStore#getBoolean(java.lang.String)}.
   */
  public void testGetBooleanString() {
    _testStore.setValue("testBoolean", false);
    assertFalse(_testStore.getBoolean("testBoolean"));
  }

  /**
   * Test method for {@link org.geocraft.core.common.preferences.PropertyStore#getDefaultBoolean(java.lang.String)}.
   */
  public void testGetDefaultBooleanString() {
    assertEquals(_testStore.getDefaultBoolean("testBoolean"), _testStore.getBoolean("testBoolean"));
  }

  /**
   * Test method for {@link org.geocraft.core.common.preferences.PropertyStore#getDefaultDouble(java.lang.String)}.
   */
  public void testGetDefaultDoubleString() {
    assertEquals(_testStore.getDefaultDouble("testDouble"), _testStore.getDouble("testDouble"));
  }

  /**
   * Test method for {@link org.geocraft.core.common.preferences.PropertyStore#getDefaultFloat(java.lang.String)}.
   */
  public void testGetDefaultFloatString() {
    assertEquals(_testStore.getDefaultFloat("testFloat"), _testStore.getFloat("testFloat"));
  }

  /**
   * Test method for {@link org.geocraft.core.common.preferences.PropertyStore#getDefaultInt(java.lang.String)}.
   */
  public void testGetDefaultIntString() {
    assertEquals(_testStore.getDefaultInt("testInt"), _testStore.getInt("testInt"));
  }

  /**
   * Test method for {@link org.geocraft.core.common.preferences.PropertyStore#getDefaultLong(java.lang.String)}.
   */
  public void testGetDefaultLongString() {
    assertEquals(_testStore.getDefaultLong("testLong"), _testStore.getLong("testLong"));
  }

  /**
   * Test method for {@link org.geocraft.core.common.preferences.PropertyStore#getDouble(java.lang.String)}.
   */
  public void testGetDoubleString() {
    _testStore.setValue("testDouble", 999.099d);
    assertEquals(999.099d, _testStore.getDouble("testDouble"));
  }

  /**
   * Test method for {@link org.geocraft.core.common.preferences.PropertyStore#getFloat(java.lang.String)}.
   */
  public void testGetFloatString() {
    _testStore.setValue("testFloat", 999.099f);
    assertEquals(999.099f, _testStore.getFloat("testFloat"));
  }

  /**
   * Test method for {@link org.geocraft.core.common.preferences.PropertyStore#getInt(java.lang.String)}.
   */
  public void testGetIntString() {
    _testStore.setValue("testInt", 333);
    assertEquals(333, _testStore.getInt("testInt"));
  }

  /**
   * Test method for {@link org.geocraft.core.common.preferences.PropertyStore#getLong(java.lang.String)}.
   */
  public void testGetLongString() {
    _testStore.setValue("testLong", 333);
    assertEquals(333, _testStore.getLong("testLong"));
  }

  /**
   * Test method for {@link org.geocraft.core.common.preferences.PropertyStore#isDefault(java.lang.String)}.
   */
  public void testIsDefaultString() {
    _testStore.setToDefault("testString");
    assertTrue(_testStore.isDefault("testString"));
  }

  /**
   * Test method for {@link org.geocraft.core.common.preferences.PropertyStore#putValue(java.lang.String, java.lang.String)}.
   */
  public void testPutValueStringString() {
    _testStore.setProjectSettings(true);
    _testStore.putValue("testString", "dooWop");
    assertEquals("dooWop", _testStore.getString("testString"));

    _testStore.setProjectSettings(false);
    assertEquals("defaultString", _testStore.getString("testString"));
  }

  /**
   * Test method for {@link org.geocraft.core.common.preferences.PropertyStore#setToDefault(java.lang.String)}.
   */
  public void testSetToDefaultString() {
    _testStore.setProjectSettings(true);
    _testStore.putValue("testString", "dooWop");
    assertEquals("dooWop", _testStore.getString("testString"));

    _testStore.setToDefault("testString");
    assertEquals("defaultString", _testStore.getString("testString"));
  }

}
