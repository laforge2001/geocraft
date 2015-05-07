/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.modspec;


public enum GridFileFormat {
  /** The constant for the ASCII format. */
  ASCII("ASCII"),

  /** The constant for the binary format. */
  BINARY("Binary");

  String _text;

  GridFileFormat(String text) {
    _text = text;
  }

  @Override
  public String toString() {
    return _text;
  }
}
