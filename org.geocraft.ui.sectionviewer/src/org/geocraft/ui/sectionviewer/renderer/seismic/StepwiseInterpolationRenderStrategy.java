/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.renderer.seismic;


import java.awt.geom.Point2D.Double;

import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;


/**
 * The strategy for rendering trace pixels using step-wise interpolation.
 */
public class StepwiseInterpolationRenderStrategy extends AbstractTraceInterpolationRenderStrategy {

  public StepwiseInterpolationRenderStrategy(final IModelSpaceCanvas canvas, final IModelSpace modelSpace) {
    super(canvas, modelSpace);
  }

  public void renderTrace(final int traceNo, final int traceIndex, final int numSamples, final float[] data,
      final int alpha, final int numColorBarColors, final boolean isReversedRange, final int polarityScalar,
      final double normalizationMin, final double normalizationMax, final double normalizationFactor,
      final double normalizationOffset, final int traceClipping, final boolean[] drawWiggle, final int pixelAnchorX,
      final int pixelAnchorY, final Double pixelCoord, final int xLeft, final int xCenter, final int xRight,
      final int yTop, final int numPixelsY, final int[] xStep, final int[] yStep, final double pixelsPerSample,
      final int imageWidth, final int imageHeight, final byte[] imageColors) {
    // Build the step-wise interpolation.
    for (int pixelIndex = 0; pixelIndex < numPixelsY; pixelIndex++) {
      double dSample = pixelIndex / pixelsPerSample;
      int iSample = (int) dSample;
      if (iSample >= 0 && iSample < numSamples) {
        double value = data[iSample] * polarityScalar;
        colorIndividualPixels(traceNo, alpha, numColorBarColors, isReversedRange, value, normalizationMin,
            normalizationMax, normalizationFactor, normalizationOffset, traceClipping, xLeft, xCenter, xRight, yTop,
            pixelAnchorX, pixelAnchorY, pixelCoord, pixelIndex, imageWidth, imageHeight, imageColors);
      }
    }
    if (drawWiggle[traceIndex]) {
      // Note: The wiggle color index is the number of colorbar colors + 1.
      renderWiggles(numColorBarColors + 1, xStep, yStep, alpha, imageWidth, imageHeight, imageColors);
    }
  }

}
