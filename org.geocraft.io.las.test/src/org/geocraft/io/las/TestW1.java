/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.las;


import junit.framework.TestCase;

import org.geocraft.core.common.util.Utilities;


public class TestW1 extends TestCase {

  public void testRead() {
    LasReader reader = new LasReader(Utilities.getPath("org.geocraft.io.las.test") + "data", "testw1.las");
    checkNull(reader, -999.25f);
    checkData(reader, 1900f, 1950f, 0.0062f);
  }

  public void checkNull(LasReader reader, float nullValue) {
    assertEquals(nullValue, reader.getNullValue());
  }

  public void checkData(LasReader reader, float firstDepth, float lastDepth, float lastLog) {

    assertEquals(firstDepth, reader.getRawData()[0][0]);
    assertEquals(lastDepth, reader.getRawData()[0][reader.getExpectedNumberOfSamples() - 1]);
    assertEquals(lastLog,
        reader.getRawData()[reader.getColumnNames().length - 1][reader.getExpectedNumberOfSamples() - 1]);
  }

}
