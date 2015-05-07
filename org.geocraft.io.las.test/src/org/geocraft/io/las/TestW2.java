/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.las;


import org.geocraft.core.common.util.Utilities;


public class TestW2 extends TestW1 {

  @Override
  public void testRead() {
    LasReader reader = new LasReader(Utilities.getPath("org.geocraft.io.las.test") + "data", "testw2.las");
    checkNull(reader, -999.25f);
    checkData(reader, 500.0244f, 620.1156f, 6259.3892f);
  }

}
