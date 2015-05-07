/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.core.color.map;


import org.eclipse.swt.graphics.RGB;


/**
 * A basic implementation of a 'rainbow' color map.
 */
public class RainbowColorMap extends AbstractColorMap {

  public RainbowColorMap() {
    super("Rainbow");
  }

  public RGB[] getRGBs(final int numColors) {
    RGB[] rgbs = new RGB[numColors];

    float[] rr = { 1, 0, 0, 0, 1, 1 };
    float[] gg = { 0, 0, 1, 1, 1, 0 };
    float[] bb = { 1, 1, 1, 0, 0, 0 };

    float step = numColors / 5.0f;
    float here = step;
    int level = 0;
    int ii = -1;
    for (int i = 0; i < numColors; i++) {
      ii++;
      float fi = i + 1;
      if (fi > here) {
        here += step;
        level++;
        ii = -1;
      }
      float rf = rr[level] + (rr[level + 1] - rr[level]) / step * ii;
      float gf = gg[level] + (gg[level + 1] - gg[level]) / step * ii;
      float bf = bb[level] + (bb[level + 1] - bb[level]) / step * ii;
      rf = Math.max(rf, 0);
      rf = Math.min(rf, 1);
      gf = Math.max(gf, 0);
      gf = Math.min(gf, 1);
      bf = Math.max(bf, 0);
      bf = Math.min(bf, 1);
      int r = (int) (rf * 255);
      int g = (int) (gf * 255);
      int b = (int) (bf * 255);
      rgbs[numColors - 1 - i] = new RGB(r, g, b);
    }
    return rgbs;
  }

}
