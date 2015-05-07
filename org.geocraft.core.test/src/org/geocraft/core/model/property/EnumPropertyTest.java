/**
 * 
 */
package org.geocraft.core.model.property;


import junit.framework.TestCase;


public class EnumPropertyTest extends TestCase {

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

  private EnumProperty<TestType> enumProp;

  /**
   * @throws java.lang.Exception
   */
  @Override
  protected void setUp() throws Exception {

    enumProp = new EnumProperty<TestType>("TestKey", TestType.class);
  }

  /**
   * Test method for
   * {@link org.geocraft.core.model.property.EnumProperty#setValueObject(java.lang.Object)}
   * .
   */
  public void testSetValueObject() {
    enumProp.setValueObject(TestType.ONE);
    assertEquals(TestType.ONE, enumProp.get());

    enumProp.setValueObject(TestType.ONE.toString());
    assertEquals(TestType.ONE, enumProp.get());
  }

  /**
   * Test method for
   * {@link org.geocraft.core.model.property.ObjectProperty#getValueObject()}.
   */
  public void testGetValueObject() {
    enumProp.set(TestType.ONE);
    assertEquals(TestType.ONE, enumProp.getValueObject());
    assertTrue(enumProp.getValueObject() instanceof TestType);
  }

  /**
   * Test method for
   * {@link org.geocraft.core.model.property.ObjectProperty#get()}.
   */
  public void testGet() {
    testSetValueObject();
  }

  /**
   * Test method for
   * {@link org.geocraft.core.model.property.ObjectProperty#set(java.lang.Object)}
   * .
   */
  public void testSet() {
    testGetValueObject();
  }

  public void testUnpickle() {
    enumProp.set(TestType.ONE);
    String pickled = enumProp.pickle();

    EnumProperty<TestType> newProp = new EnumProperty<TestType>("test key", TestType.class);
    newProp.unpickle(pickled);

    assertEquals(newProp.get(), TestType.ONE);
  }

}
