/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.defs;


public enum ClassificationMethod {

  CLASS_OF_SAMPLE("Class of Sample"), CLASS_OF_PEAK_TROUGH("Class of Peak/Trough");

  private String _text;

  ClassificationMethod(final String text) {
    _text = text;
  }

  public String getText() {
    return _text;
  }

  @Override
  public String toString() {
    return getText();
  }
}
