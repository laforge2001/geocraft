/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.renderer.seismic;


import java.awt.geom.Point2D;
import java.util.Arrays;

import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.sectionviewer.IPlotTrace;
import org.geocraft.ui.sectionviewer.NormalizationMethod;


public class TraceRenderRunnable implements Runnable {

  private final IModelSpaceCanvas _canvas;

  private final IModelSpace _modelSpace;

  private final int _numColorBarColors;

  private final boolean _isReversedRange;

  private final int _alpha;

  private final int _traceStart;

  private final int _traceEnd;

  private final ITraceInterpolationRenderStrategy _interpolationRenderStrategy;

  private final boolean _isLinearInterpolation;

  private final boolean _isStepInterpolation;

  private final NormalizationMethod _normalizationMethod;

  private final boolean _byTraceNormalization;

  private final int _traceClipping;

  private double _normalizationMin;

  private double _normalizationMax;

  private final double _normalizationOffset;

  private final int _polarityScalar;

  private final boolean[] _drawWiggle;

  private final IPlotTrace[] _plotTraces;

  private final int _pixelAnchorX;

  private final int _pixelAnchorY;

  private final int _imageWidth;

  private final int _imageHeight;

  private final byte[] _imageColors;

  public TraceRenderRunnable(final IModelSpaceCanvas canvas, final IModelSpace modelSpace, final boolean isReversedRange, final int numColorBarColors, final int alpha, final IPlotTrace[] plotTraces, final int traceStart, final int traceEnd, final ITraceInterpolationRenderStrategy interpolationStrategy, final boolean isLinearInterpolation, final boolean isStepInterpolation, final NormalizationMethod normalizationMethod, final boolean byTraceNormalization, final int traceClipping, final double normalizationMin, final double normalizationMax, final double normalizationOffset, final int polarityScalar, final boolean[] drawWiggle, final int pixelAnchorX, final int pixelAnchorY, final int imageWidth, final int imageHeight, final byte[] imageColors) {
    _canvas = canvas;
    _modelSpace = modelSpace;
    _numColorBarColors = numColorBarColors;
    _isReversedRange = isReversedRange;
    _alpha = alpha;
    _plotTraces = Arrays.copyOf(plotTraces, plotTraces.length);
    _traceStart = traceStart;
    _traceEnd = traceEnd;
    _interpolationRenderStrategy = interpolationStrategy;
    _isLinearInterpolation = isLinearInterpolation;
    _isStepInterpolation = isStepInterpolation;
    _traceClipping = traceClipping;
    _normalizationMethod = normalizationMethod;
    _byTraceNormalization = byTraceNormalization;
    _normalizationMin = normalizationMin;
    _normalizationMax = normalizationMax;
    _normalizationOffset = normalizationOffset;
    _drawWiggle = Arrays.copyOf(drawWiggle, drawWiggle.length);
    _polarityScalar = polarityScalar;
    _pixelAnchorX = pixelAnchorX;
    _pixelAnchorY = pixelAnchorY;
    _imageWidth = imageWidth;
    _imageHeight = imageHeight;
    _imageColors = imageColors;
  }

  /**
   * @param canvas
   * @param modelSpace
   * @param isReversedRange
   * @param numColorBarColors
   * @param alpha
   * @param plotTraces
   * @param traceStart
   * @param traceEnd
   * @param interpolationRenderStrategy
   * @param isLinearInterpolation
   * @param isStepInterpolation
   * @param normalization
   * @param byTraceNormalization
   * @param traceClipping
   * @param normalizationMin
   * @param normalizationMax
   * @param normalizationOffset
   * @param polarityScalar
   * @param drawWiggle
   * @param pixelAnchorX
   * @param pixelAnchorY
   * @param imageWidth
   * @param imageHeight
   * @param imageColors
   */

  public void run() {

    int xCenterPrev = -999;

    for (int traceNo = _traceStart; traceNo <= _traceEnd; traceNo++) {
      int traceIndex = traceNo - _traceStart;
      IPlotTrace plotTrace = _plotTraces[traceNo - 1];
      Trace trace = plotTrace.getTrace();
      float[] data = trace.getDataReference();

      // Determine the normalization min and max, if normalization is trace-based.
      if (_normalizationMethod.equals(NormalizationMethod.BY_TRACE_MAXIMUM)) {
        _normalizationMin = plotTrace.getDataMinimum() * _polarityScalar;
        _normalizationMax = plotTrace.getDataMaximum() * _polarityScalar;
        double minmax = Math.max(Math.abs(_normalizationMin), Math.abs(_normalizationMax));
        _normalizationMin = -minmax;
        _normalizationMax = minmax;
      } else if (_normalizationMethod.equals(NormalizationMethod.BY_TRACE_AVERAGE)) {
        _normalizationMax = plotTrace.getDataAverage() * _polarityScalar;
        _normalizationMin = -_normalizationMax * _polarityScalar;
      }
      double normalizationFactor = Math.max(Math.abs(_normalizationMin), Math.abs(_normalizationMax));

      // For dead or missing traces, the normalization factor equals zero, which is invalid,
      // so reset it to 1. Also set a normalization min and max so that the computed color
      // is in the center of the color bar.
      if (_byTraceNormalization && (plotTrace.getTrace().isDead() || plotTrace.getTrace().isMissing())) {
        _normalizationMin = -1;
        _normalizationMax = 1;
        normalizationFactor = 1;
      }

      Point2D.Double pixelTopCenter = new Point2D.Double();
      _canvas.transformModelToPixel(_modelSpace, traceNo, trace.getZStart(), pixelTopCenter);
      Point2D.Double pixelTopLeft = new Point2D.Double();
      _canvas.transformModelToPixel(_modelSpace, traceNo - 0.5, trace.getZStart(), pixelTopLeft);
      Point2D.Double pixelBottomRight = new Point2D.Double();
      _canvas.transformModelToPixel(_modelSpace, traceNo + 0.5, trace.getZEnd(), pixelBottomRight);

      int xCenter = Math.round((float) pixelTopCenter.x);
      int xLeft = Math.round((float) pixelTopLeft.x);
      int yTop = Math.round((float) pixelTopLeft.y);
      int xRight = Math.round((float) pixelBottomRight.x);
      int yBottom = Math.round((float) pixelBottomRight.y);
      xLeft -= _pixelAnchorX;
      xRight -= _pixelAnchorX;

      // Check the pixel x coordinate against the previously rendered trace.
      // If it is the same, then skip this trace.
      if (xCenterPrev != -999 && xCenter == xCenterPrev) {
        continue;
      }
      xCenterPrev = xCenter;

      // Build the wiggle arrays.
      int numSamples = trace.getNumSamples();
      int[] xLinear = new int[numSamples];
      int[] yLinear = new int[numSamples];
      int[] xStep = new int[1 + (numSamples - 1) * 2];
      int[] yStep = new int[1 + (numSamples - 1) * 2];
      double pixelsPerSample = (pixelBottomRight.y - pixelTopLeft.y) / (numSamples - 1);
      Point2D.Double pixelCoord = new Point2D.Double();
      for (int i = 0; i < numSamples; i++) {
        double dx = data[i] * _polarityScalar * _normalizationOffset / normalizationFactor;
        if (dx >= 0) {
          dx = Math.min(dx, _traceClipping);
        } else {
          dx = Math.max(dx, -_traceClipping);
        }
        double x = traceNo + dx;
        double y = trace.getZStart() + i * trace.getZDelta();
        _canvas.transformModelToPixel(_modelSpace, x, y, pixelCoord);
        int px = (int) (pixelCoord.x + 0.5) - _pixelAnchorX;
        int py = (int) (pixelCoord.y + 0.5) - _pixelAnchorY;
        xLinear[i] = px;
        yLinear[i] = py;
        if (i > 0) {
          xStep[1 + (i - 1) * 2] = xLinear[i - 1];
          yStep[1 + (i - 1) * 2] = yLinear[i];
          xStep[2 + (i - 1) * 2] = xLinear[i];
          yStep[2 + (i - 1) * 2] = yLinear[i];
        } else {
          xStep[i] = px;
          yStep[i] = py;
        }
      }
      int numPixelsY = yLinear[numSamples - 1] - yLinear[0] + 1;

      int[] xWiggle = xLinear;
      int[] yWiggle = yLinear;
      if (_isLinearInterpolation) {
        xWiggle = xLinear;
        yWiggle = yLinear;
      } else if (_isStepInterpolation) {
        xWiggle = xStep;
        yWiggle = yStep;
      }
      _interpolationRenderStrategy.renderTrace(traceNo, traceIndex, numSamples, data, _alpha, _numColorBarColors,
          _isReversedRange, _polarityScalar, _normalizationMin, _normalizationMax, normalizationFactor,
          _normalizationOffset, _traceClipping, _drawWiggle, _pixelAnchorX, _pixelAnchorY, pixelCoord, xLeft, xCenter,
          xRight, yTop, numPixelsY, xWiggle, yWiggle, pixelsPerSample, _imageWidth, _imageHeight, _imageColors);
    }
  }

}
