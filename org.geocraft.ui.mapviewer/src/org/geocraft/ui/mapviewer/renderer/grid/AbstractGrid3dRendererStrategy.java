/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.mapviewer.renderer.grid;


import org.geocraft.core.color.ColorBar;


public abstract class AbstractGrid3dRendererStrategy implements IGrid3dRendererStragegy {

  protected final Grid3dRendererModel _model;

  public AbstractGrid3dRendererStrategy(final Grid3dRendererModel model) {
    _model = model;
  }

  /**
   * Sets the color index of the specified image pixel.
   * @param x the x pixel.
   * @param y the y pixel.
   * @param width the image width in pixels.
   * @param clrIndex the color index.
   */
  protected void setColorIndexOfPixel(final int x, final int y, final int width, final ColorBar colorBar,
      final int clrIndex, final int alpha, final byte[] imagePixels, final byte[] imageAlphas) {
    int numColors = colorBar.getNumColors();
    int colorIndex = Math.max(clrIndex, 0);
    colorIndex = Math.min(colorIndex, numColors - 1);
    int pixelIndex = y * width + x;
    if (colorIndex >= 0 && colorIndex < numColors) {
      imagePixels[pixelIndex] = (byte) colorIndex;
      imageAlphas[pixelIndex] = (byte) alpha;
    } else {
      imageAlphas[pixelIndex] = Grid3dRenderer.FULLY_TRANSPARENT;
    }
  }

  /**
   * Sets the color index of the specified image pixel.
   * @param x the x pixel.
   * @param y the y pixel.
   * @param width the image width in pixels.
   * @param clrIndex the color index.
   */
  protected void setColorIndexOfPixel(final int x, final int y, final int width, final int clrIndex, final int alpha,
      final byte[] imagePixels, final byte[] imageAlphas) {
    int numColors = 32 * 32 * 32;
    int colorIndex = Math.max(clrIndex, 0);
    colorIndex = Math.min(colorIndex, numColors - 1);
    int pixelIndex = y * width + x;
    if (colorIndex >= 0 && colorIndex < numColors) {
      imagePixels[pixelIndex] = (byte) colorIndex;
      imageAlphas[pixelIndex] = (byte) alpha;
    } else {
      imageAlphas[pixelIndex] = Grid3dRenderer.FULLY_TRANSPARENT;
    }
  }

  /**
   * Sets the color index of the specified image pixel.
   * @param x the x pixel.
   * @param y the y pixel.
   * @param width the image width in pixels.
   * @param clrIndex the color index.
   */
  protected void setColorIndexOfPixel32Bits(final int x, final int y, final int width, final byte[] colorBytes,
      final int alpha, final boolean isTransparent, final int numBytesPerPixel, final byte[] imagePixels,
      final byte[] imageAlphas) {
    int pixelIndex = y * width + x;
    if (!isTransparent) {
      for (int j = 0; j < numBytesPerPixel; j++) {
        imagePixels[pixelIndex * numBytesPerPixel + j] = colorBytes[j];
      }
      imageAlphas[pixelIndex] = (byte) alpha;
    } else {
      imageAlphas[pixelIndex] = Grid3dRenderer.FULLY_TRANSPARENT;
    }
  }

}