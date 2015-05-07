package org.geocraft.abavo;


import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.color.map.AbstractColorMap;
import org.geocraft.core.service.ServiceProvider;


/**
 * Defines the class background color map used in the A vs B crossplot.
 * The class background color map consists of 64 colors divided into
 * 8 groups of colors, with 8 gradations in each group. This class is
 * provided as an extension to the color map extension point located
 * in the <code>org.geocraft.ui.color</code> bundle.
 */
public class ClassBackgroundColorMap extends AbstractColorMap {

  /** The array of RGB values in the class background color map. */
  public static final int[] RGBS = { 127, 0, 0, 145, 36, 36, 163, 73, 73, 182, 109, 109, 200, 145, 145, 218, 181, 181,
      236, 218, 218, 254, 254, 254,

      0, 127, 0, 36, 145, 36, 73, 163, 73, 109, 182, 109, 145, 200, 145, 181, 218, 181, 218, 236, 218, 254, 254, 254,

      127, 0, 127, 145, 36, 145, 163, 73, 163, 182, 109, 182, 200, 145, 200, 218, 181, 218, 236, 218, 236, 255, 254,
      254,

      0, 0, 127, 36, 36, 145, 73, 73, 163, 109, 109, 182, 145, 145, 200, 181, 181, 218, 218, 218, 236, 254, 254, 254,

      254, 254, 254, 218, 254, 254, 181, 254, 254, 145, 254, 254, 109, 254, 254, 73, 254, 254, 36, 254, 254, 0, 254,
      254,

      254, 254, 254, 254, 218, 254, 254, 181, 254, 254, 145, 254, 254, 109, 254, 254, 73, 254, 254, 36, 254, 254, 0,
      254,

      254, 254, 254, 218, 254, 218, 181, 254, 181, 145, 254, 145, 109, 254, 109, 73, 254, 73, 36, 254, 36, 0, 254, 0,

      254, 254, 254, 254, 218, 218, 254, 181, 181, 254, 145, 145, 254, 109, 109, 254, 73, 73, 254, 36, 36, 254, 0, 0 };

  /**
   * The default constructor.
   */
  public ClassBackgroundColorMap() {
    super("Class Background");
  }

  /**
   * Returns an array of the colors in the color map.
   * It is the responsibility of the consumer to dispose
   * of the resources when done with them.
   * @return the array of 64 colors in the color map.
   */
  public Color[] createColors() {
    return createColors(64);
  }

  /**
   * Creates an array of RGBs in the color map.
   * The number of colors requested must be exactly 64, or else an
   * <code>IllegalArgumentException</code> is thrown.
   * @param numColors the number of colors.
   * @return the array of RGBs in the color map.
   */
  public RGB[] getRGBs(final int numColors) {
    if (numColors != 64) {
      ServiceProvider.getLoggingService().getLogger(getClass()).warn(
          "The class background colormap only supports 64 colors. Returning 64 colors.");
    }
    RGB[] rgbs = new RGB[64];
    for (int i = 0; i < 64; i++) {
      rgbs[64 - i - 1] = new RGB(RGBS[i * 3 + 0], RGBS[i * 3 + 1], RGBS[i * 3 + 2]);
    }
    return rgbs;
  }
}
