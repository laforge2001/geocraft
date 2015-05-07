/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.core.color.map;


import org.eclipse.swt.graphics.RGB;


/**
 * A basic implementation of a 'seismic' color map.
 * The colors go from blue->white->red.
 */
public class SeismicColorMap extends AbstractColorMap {

  public static final String COLOR_MAP_NAME = "Seismic";

  public SeismicColorMap() {
    super(COLOR_MAP_NAME);
  }

  public RGB[] getRGBs(final int numColors) {
    RGB[] rgb = new RGB[numColors];
    float factor = (float) (256 / (numColors / 2.0));
    int red;
    int green;
    int blue;
    for (int i = 0; i < numColors; i++) {
      if (i < numColors / 2) {
        blue = 255;
        green = (int) (i * factor);
        red = (int) (i * factor);
      } else {
        int j = i - numColors / 2;
        blue = 255 - (int) (j * factor);
        green = 255 - (int) (j * factor);
        red = 255;
      }
      rgb[i] = new RGB(red, green, blue);
    }
    return rgb;
  }
}
