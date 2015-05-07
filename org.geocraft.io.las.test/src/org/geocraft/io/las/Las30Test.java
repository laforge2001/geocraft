/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.las;


import junit.framework.TestCase;

import org.geocraft.core.common.util.Utilities;


/**
 * Won't be able to handle all of this file just yet but
 * it should at least not crash and return the stuff it
 * does understand. 
 */
public class Las30Test extends TestCase {

  public void testRead() {
    LasReader reader = new LasReader(Utilities.getPath("org.geocraft.io.las.test") + "data", "LAS30.las");

    assertEquals(1670.0000f, reader.getDataRange()[0]);
    assertEquals(1669.750f, reader.getDataRange()[1]);
    assertEquals(-0.1250f, reader.getDataRange()[2]);
    assertEquals(-999.25f, reader.getNullValue());
    assertEquals("ANY OIL COMPANY INC.", reader.getCompany());
    assertEquals("ANY ET AL 12-34-12-34", reader.getWellName());
    assertEquals("WILDCAT", reader.getField());
    assertEquals("12-34-12-34W5M", reader.getLocation());
    assertEquals("ALBERTA", reader.getProvince());
    assertEquals("ANY LOGGING COMPANY INC.", reader.getServiceCompany());
    assertEquals("13/12/1986", reader.getServiceDate());
    assertEquals("100123401234W500", reader.getUwi());
    assertEquals("12345678", reader.getApi());
    assertEquals(34.56789, reader.getLatitude());
    assertEquals(-102.34567, reader.getLongitude());
  }
}
