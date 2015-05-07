/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.chartviewer;


import org.eclipse.swt.graphics.RGB;
import org.geocraft.ui.plot.defs.PointStyle;


public class ChartUtil {

  public static RGB createRandomRGB() {
    int r = (int) (Math.random() * 256);
    int g = (int) (Math.random() * 256);
    int b = (int) (Math.random() * 256);
    return new RGB(r, g, b);
  }

  public static PointStyle createRandomPointStyle() {
    PointStyle[] styles = PointStyle.values();
    int index = (int) (Math.random() * styles.length);
    return styles[index];
  }
}
