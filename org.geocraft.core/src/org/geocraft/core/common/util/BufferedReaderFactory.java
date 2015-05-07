/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.util;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;


public class BufferedReaderFactory {

  public static BufferedReader getBufferedReader(final File file) throws FileNotFoundException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
    return reader;
  }

  public static BufferedReader getBufferedReader(final String filePath) throws FileNotFoundException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath))));
    return reader;
  }

}
