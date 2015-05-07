/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.property;


import junit.framework.TestCase;


public class EnumArrayPropertyTest extends TestCase {

  private enum TestType {
    ONE("test number one"),
    TWO("test number two");

    private final String _name;

    TestType(String name) {
      _name = name;
    }

    @Override
    public String toString() {
      return _name;
    }
  }

  private EnumArrayProperty<TestType> _prop;

  private TestType[] _array;

  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    _prop = new EnumArrayProperty<TestType>("test key", TestType.class);
    _array = new TestType[] { TestType.ONE, TestType.ONE, TestType.TWO };
  }

  /**
   * Test method for {@link org.geocraft.core.model.property.EnumArrayProperty#unpickle(java.lang.String)}.
   */
  public void testUnpickle() {
    _prop.set(_array);
    String pickled = _prop.pickle();
    assertEquals("test number one,test number one,test number two", pickled);

    EnumArrayProperty<TestType> newprop = new EnumArrayProperty<TestType>("test2", TestType.class);
    newprop.unpickle(pickled);
    TestType[] values = newprop.get();
    assertEquals(values[2], _array[2]);
  }

  /**
   * Test method for {@link org.geocraft.core.model.property.ObjectArrayProperty#getValueObject()}.
   */
  public void testGetValueObject() {
    _prop.set(_array);
    TestType[] values = (TestType[]) _prop.getValueObject();
    assertEquals(values[2], _array[2]);
  }

  /**
   * Test method for {@link org.geocraft.core.model.property.ObjectArrayProperty#get()}.
   */
  public void testGet() {
    _prop.set(_array);
    TestType[] vals = _prop.get();
    assertEquals(vals[2], _array[2]);
  }

  /**
   * Test method for {@link org.geocraft.core.model.property.ObjectArrayProperty#isEmpty()}.
   */
  public void testIsEmpty() {
    assertTrue(_prop.isEmpty());
    _prop.set(_array);
    assertFalse(_prop.isEmpty());
  }

  /**
   * Test method for {@link org.geocraft.core.model.property.ObjectArrayProperty#getKlazz()}.
   */
  public void testGetKlazz() {
    assertEquals(_prop.getKlazz(), TestType.class);
  }

}
