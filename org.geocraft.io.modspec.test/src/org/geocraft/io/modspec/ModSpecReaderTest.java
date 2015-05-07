package org.geocraft.io.modspec;


import java.io.File;

import junit.framework.TestCase;

import org.geocraft.core.common.util.Utilities;


public class ModSpecReaderTest extends TestCase {

  public void testFile() {
    String path = Utilities.getPath("org.geocraft.io.modspec.test");
    File file = new File(path + "data" + File.separator + "TopSaltTime.grid");
    assertTrue(file.exists());
    assertTrue(file.canRead());
  }

}
