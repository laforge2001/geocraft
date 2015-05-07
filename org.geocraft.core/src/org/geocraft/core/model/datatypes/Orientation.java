/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.datatypes;


public enum Orientation {

  COLUMN("Column"), ROW("Row");

  private final String _title;

  Orientation(final String title) {
    _title = title;
  }

  @Override
  public String toString() {
    return getTitle();
  }

  public String getTitle() {
    return _title;
  }
}
