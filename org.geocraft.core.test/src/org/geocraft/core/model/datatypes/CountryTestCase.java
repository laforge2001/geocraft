/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.datatypes;


import junit.framework.TestCase;


/**
 * Unit tests for the <code>Country</code> enumeration.
 */
public class CountryTestCase extends TestCase {

  public void testName() {
    String description = "" + Country.AD;
    assertEquals("AD Andorra", description);
    assertEquals(Country.US, Country.valueOf("US"));
    for (Country country : Country.values()) {
      assertEquals(country, Country.lookupByCode(country.getCode()));
    }
  }
}
