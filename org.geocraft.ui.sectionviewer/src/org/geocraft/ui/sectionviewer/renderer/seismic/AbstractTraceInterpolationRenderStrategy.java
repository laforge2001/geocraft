/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.renderer.seismic;


import java.awt.geom.Point2D;

import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;


public abstract class AbstractTraceInterpolationRenderStrategy implements ITraceInterpolationRenderStrategy {

  protected IModelSpaceCanvas _canvas;

  protected IModelSpace _modelSpace;

  protected IPixelRenderStrategy _pixelRenderNoOp = new NoOpPixelRenderStrategy();

  protected IPixelRenderStrategy _pixelRenderBasic = new BasicPixelRenderStrategy();

  private IPixelRenderStrategy _wiggleTraceRenderer;

  private IPixelRenderStrategy _positiveColorFillRenderer;

  private IPixelRenderStrategy _negativeColorFillRenderer;

  private IPixelRenderStrategy _positiveDensityFillRenderer;

  private IPixelRenderStrategy _negativeDensityFillRenderer;

  private IPixelRenderStrategy _variableDensityRenderer;

  private boolean _needsTransform = false;

  public AbstractTraceInterpolationRenderStrategy(final IModelSpaceCanvas canvas, final IModelSpace modelSpace) {
    _canvas = canvas;
    _modelSpace = modelSpace;
    IPixelRenderStrategy noOp = new NoOpPixelRenderStrategy();
    _wiggleTraceRenderer = noOp;
    _positiveColorFillRenderer = noOp;
    _negativeColorFillRenderer = noOp;
    _positiveDensityFillRenderer = noOp;
    _negativeDensityFillRenderer = noOp;
    _variableDensityRenderer = noOp;
  }

  public void setPixelRenderStrategies(final boolean wiggles, final boolean positiveColorFill,
      final boolean negativeColorFill, final boolean positiveDensityFill, final boolean negativeDensityFill,
      final boolean variableDensity) {

    _needsTransform = false;
    _wiggleTraceRenderer = _pixelRenderNoOp;
    _positiveColorFillRenderer = _pixelRenderNoOp;
    _positiveDensityFillRenderer = _pixelRenderNoOp;
    _negativeColorFillRenderer = _pixelRenderNoOp;
    _negativeDensityFillRenderer = _pixelRenderNoOp;
    _variableDensityRenderer = _pixelRenderNoOp;
    if (wiggles) {
      _wiggleTraceRenderer = _pixelRenderBasic;
    }
    if (positiveColorFill) {
      _positiveColorFillRenderer = _pixelRenderBasic;
      _needsTransform = true;
    }
    if (negativeColorFill) {
      _negativeColorFillRenderer = _pixelRenderBasic;
      _needsTransform = true;
    }
    if (positiveDensityFill) {
      _positiveDensityFillRenderer = _pixelRenderBasic;
      _needsTransform = true;
    }
    if (negativeDensityFill) {
      _negativeDensityFillRenderer = _pixelRenderBasic;
      _needsTransform = true;
    }
    if (variableDensity) {
      _variableDensityRenderer = _pixelRenderBasic;
    }
  }

  protected void colorIndividualPixels(final int traceNo, final int alpha, final int numColorBarColors,
      final boolean isReversedRange, final double value, final double normalizationMin, final double normalizationMax,
      final double normalizationFactor, final double normalizationOffset, final int traceClipping, final int xLeft,
      final int xCenter, final int xRight, final int yTop, final int pixelAnchorX, final int pixelAnchorY,
      final Point2D.Double pixelCoord, final int sampleIndex, final int imageWidth, final int imageHeight,
      final byte[] imageColors) {

    double dx = value * normalizationOffset / normalizationFactor;
    if (dx >= 0) {
      dx = Math.min(dx, traceClipping);
    } else {
      dx = Math.max(dx, -traceClipping);
    }
    double x = traceNo + dx;

    int colorIndexPosFill = numColorBarColors + 2;
    int colorIndexNegFill = numColorBarColors + 3;
    int colorIndex = getColorIndex(numColorBarColors, normalizationMin, normalizationMax, value, isReversedRange);
    if (colorIndex < 0) {
      colorIndex = 0;
    } else if (colorIndex >= numColorBarColors) {
      colorIndex = numColorBarColors - 1;
    }

    int py = yTop + sampleIndex - pixelAnchorY;
    if (_needsTransform) {
      _canvas.transformModelToPixel(_modelSpace, x, 0, pixelCoord); // only care about x.
      int px0 = xCenter - pixelAnchorX;
      int px1 = xCenter - pixelAnchorX;
      px1 = (int) Math.round(pixelCoord.x);
      px1 -= pixelAnchorX;

      if (value > 0) {
        _positiveColorFillRenderer.renderPixels(px0, px1, py, colorIndexPosFill, alpha, imageWidth, imageHeight,
            imageColors);
        _positiveDensityFillRenderer
            .renderPixels(px0, px1, py, colorIndex, alpha, imageWidth, imageHeight, imageColors);
      } else if (value < 0) {
        _negativeColorFillRenderer.renderPixels(px0, px1, py, colorIndexNegFill, alpha, imageWidth, imageHeight,
            imageColors);
        _negativeDensityFillRenderer
            .renderPixels(px0, px1, py, colorIndex, alpha, imageWidth, imageHeight, imageColors);
      }
    }
    _variableDensityRenderer.renderPixels(xLeft, xRight, py, colorIndex, alpha, imageWidth, imageHeight, imageColors);
  }

  /**
   * Returns the color index to associated with a value, based on a start and end value.
   * 
   * @param numColors the number of color divisions in the range.
   * @param start the starting value of the range.
   * @param end the ending value of the range.
   * @param value the value to lookup.
   * @return the color index associated with the value.
   */
  protected int getColorIndex(final int numColors, final double start, final double end, final double value,
      final boolean isReversedRange) {
    // Find position in the range.
    // TODO: think about this!
    double pcntg = 0;
    if (!isReversedRange) {
      pcntg = (value - start) / (end - start);
    } else {
      pcntg = (value - end) / (start - end);
    }
    return (int) Math.round(pcntg * (numColors - 1));
  }

  /**
   * Colors the pixels for the rendering the wiggle traces.
   * 
   * @param colorIndexWiggle the color index used for rendering the wiggles.
   * @param xIndices the array of x indices for drawing the wiggle lines.
   * @param yIndices the array of y indices for drawing the wiggle lines.
   * @param alpha the alpha value used for rendering the wiggles.
   */
  protected void renderWiggles(final int colorIndexWiggle, final int[] xIndices, final int[] yIndices, final int alpha,
      final int imageWidth, final int imageHeight, final byte[] imageColors) {
    for (int i = 1; i < xIndices.length; i++) {
      int ppx0 = xIndices[i - 1];
      int ppx1 = xIndices[i];
      int ppy0 = yIndices[i - 1];
      int ppy1 = yIndices[i];
      int xdiff = ppx1 - ppx0;
      int ydiff = ppy1 - ppy0;
      int axdiff = Math.abs(xdiff);
      int aydiff = Math.abs(ydiff);
      if (axdiff >= aydiff) {
        double dx = (double) xdiff / axdiff;
        double dy = (double) ydiff / axdiff;
        for (int j = 0; j <= axdiff; j++) {
          int ppx = (int) (ppx0 + j * dx + 0.5);
          int ppy = (int) (ppy0 + j * dy + 0.5);
          _wiggleTraceRenderer.renderPixel(ppx, ppy, colorIndexWiggle, alpha, imageWidth, imageHeight, imageColors);
        }
      } else {
        double dx = (double) xdiff / aydiff;
        double dy = (double) ydiff / aydiff;
        for (int j = 0; j <= aydiff; j++) {
          int ppy = (int) (ppy0 + j * dy + 0.5);
          int ppx = (int) (ppx0 + j * dx + 0.5);
          _wiggleTraceRenderer.renderPixel(ppx, ppy, colorIndexWiggle, alpha, imageWidth, imageHeight, imageColors);
        }
      }
    }
  }
}
