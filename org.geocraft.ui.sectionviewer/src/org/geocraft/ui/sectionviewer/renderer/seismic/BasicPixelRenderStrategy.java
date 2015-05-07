/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.renderer.seismic;


public class BasicPixelRenderStrategy implements IPixelRenderStrategy {

  /**
   * Sets the pixel bytes values at specified locations in the image array.
   * The locations are specified as a y index and a range of x indices.
   * 
   * @param xIndex0 the starting x index of the pixels.
   * @param xIndex1 the ending x index of the pixels.
   * @param yIndex the y index of the pixel.
   * @param imageWidth the width of the image.
   * @param imageHeight the height of the image.
   * @param colorIndex the color index to set.
   * @param alpha the alpha value to set.
   */
  public void renderPixels(final int xIndex0, final int xIndex1, final int yIndex, final int colorIndex,
      final int alpha, final int imageWidth, final int imageHeight, final byte[] imagePixels) {
    int pxmin = Math.min(xIndex0, xIndex1);
    int pxmax = Math.max(xIndex0, xIndex1);
    for (int xIndex = pxmin; xIndex <= pxmax; xIndex++) {
      renderPixel(xIndex, yIndex, colorIndex, alpha, imageWidth, imageHeight, imagePixels);
    }
  }

  /**
   * Sets the pixel bytes values at a specified location in the image array.
   * The location is specified as an x index and a y index.
   * 
   * @param xIndex the x index of the pixel.
   * @param yIndex the y index of the pixel.
   * @param imageWidth the width of the image.
   * @param imageHeight the height of the image.
   * @param colorIndex the color index to set.
   * @param alpha the alpha value to set.
   */
  public void renderPixel(final int xIndex, final int yIndex, final int colorIndex, final int alpha,
      final int imageWidth, final int imageHeight, final byte[] imagePixels) {
    int pixelIndex = getPixelIndex(xIndex, yIndex, imageWidth, imageHeight);
    if (pixelIndex >= 0 && pixelIndex < imagePixels.length) {
      imagePixels[pixelIndex] = (byte) colorIndex;
    }
  }

  /**
   * Returns the index of a pixel coordinate in a 1D array, based on its indices in a 2D array.
   * 
   * @param xIndex the x index of the pixel in a 2D array.
   * @param yIndex the y index of the pixel in a 2D array.
   * @param numPixelsX the width (x dimension) of the 2D array.
   * @param numPixelsY the height (y dimension) of the 2D array.
   * @return the index in a 1D array.
   */
  private int getPixelIndex(final int xIndex, final int yIndex, final int numPixelsX, final int numPixelsY) {
    if (xIndex < 0 || xIndex > numPixelsX || yIndex < 0 || yIndex > numPixelsY) {
      return -1;
    }
    return yIndex * numPixelsX + xIndex;
  }
}
