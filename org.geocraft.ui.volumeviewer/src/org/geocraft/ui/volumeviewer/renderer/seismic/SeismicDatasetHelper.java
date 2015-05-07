/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.volumeviewer.renderer.seismic;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.geocraft.core.color.ColorBar;
import org.geocraft.core.color.map.GrayscaleColorMap;
import org.geocraft.core.color.map.RainbowColorMap;
import org.geocraft.core.color.map.SeismicColorMap;
import org.geocraft.core.color.map.SpectrumColorMap;
import org.geocraft.core.common.math.MathUtil;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.seismic.PostStack2dLine;
import org.geocraft.ui.volumeviewer.renderer.util.VolumeViewerHelper;

import com.ardor3d.math.ColorRGBA;


public class SeismicDatasetHelper {

  /** The supported color map names. */
  public static final String[] COLOR_MAP_NAMES = { "Grayscale", "Seismic", "Rainbow", "Spectrum", "Class Background" };

  /** The number of colors in the color bar. */
  public static final int NUMBER_OF_COLORS = 64;

  /** Transparent color for the traces that don't need to be rendered. */
  private static final int TRANSPARENT_COLOR = new ColorRGBA(1, 1, 1, 0).asIntARGB();

  /**
   * Create a texture image based on the provided traces float data.
   * @param data the data
   * @param colorBar the color bar
   * @param numRows the number of rows
   * @param numCols the number of columns
   * @param traces the traces that the texture image is made of
   * @param maximumSize the maximum texture size
   * @return the image for the texture
   */
  public static BufferedImage createTexture(final float[] data, final ColorBar colorBar, final int numRows,
      final int numCols, final Trace[] traces, final int maximumSize) {
    // if the slice data in null, a texture cannot be computed
    if (data == null) {
      return null;
    }
    final BufferedImage textureImage = getImage(data, numRows, numCols, colorBar, traces, maximumSize, false, 0);
    return textureImage;
  }

  /**
   * Create a texture image based on the provided traces float data.
   * @param data the data
   * @param colorBar the color bar
   * @param numRows the number of rows
   * @param numCols the number of columns
   * @param traces the traces that the texture image is made of
   * @param maximumSize the maximum texture size
   * @return the image for the texture
   */
  public static BufferedImage createTexture(final float[] data, final ColorBar colorBar, final int numRows,
      final int numCols, final Trace[] traces, final int maximumSize, final boolean reversePolarity,
      final int transparency) {
    // if the slice data in null, a texture cannot be computed
    if (data == null) {
      return null;
    }
    final BufferedImage textureImage = getImage(data, numRows, numCols, colorBar, traces, maximumSize, reversePolarity,
        transparency);
    return textureImage;
  }

  /**
   * Build a texture image based on the provided traces float data.
   * @param data the data
   * @param colorBar the color bar
   * @param numRows the number of rows
   * @param numCols the number of columns
   * @param traces the traces that the texture image is made of
   * @param maximumSize the maximum texture size
   * @return the image for the texture
   */
  private static BufferedImage getImage(final float[] data, final int numRows, final int numCols,
      final ColorBar colorBar, final Trace[] traces, final int maximumSize, final boolean reversePolarity,
      final int transparency) {
    final BufferedImage image = new BufferedImage(numCols, numRows, BufferedImage.TYPE_INT_ARGB);
    final float alpha = 1f - transparency / 100f;
    for (int i = 0; i < data.length; i++) {
      final int row = i / numCols;
      final int column = i % numCols;
      final float scalar = reversePolarity ? -1 : 1;
      if ((traces == null || !traces[row].isMissing()) && !Float.isNaN(data[i])) {
        final Color color = VolumeViewerHelper.swtColorToColor(colorBar.getColor(data[i] * scalar, true));
        final ColorRGBA colorRGBA = VolumeViewerHelper.colorToColorRGBA(color, alpha);
        image.setRGB(column, row, colorRGBA.asIntARGB());
        //final int rgb = color.getRGB();
        //image.setRGB(column, row, rgb);
      } else {
        image.setRGB(column, row, TRANSPARENT_COLOR);
      }
    }
    return getValidTextureImage(image, maximumSize);
  }

  /**
   * Create a reversed texture image based on the provided traces float data.
   * @param data the data
   * @param cb the color bar
   * @param nRow the number of rows
   * @param nCol the number of columns
   * @param traces the traces that the texture image is made of
   * @param maximumSize the maximum texture size
   * @return the image for the texture
   */
  public static BufferedImage createReversedTexture(final float[] data, final ColorBar cb, final int nRow,
      final int nCol, final float nullValue, final int maximumSize) {
    // if the slice data in null, a texture cannot be computed
    if (data == null) {
      return null;
    }
    final BufferedImage textureImage = getReversedImage(data, nRow, nCol, cb, nullValue, maximumSize);
    return textureImage;
  }

  /**
   * Create a reversed texture image based on the provided traces float data.
   * @param data the data
   * @param cb the color bar
   * @param nRow the number of rows
   * @param nCol the number of columns
   * @param traces the traces that the texture image is made of
   * @param maximumSize the maximum texture size
   * @return the image for the texture
   */
  private static BufferedImage getReversedImage(final float[] data, final int nRow, final int nCol, final ColorBar cb,
      final float nullValue, final int maximumSize) {
    final BufferedImage b = new BufferedImage(nRow, nCol, BufferedImage.TYPE_INT_RGB);

    for (int i = 0; i < data.length; i++) {
      if (data[i] != nullValue) {
        final Color col = VolumeViewerHelper.swtColorToColor(cb.getColor(data[i], true));
        final int color = col.getRGB();
        b.setRGB(i / nCol, i % nCol, color);
      }
    }
    return getValidTextureImage(b, maximumSize);
  }

  /**
   * Create a texture image of at most 4096 * 4096 pixels.
   * @param image the input image
   * @param maximumSize the maximum texture size
   * @return the resized (if needed) image for the texture
   */
  private static BufferedImage getValidTextureImage(final BufferedImage image, final int maximumSize) {
    int width = image.getWidth();
    int height = image.getHeight();
    if (width <= maximumSize && height <= maximumSize) {
      return image;
    }
    width = Math.min(width, 4096);
    height = Math.min(height, 4096);
    final BufferedImage image2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    final Graphics2D g2 = image2.createGraphics();
    g2.drawImage(image, 0, 0, width, height, null);
    return image2;
  }

  /**
   * Set the color bar based on the color map name.
   * @param colorBar the color bar
   * @param name the color map name
   */
  public static void setColorBar(final ColorBar colorBar, final String name) {
    if (name.equals("Rainbow")) {
      colorBar.setColors(new RainbowColorMap().getRGBs(NUMBER_OF_COLORS));
    } else if (name.equals("Seismic")) {
      colorBar.setColors(new SeismicColorMap().getRGBs(NUMBER_OF_COLORS));
    } else if (name.equals("Grayscale")) {
      colorBar.setColors(new GrayscaleColorMap().getRGBs(NUMBER_OF_COLORS));
    } else if (name.equals("Spectrum")) {
      colorBar.setColors(new SpectrumColorMap().getRGBs(NUMBER_OF_COLORS));
    }// else if (name.equals("Class Background")) {
    //  colorBar.setColors(new ClassBackgroundColorMap().getRGBs(NUMBER_OF_COLORS));
    //}
  }

  /**
   * Calculate the color range for a given PostStack2d slice.
   * @param slice the slice
   * @return the color range
   */
  public static float[] getColorRange(final PostStack2dLine slice) {
    final float[] cdps = new float[(int) ((slice.getCdpEnd() - slice.getCdpStart()) / slice.getCdpDelta()) + 1];
    int k = 0;
    for (float value = slice.getCdpStart(); value <= slice.getCdpEnd(); value += slice.getCdpDelta()) {
      cdps[k++] = value;
    }
    final float[] values = slice.getTraces(cdps, slice.getZStart(), slice.getZEnd()).getData();
    return MathUtil.computeRange(values, Float.MAX_VALUE);
  }
}
