/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.core.common.io;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;


/**
 * Make java i/o as simple as Python. :-)
 */
public class TextFile extends ArrayList<String> {

  /** Initial size of array to save it having to resize too often. */
  private static int _initialSize = 100;

  /**
   * Initialize an empty TextFile.
   */
  public TextFile() {
    super(_initialSize);
  }

  /**
   * Initialize the TextFile by loading records from a file.
   * @param fileName Pathname of the file
   */
  public TextFile(String fileName) {

    this();
    read(fileName);
  }

  /**
   * Initialize the TextFile by loading records from a file.
   * @param fd Descriptor of the file
   */
  public TextFile(FileDescriptor fd) {
    this();
    read(fd);
  }

  /**
   * Read the file into the array and remove the end of line characters.
   */
  public void read(String fileName) {

    try {

      BufferedReader in = new BufferedReader(new FileReader(new File(fileName).getAbsoluteFile()));

      try {
        String line;

        while ((line = in.readLine()) != null) {
          add(line);
        }
      } finally {
        in.close();
      }

    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Read the file into the array and remove the end of line characters.
   */
  public void read(FileDescriptor fd) {

    try {

      BufferedReader in = new BufferedReader(new FileReader(fd));

      try {
        String line;

        while ((line = in.readLine()) != null) {
          add(line);
        }
      } finally {
        in.close();
      }

    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Save the array to the specified filename. Adds back the end of line
   * characters.
   */

  public void write(String fileName) {

    try {

      PrintWriter out = new PrintWriter(new File(fileName).getAbsoluteFile());

      try {
        for (String record : this) {
          out.println(record);
        }
      } finally {
        out.close();
      }
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }

  }

  /**
   * Put this in a method as a reminder that this is the idiomatic way to do
   * this.
   */
  public String[] getRecords() {
    return toArray(new String[size()]);
  }

}
