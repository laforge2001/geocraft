/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.core.common.util;


import java.io.File;
import java.sql.Timestamp;


/**
 * Defines a result returned by a <code>FileFinder</code> search.
 */
public class FileFinderResult {

  /** The entry name. */
  private final String _name;

  /** The entry path. */
  private final String _path;

  /** The entry owner. */
  private String _owner;

  /** The entry last modified date. */
  private final Timestamp _date;

  /** The entry size. */
  private final long _size;

  public FileFinderResult(final String fileName, final String filePath, final String fileOwner, final Timestamp fileDate, final long fileSize) {
    _name = fileName;
    _path = filePath;
    _owner = fileOwner;
    _date = fileDate;
    _size = fileSize;
  }

  @Override
  public boolean equals(final Object object) {
    if (object != null && object instanceof FileFinderResult) {
      FileFinderResult result = (FileFinderResult) object;
      if (_name.equals(result.getName()) && _path.equals(result.getPath())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    HashCode hashCode = new HashCode();
    hashCode.add(_path);
    hashCode.add(_name);
    return hashCode.getHashCode();
  }

  /**
   * Gets the entry name.
   * @return the entry name.
   */
  public String getName() {
    return _name;
  }

  /**
   * Gets the entry path.
   * @return the entry path.
   */
  public String getPath() {
    return _path;
  }

  /**
   * Gets the entry owner.
   * @return the entry owner.
   */
  public String getOwner() {
    return _owner;
  }

  /**
   * Sets the entry owner.
   * @param owner the entry owner.
   */
  public void setOwner(final String owner) {
    _owner = owner;
  }

  /**
   * Gets the entry date.
   * @return the entry date.
   */
  public Timestamp getDate() {
    return _date;
  }

  /**
   * Gets the entry size (in bytes).
   * @return the entry size (in bytes).
   */
  public long getSize() {
    return _size;
  }

  @Override
  public String toString() {
    return _path + File.separator + _name;
  }
}
