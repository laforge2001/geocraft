/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo;


public enum OutputVolumeType {
  INTEGER_08("8-bit integer"), INTEGER_16("16-bit integer"), FLOAT_32("32-bit float");

  private String _name;

  OutputVolumeType(final String name) {
    _name = name;
  }

  @Override
  public String toString() {
    return _name;
  }
}
