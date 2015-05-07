/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */
package org.geocraft.core.common.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;


/**
 * Common utilities for working with files.
 */
public class FileUtil {

  /**
   * The constructor.
   */
  private FileUtil() {
    // intentionally left blank
  }

  /**
   * Copies the contents of a stream to another.
   * 
   * @param in
   *                the input stream to be copied
   * @param out
   *                the output stream to which the copy is done
   * @throws IOException
   *                 if a I/O problem occurs
   */
  public static void copyStream(final InputStream in, final OutputStream out) throws IOException {
    int bytesRead;
    byte[] b = new byte[10000];
    while ((bytesRead = in.read(b)) != -1) {
      out.write(b, 0, bytesRead);
    }
    out.flush();
  }

  /**
   * By default File#delete fails for non-empty directories, it works like "rm". 
   * We need something a little more brutual - this does the equivalent of "rm -r"
   * @param path Root File Path
   * @return true iff the file and all sub files/directories have been removed
   * @throws FileNotFoundException
   */
  public static boolean deleteRecursive(final File path) throws FileNotFoundException {
    if (!path.exists())
      throw new FileNotFoundException(path.getAbsolutePath());
    boolean ret = true;
    if (path.isDirectory()) {
      for (File f : path.listFiles()) {
        ret = ret && FileUtil.deleteRecursive(f);
      }
    }
    return ret && path.delete();
  }

  /**
   * Extract the files from the specified archive to the specified directory.
   * 
   * @param dir
   *                the directory to which the files are unzipped
   * @param zipFile
   *                the zip file
   */
  public static void unzipFile(final File dir, final File zipFile) {
    ILogger logger = ServiceProvider.getLoggingService().getLogger(FileUtil.class);
    try {
      dir.mkdirs();
      JarFile file = new JarFile(zipFile);
      logger.info("Extracting from file " + zipFile);
      Enumeration<JarEntry> entries = file.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        if (entry.getName().indexOf("META-INF") < 0) {
          logger.info("Extracting " + entry.getName());
          if (entry.isDirectory()) {
            new File(dir, entry.getName()).mkdirs();
          } else {
            copyStream(file.getInputStream(entry), new FileOutputStream(new File(dir, entry.getName())));
          }
        }
      }
    } catch (IOException ex) {
      logger.warn("Unable to extract files from " + zipFile.getPath(), ex);
    }
  }

  /**
   * Gets the extension of a file (e.g. for "text.segy", the extension is ".segy").
   * 
   * @param name
   *                the file name.
   * @return the extension name.
   */
  public static String getFileExtension(final String name) {
    if (name.lastIndexOf(".") > -1) {
      return name.substring(name.lastIndexOf("."));
    }
    return name;
  }

  /**
   * Return all the files in a directory that end with the specified extension.
   * 
   * @param dirName
   *                the directory to search
   * @param extension
   *                the files extension
   * @return list of Java files
   */
  public static List<String> findFiles(final String dirName, final String extension) {
    File dir = new File(dirName);
    if (!dir.exists()) {
      ServiceProvider.getLoggingService().getLogger(FileUtil.class).error("Directory does not exist: " + dirName);
    } else if (!dir.canRead()) {
      ServiceProvider.getLoggingService().getLogger(FileUtil.class).error("Cannot read directory: " + dirName);
    }
    String[] fileNames = {};
    List<String> files = new ArrayList<String>();
    if (dir.exists() && dir.canRead() && dir.isDirectory()) {
      fileNames = dir.list();
    }
    for (String fileName : fileNames) {
      if (fileName.endsWith(extension)) {
        String name = fileName.substring(0, fileName.length() - extension.length());
        files.add(name);
      }
    }
    return files;
  }

  /**
   * Gets the short name of a file (strips off the full path).
   * 
   * @param name
   *                the full path name.
   * @return the short name.
   */
  public static String getShortName(final String name) {
    if (name.lastIndexOf(File.separator) > -1) {
      return name.substring(name.lastIndexOf(File.separator) + 1);
    }
    return name;
  }

  /**
   * Gets the base name of a file (e.g. for "test.segy", the base name is "test").
   * 
   * @param name
   *                the file name.
   * @return the base name.
   */
  public static String getBaseName(final String name) {
    String tempname = name;
    if (tempname.lastIndexOf(File.separator) > -1) {
      tempname = tempname.substring(tempname.lastIndexOf(File.separator) + 1);
    }
    if (tempname.lastIndexOf(".") > -1) {
      return tempname.substring(0, tempname.lastIndexOf("."));
    }
    return tempname;
  }

  /**
   * Gets the path to the file name
   * 
   * @param name
   *                eg /foo/bar/baz.segy returns /foo/bar
   * @return the path to the file name.
   */
  // TODO: this test is incorrect if a not file separators exist.
  public static String getPathName(final String name) {
    if (name.lastIndexOf(File.separator) > -1) {
      return name.substring(0, name.lastIndexOf(File.separator));
    }
    return name;
  }

  public static void copy(final String fromFileName, final String toFileName) throws IOException {

    ILogger logger = ServiceProvider.getLoggingService().getLogger(FileUtil.class);

    File fromFile = new File(fromFileName);
    File toFile = new File(toFileName);

    if (!fromFile.exists()) {
      throw new IOException("FileCopy: " + "no such source file: " + fromFileName);
    }
    if (!fromFile.isFile()) {
      throw new IOException("FileCopy: " + "can't copy directory: " + fromFileName);
    }
    if (!fromFile.canRead()) {
      throw new IOException("FileCopy: " + "source file is unreadable: " + fromFileName);
    }

    if (toFile.isDirectory()) {
      toFile = new File(toFile, fromFile.getName());
    }

    if (toFile.exists()) {
      if (!toFile.canWrite()) {
        throw new IOException("FileCopy: " + "destination file is unwriteable: " + toFileName);
      }
    } else {
      String parent = toFile.getParent();
      if (parent == null) {
        parent = System.getProperty("user.dir");
      }
      File dir = new File(parent);
      if (!dir.exists()) {
        throw new IOException("FileCopy: " + "destination directory doesn't exist: " + parent);
      }
      if (dir.isFile()) {
        throw new IOException("FileCopy: " + "destination is not a directory: " + parent);
      }
      if (!dir.canWrite()) {
        throw new IOException("FileCopy: " + "destination directory is unwriteable: " + parent);
      }
    }

    FileInputStream from = null;
    FileOutputStream to = null;
    try {
      from = new FileInputStream(fromFile);
      to = new FileOutputStream(toFile);
      copyStream(from, to);
    } finally {
      if (from != null) {
        try {
          from.close();
        } catch (IOException e) {
          logger.error("error closing source file", e);
        }
      }
      if (to != null) {
        try {
          to.close();
        } catch (IOException e) {
          logger.error("error closing destination file", e);
        }
      }
    }
  }
}
