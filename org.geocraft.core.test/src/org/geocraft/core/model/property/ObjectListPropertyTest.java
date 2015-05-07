/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.property;


import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;


public class ObjectListPropertyTest extends TestCase {

  private class SomeObj {

    private String testStr;

    private int testInt;

    private boolean testBool;

    SomeObj() {
      super();
      testStr = "default";
      testInt = 999;
      testBool = false;
    }

    void setStr(String val) {
      testStr = val;
    }

    String getStr() {
      return testStr;
    }

    void setInt(int val) {
      testInt = val;
    }

    int getInt() {
      return testInt;
    }

    void setBool(boolean val) {
      testBool = val;
    }

    boolean getBool() {
      return testBool;
    }

    @Override
    public String toString() {
      return testStr;
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof SomeObj) {
        SomeObj t = (SomeObj) o;
        return t.getBool() == getBool() && t.getInt() == getInt() && t.getStr().equals(getStr());
      }
      return false;
    }
  }

  private class ListProperty<T> extends ObjectListProperty<T> {

    /**
     * @param key
     * @param klazz
     */
    public ListProperty(String key, Class<T> klazz) {
      super(key, klazz);
    }

    /* (non-Javadoc)
     * @see org.geocraft.core.model.property.Property#pickle()
     */
    @Override
    public String pickle() {
      // TODO Auto-generated method stub
      return null;
    }

    /* (non-Javadoc)
     * @see org.geocraft.core.model.property.Property#unpickle(java.lang.String)
     */
    @Override
    public void unpickle(@SuppressWarnings("unused") String value) {
      // TODO Auto-generated method stub

    }

  }

  private SomeObj testClass;

  private ListProperty<SomeObj> testProp;

  private List<SomeObj> testList;

  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    testClass = new SomeObj();
    testList = new ArrayList<SomeObj>();
    for (int i = 0; i < 5; ++i) {
      SomeObj t = new SomeObj();
      t.setInt(i);
      t.setStr("" + i);
      t.setBool(true);
      testList.add(t);
    }
    testProp = new ListProperty<SomeObj>("test string", SomeObj.class);
  }

  public void testSet() {
    testProp.set(testList);
    List<SomeObj> list = testProp.get();

    assertEquals(list.get(2).getBool(), testList.get(2).getBool());
  }

  public void testSetValueObject() {
    testProp.setValueObject(testList);
    List<SomeObj> list = testProp.get();

    assertEquals(list.get(2).getBool(), testList.get(2).getBool());
  }

  public void testAddRemove() {
    testProp.set(testList);
    testProp.add(testClass);
    assertEquals(testProp.get().size(), testList.size() + 1);

    testProp.remove(new SomeObj());
    assertEquals(testProp.get().size(), testList.size());
  }

}
