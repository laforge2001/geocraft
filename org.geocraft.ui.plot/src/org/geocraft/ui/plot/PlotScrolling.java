/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot;


public enum PlotScrolling {
  NONE,
  VERTICAL_ONLY,
  HORIZONTAL_ONLY,
  BOTH;

  public boolean hasHorizontal() {
    return this == HORIZONTAL_ONLY || this == BOTH;
  }

  public boolean hasVertical() {
    return this == VERTICAL_ONLY || this == BOTH;
  }
}
