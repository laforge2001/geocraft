/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.internal;


import java.text.DecimalFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.geocraft.core.common.util.Labels;
import org.geocraft.ui.plot.IAxisRangeRenderer;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.PlotScrolling;
import org.geocraft.ui.plot.attribute.TextProperties;
import org.geocraft.ui.plot.axis.IAxis;
import org.geocraft.ui.plot.defs.AxisDirection;
import org.geocraft.ui.plot.defs.AxisPlacement;
import org.geocraft.ui.plot.defs.AxisScale;
import org.geocraft.ui.plot.defs.Orientation;
import org.geocraft.ui.plot.layout.CanvasLayoutModel;


public class DefaultAxisRangeRenderer implements IAxisRangeRenderer {

  /** The axis associated with the canvas. */
  protected IAxis _axis;

  /** The axis placement. */
  protected AxisPlacement _placement;

  protected int _increment = 1;

  protected boolean _scrolled = false;

  protected DecimalFormat _formatter;

  protected IPlot _plot;

  protected AxisRangeCanvas _canvas;

  public DefaultAxisRangeRenderer(final AxisRangeCanvas canvas, final IPlot plot, final IAxis axis, final AxisPlacement placement, final CanvasLayoutModel layoutModel) {
    _canvas = canvas;
    _plot = plot;
    _axis = axis;
    _placement = placement;
    _formatter = new DecimalFormat("0.0####E0");
    PlotScrolling scrolling = plot.getScrolling();
    if (_placement == AxisPlacement.TOP || _placement == AxisPlacement.BOTTOM) {
      _scrolled = scrolling.hasHorizontal();
    } else if (_placement == AxisPlacement.LEFT || _placement == AxisPlacement.RIGHT) {
      _scrolled = scrolling.hasVertical();
    }
  }

  // TODO this could be simplified. Code review? 
  public void render(final GC gc, final Rectangle rectangle, final TextProperties textProperties, final int thumb,
      final int minimum, final int maximum, final int selection, final int sizeToSubtract) {
    int count = 0;
    int ix = 0;
    int idx;
    int iy = 0;
    int idy;
    int width = rectangle.width;
    int height = rectangle.height;
    double percent;
    String text;
    int textWidth = 0;
    int textHeight = 0;
    AxisScale scale = _axis.getScale();
    Orientation orientation = _axis.getOrientation();
    AxisDirection direction = _axis.getDirection();
    double start = _axis.getViewableStart();
    double end = _axis.getViewableEnd();
    if (_scrolled) {
      double delta = maximum - minimum;
      Point modelSpaceCanvasSize = _plot.getModelSpaceCanvas().getSize();
      if (_axis.getOrientation().equals(Orientation.HORIZONTAL)) {
        width -= sizeToSubtract;
        width = Math.min(width, modelSpaceCanvasSize.x);
      } else if (_axis.getOrientation().equals(Orientation.VERTICAL)) {
        height -= sizeToSubtract;
        height = Math.min(height, modelSpaceCanvasSize.y);
      }
      boolean switchPercents = false;
      if (orientation.equals(Orientation.VERTICAL)) {
        switchPercents = !switchPercents;
      }
      if (!direction.isStartToEnd()) {
        switchPercents = !switchPercents;
      }
      double startPercent = selection / delta;
      double endPercent = (selection + thumb) / delta;
      if (switchPercents) {
        startPercent = (delta - selection - thumb) / delta;
        endPercent = (delta - selection) / delta;
      }
      double length = end - start;
      double startTemp = start + startPercent * length;
      double endTemp = start + endPercent * length;
      start = startTemp;
      end = endTemp;
    }
    double step = 10;
    double stepOrigin = 0;
    double diff = end - start;
    Font textFont = textProperties.getFont();
    Color textColor = new Color(gc.getDevice(), textProperties.getColor());
    FontMetrics metrics;
    int x0 = rectangle.x;
    int y0 = rectangle.y;
    int x1 = x0 + width - 1;
    int y1 = y0 + height - 1;

    // If step value is <= zero, do not draw.
    if (step <= 0) {
      return;
    }
    if (scale.equals(AxisScale.LOG)) {
      start = Math.log(start);
      end = Math.log(end);
      step = Math.log(step);
      stepOrigin = 1;
      stepOrigin = Math.log(stepOrigin);
      diff = end - start;
    }

    int index0 = Integer.MAX_VALUE;
    int index1 = Integer.MIN_VALUE;
    double min = Math.min(start, end);
    double max = Math.max(start, end);

    double[] labels = Labels.computeLabels(min, max, _canvas.getLabelDensity());
    if (scale.equals(AxisScale.LINEAR)) {
      step = Math.abs(labels[2]);
    } else if (scale.equals(AxisScale.LOG)) {
      step = Math.log(10);
    }

    index0 = (int) ((min - stepOrigin) / step);
    index1 = (int) ((max - stepOrigin) / step);
    if (stepOrigin + index0 * step < min) {
      index0++;
    }
    if (stepOrigin + index1 * step > max) {
      index1--;
    }
    if (index0 == Integer.MAX_VALUE || index1 == Integer.MIN_VALUE) {
      return;
    }
    count = 1 + Math.abs(index1 - index0);

    gc.setAntialias(SWT.OFF);
    gc.setTextAntialias(SWT.ON);
    gc.setFont(textFont);
    gc.setForeground(textColor);
    textColor.dispose();
    metrics = gc.getFontMetrics();
    int canvasWidth = _canvas.getSize().x;
    int canvasHeight = _canvas.getSize().y;
    for (int i = 0; i < count; i++) {
      double anno = stepOrigin + (index0 + i) * step;
      percent = (anno - start) / diff;
      percent = Math.abs(percent);
      if (scale.equals(AxisScale.LOG)) {
        anno = Math.exp(anno);
      }

      Float flt = new Float(anno);
      float value = flt.floatValue();
      try {
        text = String.format(Labels.getFormat(flt), flt);
      } catch (Exception e) {
        text = "Unknown";
      }
      textWidth = metrics.getAverageCharWidth() * text.length();
      //if (orientation.equals(Orientation.VERTICAL) && textWidth > canvasWidth) {
      //  text = _formatter.format(flt.floatValue());
      //}
      //textWidth = metrics.getAverageCharWidth() * text.length();
      if (orientation.equals(Orientation.VERTICAL) && textWidth >= canvasWidth) {
        _canvas.setSize(textWidth + 4, canvasHeight);
      }
      textHeight = metrics.getHeight();

      if (orientation.equals(Orientation.HORIZONTAL)) {
        idx = (int) (percent * (width - 1) + 0.5);
        if (direction.isStartToEnd()) {
          ix = x0 + idx;
        } else {
          ix = x0 + width - 1 - idx;
        }
        int ixa = 0;
        int iya = 0;
        if (_placement.equals(AxisPlacement.TOP)) {
          ixa = ix - textWidth / 2;
          iya = y1 - textHeight - 4;
        }
        if (_placement.equals(AxisPlacement.BOTTOM)) {
          ixa = ix - textWidth / 2;
          iya = y0 + 4;
        }
        ixa = Math.max(0, ixa);
        if (ixa + textWidth > width) {
          ixa = width - textWidth;
        }
        gc.drawText(text, ixa, iya);
      } else if (orientation.equals(Orientation.VERTICAL)) {
        idy = (int) (percent * (height - 1) + 0.5);
        if (direction.isStartToEnd()) {
          iy = y0 + height - 1 - idy;
        } else {
          iy = y0 + idy;
        }
        int ixa = 0;
        int iya = 0;
        if (_placement.equals(AxisPlacement.LEFT) && _placement.equals(AxisPlacement.LEFT)) {
          ixa = x1 - textWidth - 4;
          iya = iy - textHeight / 2;
        }
        if (_placement.equals(AxisPlacement.RIGHT) && _placement.equals(AxisPlacement.RIGHT)) {
          ixa = x0 + 4;
          iya = iy - textHeight / 2;
        }
        iya = Math.max(0, iya);
        if (iya + textHeight > height) {
          iya = height - textHeight;
        }
        gc.drawText(text, ixa, iya);
      }
    }
  }
}
