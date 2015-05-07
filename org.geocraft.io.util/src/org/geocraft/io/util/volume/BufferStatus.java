/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.util.volume;


public enum BufferStatus {
  FILLING_UP("Filling up"),
  STATIC("Static"),
  RUNNING_OUT("Running out");

  private String _text;

  BufferStatus(String text) {
    _text = text;
  }

  @Override
  public String toString() {
    return _text;
  }
}
