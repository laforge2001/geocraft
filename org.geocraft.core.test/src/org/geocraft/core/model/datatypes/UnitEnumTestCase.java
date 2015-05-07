/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.datatypes;


import java.util.Arrays;

import junit.framework.TestCase;


public class UnitEnumTestCase extends TestCase {

  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  /**
   * Test method for {@link org.geocraft.core.model.datatypes.Unit#getListOfNames(boolean)}.
   */
  public void testGetListOfNamesBoolean() {
    String[] namesList = Unit.getListOfNames();
    String[] namesListTrue = Unit.getListOfAllNames();

    assertEquals(namesList.length + 1, namesListTrue.length);
    assertTrue(namesList.length > 1);
    assertFalse(Arrays.asList(namesList).contains(Unit.UNDEFINED.toString()));
    assertTrue(Arrays.asList(namesListTrue).contains(Unit.UNDEFINED.toString()));

  }

  public void testLookupBySymbol() {
    assertEquals(Unit.lookupBySymbol("m"), Unit.METER);
  }
}
