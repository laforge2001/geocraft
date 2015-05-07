/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.core.color.map;


import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;


/**
 * The interface for color maps.
 */
public interface IColorMap {

  /**
   * Gets the color map name.
   * @return the color map name.
   */
  String getName();

  /**
   * Creates an array of RGBs in the color map.
   * @param numColors the number of colors.
   * @return the array of RGBs in the color map.
   */
  RGB[] getRGBs(int numColors);

  /**
   * Creates an array of colors in the color map.
   * It is the responsibility of the consumer to dispose
   * of the resources when done with them.
   * @param numColors the number of colors.
   * @return the array of colors in the color map.
   */
  Color[] createColors(int numColors);
}
