/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.core.color.map;


import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.color.ColorUtil;


/**
 * A basic implementation of a 'spectrum' color map.
 */
public class SpectrumColorMap extends AbstractColorMap {

  public static final String COLOR_MAP_NAME = "Spectrum";

  public SpectrumColorMap() {
    super(COLOR_MAP_NAME);
  }

  public RGB[] getRGBs(final int numColors) {
    RGB[] rgbs = new RGB[numColors];

    double direction = 1;
    double startHue = -30;
    double endHue = 240;
    double step = direction * (endHue - startHue) / numColors;

    for (int i = 0; i < numColors; i++) {
      double hue = startHue + direction * i * step;
      rgbs[i] = ColorUtil.hsvToRgb(hue, 1, 1);
    }

    return rgbs;
  }

}
