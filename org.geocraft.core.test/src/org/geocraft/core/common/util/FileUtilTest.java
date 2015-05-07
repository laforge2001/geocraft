/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.util;


import java.io.File;

import junit.framework.TestCase;


public class FileUtilTest extends TestCase {

  private String _testString;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    _testString = Utilities.getPath("org.geocraft.core.test") + "data" + File.separator + "test.txt";
    new File(_testString);
  }

  /**
   * Test method for
   * {@link org.geocraft.core.common.util.FileUtil#getFileExtension(java.lang.String)}
   * .
   */
  public void testGetFileExtension() {
    String fileExtension = FileUtil.getFileExtension(_testString);
    assertEquals(".txt", fileExtension);
  }

  /**
   * Test method for
   * {@link org.geocraft.core.common.util.FileUtil#getShortName(java.lang.String)}
   * .
   */
  public void testGetShortName() {
    String shortName = FileUtil.getShortName(_testString);
    assertEquals("test.txt", shortName);
  }

  /**
   * Test method for
   * {@link org.geocraft.core.common.util.FileUtil#getBaseName(java.lang.String)}
   * .
   */
  public void testGetBaseName() {
    String baseName = FileUtil.getBaseName(_testString);
    assertEquals("test", baseName);
  }

  /**
   * Test method for
   * {@link org.geocraft.core.common.util.FileUtil#getPathName(java.lang.String)}
   * .
   */
  public void testGetPathName() {
    String pathName = FileUtil.getPathName(_testString);
    assertEquals(_testString.substring(0, _testString.lastIndexOf(File.separator)), pathName);
  }

}
