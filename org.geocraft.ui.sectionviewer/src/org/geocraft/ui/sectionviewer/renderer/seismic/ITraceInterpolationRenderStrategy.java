/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.renderer.seismic;


import java.awt.geom.Point2D;


public interface ITraceInterpolationRenderStrategy {

  /**
   * Sets the pixel rendering strategies for wiggles, color fills, density fills and variable density.
   * Depending on the display settings, some of these strategies will be no-ops.
   * 
   * @param wiggleStrategy
   * @param posColorFillStrategy
   * @param negColorFillStrategy
   * @param posDensityFillStrategy
   * @param negDensityFillStrategy
   * @param varDensityStrategy
   */
  public void setPixelRenderStrategies(final boolean wiggleStrategy, final boolean posColorFillStrategy,
      final boolean negColorFillStrategy, final boolean posDensityFillStrategy, final boolean negDensityFillStrategy,
      final boolean varDensityStrategy);

  /**
   * Sets the the pixel values for the specified trace that will be rendered into the seismic image.
   * TODO: How to reduce the args?
   * 
   * @param traceNo
   * @param traceIndex
   * @param numSamples
   * @param data
   * @param alpha
   * @param numColorBarColors
   * @param normalizationMin
   * @param normalizationMax
   * @param normalizationFactor
   * @param normalizationOffset
   * @param colorIndexWiggle
   * @param drawWiggle
   * @param pixelAnchorX
   * @param pixelAnchorY
   * @param pixelCoord
   * @param xCenter
   * @param xLeft
   * @param xRight
   * @param yTop
   * @param numPixelsY
   * @param xLinear
   * @param yLinear
   * @param pixelsPerSample
   * @param imageWidth
   * @param imageHeight
   * @param imagePixels
   */
  public void renderTrace(final int traceNo, final int traceIndex, final int numSamples, final float[] data,
      final int alpha, final int numColorBarColors, final boolean isReversedRange, final int polarityScalar,
      final double normalizationMin, final double normalizationMax, final double normalizationFactor,
      final double normalizationOffset, int traceClipping, final boolean[] drawWiggle, final int pixelAnchorX,
      final int pixelAnchorY, final Point2D.Double pixelCoord, final int xCenter, final int xLeft, final int xRight,
      final int yTop, final int numPixelsY, final int[] xLinear, final int[] yLinear, final double pixelsPerSample,
      final int imageWidth, final int imageHeight, final byte[] imagePixels);

}
