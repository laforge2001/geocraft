/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.renderer.seismic;


public interface IPixelRenderStrategy {

  /**
   * Sets the color index for the specified block of pixels in an image array.
   * 
   * @param xIndex0 the starting x pixel location.
   * @param xIndex1 the ending x pixel location.
   * @param yIndex the y pixel location.
   * @param imageWidth the image width.
   * @param imageHeight the image height.
   * @param colorIndex the color index.
   * @param alpha the alpha value.
   * @param imageColors the array of color pixels.
   */
  void renderPixels(final int xIndex0, final int xIndex1, final int yIndex, final int colorIndex, final int alpha,
      final int imageWidth, final int imageHeight, final byte[] imageColors);

  /**
   * Sets the color index for the specified pixel location in an image array.
   * 
   * @param xIndex0
   * @param xIndex1
   * @param yIndex
   * @param imageWidth
   * @param imageHeight
   * @param colorIndex
   * @param alpha
   * @param imageColors
   */
  void renderPixel(final int xIndex, final int yIndex, final int colorIndex, final int alpha, final int imageWidth,
      final int imageHeight, final byte[] imageColors);
}
