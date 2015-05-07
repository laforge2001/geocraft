/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.core.color.map;


import org.eclipse.swt.graphics.RGB;


/**
 * A basic implementation of a 'grayscale' color map.
 */
public class GrayscaleColorMap extends AbstractColorMap {

  public static final String COLOR_MAP_NAME = "Grayscale";

  public GrayscaleColorMap() {
    super(COLOR_MAP_NAME);
  }

  public RGB[] getRGBs(final int numColors) {
    RGB[] rgbs = new RGB[numColors];
    for (int i = 0; i < numColors; i++) {
      int col = (int) (255 * i / (float) (numColors - 1));
      rgbs[i] = new RGB(col, col, col);
    }
    return rgbs;
  }

}
