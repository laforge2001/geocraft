/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.core.color;


import org.eclipse.swt.graphics.RGB;


/**
 * Generic color functions eg hsv -> rgb.
 *
 * does Color.HSBtoRGB() achieve the same thing?
 */
public class ColorUtil {

  private ColorUtil() {
    // The private constructor.
  }

  private static int _commonIndex = 0;

  private static final RGB[] _commonRGBs = new RGB[] { new RGB(255, 0, 0), new RGB(0, 255, 0), new RGB(0, 0, 255),
  /*new RGB(255, 255, 0),*/new RGB(255, 0, 255), new RGB(0, 255, 255), new RGB(255, 200, 0) };

  public static synchronized RGB getCommonRGB() {
    RGB rgb = _commonRGBs[_commonIndex++];
    if (_commonIndex >= _commonRGBs.length) {
      _commonIndex = 0;
    }
    return rgb;
  }

  /**
   * Convert from hue, luminosity and saturation into a Color object.
   *
   * red = 0 degrees
   * yellow = 60
   * green = 120
   * cyan = 180
   * blue = 240
   * magenta = 300
   *
   * Algorithm from http://www.cs.rit.edu/~ncs/color/t_convert.html
   *
   * @param hue range 0 - 360
   * @param sat range 0 - 1
   * @param val range 0 - 1
   * @return <CODE>java.awt.Color</CODE>
   */
  public static RGB hsvToRgb(final double hue, final double sat, final double val) {

    RGB result = null;

    // 0 < hue < 1
    double hue01 = hue / 360;

    hue01 = Math.abs(hue01 - Math.floor(hue01));

    // internally we use floats because the Color constructor does
    // not accept doubles.
    float h = (float) hue01;
    float s = (float) sat;
    float v = (float) val;

    int iv = (int) (v * 255);

    if (s == 0.0) {
      result = new RGB(iv, iv, iv);
    } else {

      h *= 6;

      int i = (int) Math.floor(h);
      float f = h - i;
      float p = v * (1 - s);
      float q = v * (1 - s * f);
      float t = v * (1 - s * (1 - f));
      int ip = (int) (p * 255);
      int iq = (int) (q * 255);
      int it = (int) (t * 255);

      switch (i) {

        case 0:
          result = new RGB(iv, it, ip);
          break;

        case 1:
          result = new RGB(iq, iv, ip);
          break;

        case 2:
          result = new RGB(ip, iv, it);
          break;

        case 3:
          result = new RGB(ip, iq, iv);
          break;

        case 4:
          result = new RGB(it, ip, iv);
          break;

        case 5:
          result = new RGB(iv, ip, iq);
          break;

        default:
          throw new RuntimeException("Invalid i in switch: " + i);
      }
    }

    return result;

  }

  public static java.awt.Color getColorAWT(RGB rgb) {
    return new java.awt.Color(rgb.red, rgb.green, rgb.blue);
  }
}
