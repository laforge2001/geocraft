/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.las;


import org.geocraft.core.common.util.Utilities;


public class TestW3 extends TestW1 {

  @Override
  public void testRead() {
    LasReader reader = new LasReader(Utilities.getPath("org.geocraft.io.las.test") + "data", "testw3.las");
    checkNull(reader, -999.25f);
    checkData(reader, 3000f, 3010f, -999.25f);
  }

}
