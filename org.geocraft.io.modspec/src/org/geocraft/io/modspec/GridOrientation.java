/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.modspec;


public enum GridOrientation {

  /** The constant for x=column, y=row. */
  X_IS_COLUMN("X->Column,Y->Row"),

  /** The constant for y=column, x=row. */
  Y_IS_COLUMN("X->Row,Y->Column");

  String _text;

  GridOrientation(String text) {
    _text = text;
  }

  @Override
  public String toString() {
    return _text;
  }
}
