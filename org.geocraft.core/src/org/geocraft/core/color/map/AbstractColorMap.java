/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.core.color.map;


import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;


/**
 * The abstract color map class.
 */
public abstract class AbstractColorMap implements IColorMap {

  /** The color map name. */
  protected String _name;

  /**
   * Constructs an abstract color map.
   * @param name the color map name.
   */
  public AbstractColorMap(final String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }

  public Color[] createColors(final int numColors) {
    RGB[] rgbs = getRGBs(numColors);
    Color[] colors = new Color[numColors];
    for (int i = 0; i < numColors; i++) {
      colors[i] = new Color(null, rgbs[i]);
    }
    return colors;
  }
}
