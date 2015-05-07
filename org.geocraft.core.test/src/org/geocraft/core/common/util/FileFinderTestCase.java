/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.core.common.util;


import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;


/**
 * Unit tests for the <code>FileFinder</code> class.
 */
public class FileFinderTestCase extends TestCase {

  /**
   * Unit test of searches for files, directories and both.
   */
  public void testFindMethods() {
    // Set the headless property.
    System.setProperty("java.awt.headless", "true");
    try {
      String cwd = Utilities.getWorkingDirectory();

      // Create a temporary file.
      File file = new File(cwd + File.separator + "abcdefghijklmnopqrstuvwxyz.txt");
      boolean fileOk = file.createNewFile();
      if (fileOk) {
        file.deleteOnExit();
      }

      // Create a temporary directory.
      File dir = new File(cwd + File.separator + "abcdefghijklmnopqrstuvwxyz.dir");
      boolean dirOk = dir.mkdir();
      if (dirOk) {
        dir.deleteOnExit();
      }
      if (fileOk && dirOk) {
        // Create a file finder.
        FileFinder finder = new FileFinder();

        // Search for files and directories matching the base name.
        FileFinderResult[] results0 = finder.find(cwd, "abcdefghijklmnopqrstuvwxyz*", false);
        assertEquals(2, results0.length);

        // Search for files ONLY matching the base name.
        FileFinderResult[] results1 = finder.findFiles(cwd, "abcdefghijklmnopqrstuvwxyz*", false);
        assertEquals(1, results1.length);
        assertEquals(file.getAbsolutePath(), results1[0].toString());

        // Search for directories ONLY matching the base name.
        FileFinderResult[] results2 = finder.findDirs(cwd, "abcdefghijklmnopqrstuvwxyz*", false);
        assertEquals(1, results2.length);
        assertEquals(dir.getAbsolutePath(), results2[0].toString());
      }
    } catch (IOException ex) {
      // If an exception is thrown, the test fails.
      fail(ex.toString());
    }
  }

  /**
   * Unit test of various string comparisons, including wild-cards.
   */
  public void testCompareStrings() {
    assertTrue(StringUtil.compareStrings("/home/walucas/test.segy", "/home/walucas/test.segy"));
    assertTrue(StringUtil.compareStrings("/home/walucas/test.segy", "*h*m*waluc*es*gy"));
    assertFalse(StringUtil.compareStrings("/home/walucas/test.segy", "*h*m*wal*x*c*es*gy"));
    assertTrue(StringUtil.compareStrings("/home/walucas/test.segy", "*test*"));
    assertFalse(StringUtil.compareStrings("/home/walucas/test.segy", "*test"));
    assertFalse(StringUtil.compareStrings("/home/walucas/test.segy", "test*"));
    assertTrue(StringUtil.compareStrings("/home/walucas/test.segy", "/home/*"));
    assertTrue(StringUtil.compareStrings("/home/walucas/test.segy", "*/home/*"));
    assertTrue(StringUtil.compareStrings("/home/walucas/test.segy", "*/h?me/*"));
    assertTrue(StringUtil.compareStrings("/home/walucas/test.segy", "*.*y"));
  }
}
