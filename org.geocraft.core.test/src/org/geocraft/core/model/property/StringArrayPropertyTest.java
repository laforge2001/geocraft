/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.property;


import junit.framework.TestCase;


public class StringArrayPropertyTest extends TestCase {

  private StringArrayProperty _prop;

  private String[] _array;

  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    _prop = new StringArrayProperty("test key", String.class);
    _array = new String[] { "apple", "banana", "orange" };
  }

  /**
   * Test method for {@link org.geocraft.core.model.property.StringArrayProperty#unpickle(java.lang.String)}.
   */
  public void testUnpickle() {
    _prop.set(_array);
    String pickled = _prop.pickle();
    assertEquals("apple,banana,orange", pickled);

    StringArrayProperty newProp = new StringArrayProperty("test key2", String.class);
    newProp.unpickle(pickled);
    String[] values = newProp.get();
    assertEquals(values[1], "banana");
  }

  /**
   * Test method for {@link org.geocraft.core.model.property.StringArrayProperty#contains(java.lang.String)}.
   */
  public void testContains() {
    _prop.set(_array);
    assertTrue(_prop.contains("apple"));
    assertFalse(_prop.contains("mango"));
  }

  /**
   * Test method for {@link org.geocraft.core.model.property.StringArrayProperty#add(java.lang.String)}.
   */
  public void testAdd() {
    _prop.set(_array);
    _prop.add("mango");
    String[] values = _prop.get();
    assertEquals(values.length, 4);
    assertTrue(_prop.contains("mango"));

  }

  /**
   * Test method for {@link org.geocraft.core.model.property.ObjectArrayProperty#getValueObject()}.
   */
  public void testGetValueObject() {
    _prop.set(_array);
    Object obj = _prop.getValueObject();
    assertTrue(obj.getClass().isArray());
    assertTrue(obj.getClass().isAssignableFrom(String[].class));
  }

}
