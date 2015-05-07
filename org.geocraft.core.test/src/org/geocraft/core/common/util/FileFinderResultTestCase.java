/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.core.common.util;


import java.io.File;
import java.sql.Timestamp;

import junit.framework.TestCase;


/**
 * Unit tests for the <code>FileFinderResult<code> class.
 */
public class FileFinderResultTestCase extends TestCase {

  /** The file path to use for testing. */
  private static final String FILE_PATH = File.separator + "temp" + File.separator + "data";

  /**
   * Test the get() methods of the file finder result.
   */
  public void testFileFinderResult() {
    // Create a file finder result.
    FileFinderResult result = new FileFinderResult("test.segy", FILE_PATH, "johndoe", new Timestamp(123456), 147);

    // Test the various get methods.
    assertEquals("test.segy", result.getName());
    assertEquals(FILE_PATH, result.getPath());
    assertEquals("johndoe", result.getOwner());
    assertEquals(new Timestamp(123456).toString(), result.getDate().toString());
    assertEquals(147, result.getSize());
    assertEquals(FILE_PATH + File.separator + "test.segy", result.toString());
  }

}
