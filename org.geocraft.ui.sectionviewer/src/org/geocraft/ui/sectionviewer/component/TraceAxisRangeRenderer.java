/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.component;


import java.awt.geom.Point2D;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.geocraft.core.common.util.Labels;
import org.geocraft.core.model.datatypes.TraceAxisKey;
import org.geocraft.core.model.seismic.TraceSection;
import org.geocraft.ui.plot.IAxisRangeCanvas;
import org.geocraft.ui.plot.IAxisRangeRenderer;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.attribute.TextProperties;
import org.geocraft.ui.plot.axis.IAxis;
import org.geocraft.ui.plot.defs.AxisDirection;
import org.geocraft.ui.plot.defs.AxisPlacement;
import org.geocraft.ui.plot.model.IModelSpace;
import org.geocraft.ui.plot.model.IModelSpaceCanvas;
import org.geocraft.ui.plot.model.ModelSpaceBounds;
import org.geocraft.ui.sectionviewer.ISectionViewer;


/**
 * Renders the horizontal trace axis for the section viewer.
 */
public class TraceAxisRangeRenderer implements IAxisRangeRenderer {

  /** The section viewer to which the renderer is associated. */
  private final ISectionViewer _sectionViewer;

  /** The axis associated with the canvas. */
  protected IAxis _axis;

  /** The axis placement. */
  protected AxisPlacement _placement;

  protected int _increment = 1;

  protected boolean _scrolled = false;

  protected int _labelDensity = 10;

  protected IPlot _plot;

  protected IAxisRangeCanvas _canvas;

  public TraceAxisRangeRenderer(final ISectionViewer sectionViewer, final IAxisRangeCanvas canvas, final IAxis axis, final AxisPlacement placement) {
    _sectionViewer = sectionViewer;
    _canvas = canvas;
    _axis = axis;
    _placement = placement;
    _plot = sectionViewer.getPlot();
    _scrolled = _plot.getScrolling().hasHorizontal();
  }

  //TODO this could be simplified. Code review? 
  public void render(final GC gc, final Rectangle rectangle, final TextProperties textProperties, final int thumb,
      final int minimum, final int maximum, final int selection, final int sizeToSubtract) {
    TraceSection section = _sectionViewer.getTraceSection();
    if (section == null) {
      return;
    }
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
    AxisDirection direction = _axis.getDirection();
    double start = _axis.getViewableStart();
    double end = _axis.getViewableEnd();
    if (_scrolled) {
      double delta = maximum - minimum;
      Point modelSpaceCanvasSize = _plot.getModelSpaceCanvas().getSize();
      width -= sizeToSubtract;
      width = Math.min(width, modelSpaceCanvasSize.x);
      boolean switchPercents = false;
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

    int index0 = Integer.MAX_VALUE;
    int index1 = Integer.MIN_VALUE;
    double min = Math.min(start, end);
    double max = Math.max(start, end);
    double[] labels = Labels.computeLabels(min, max, _labelDensity);
    step = Math.abs(labels[2]);

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

    TraceAxisKey[] keys = section.getTraceAxisKeys();
    int startIndex = 0;
    if (section.is2D()) {
      startIndex = 1;
    }

    int numKeys = keys.length;
    for (int keyIndex = startIndex; keyIndex < numKeys; keyIndex++) {
      float[] keyValues = section.getTraceAxisKeyValues(keys[keyIndex]);
      for (int i = 0; i < count; i++) {
        double anno = stepOrigin + (index0 + i) * step;
        percent = (anno - start) / diff;
        percent = Math.abs(percent);

        Float flt = new Float(anno);
        float value = flt.floatValue();
        text = String.format(Labels.getFormat(flt), flt);
        int traceIndex = (int) anno - 1;
        if (traceIndex < 0 || traceIndex >= section.getNumTraces()) {
          continue;
        }
        text = "" + keyValues[traceIndex];
        textWidth = metrics.getAverageCharWidth() * text.length();

        textHeight = metrics.getHeight();

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
          int py0 = rectangle.height - numKeys * 2 - numKeys * metrics.getHeight() - 2;
          int py = py0 + keyIndex * 2 + keyIndex * metrics.getHeight();
          iya = py;
          gc.drawLine(ix, rectangle.y + rectangle.height - 4, ix, rectangle.y + rectangle.height);
        }
        if (_placement.equals(AxisPlacement.BOTTOM)) {
          ixa = ix - textWidth / 2;
          iya = y0 + 4;
          gc.drawLine(ix, rectangle.y, ix, rectangle.y + 4);
        }
        ixa = Math.max(0, ixa);
        if (ixa + textWidth > width) {
          ixa = width - textWidth;
        }
        gc.drawText(text, ixa, iya);
      }
    }
  }

  public void render2(final GC graphics, final Rectangle rectangle, final TextProperties textProperties,
      final int thumb, final int minimum, final int maximum, final int selection, final int sizeToSubtract) {
    // If the trace section currently in the viewer is null, then simply fill the rectangle
    // with the background color and then return.
    TraceSection section = _sectionViewer.getTraceSection();
    if (section == null) {
      graphics.fillRectangle(rectangle);
      return;
    }

    graphics.setFont(textProperties.getFont());
    FontMetrics metrics = graphics.getFontMetrics();

    int numTraces = section.getNumTraces();
    //SectionType sectionType = section.getSectionType();
    TraceAxisKey[] keys = section.getTraceAxisKeys();
    int numKeys = keys.length;

    IPlot plot = _sectionViewer.getPlot();
    IModelSpace modelSpace = plot.getActiveModelSpace();
    ModelSpaceBounds bounds = modelSpace.getViewableBounds();
    IModelSpaceCanvas canvas = plot.getModelSpaceCanvas();
    Point2D.Double pixelMin = new Point2D.Double(0, 0);
    canvas.transformModelToPixel(modelSpace, bounds.getStartX(), 0, pixelMin);
    Point2D.Double pixelMax = new Point2D.Double(0, 0);
    canvas.transformModelToPixel(modelSpace, bounds.getEndX(), 0, pixelMax);

    // Compute an estimate for the number of annotations to generate, based on
    // the width of the canvas and the width of the annotations.
    int textWidthMax = 0;
    for (int traceIndex = 0; traceIndex < numTraces; traceIndex++) {
      for (int keyIndex = 0; keyIndex < numKeys; keyIndex++) {
        float[] keyValues = section.getTraceAxisKeyValues(keys[keyIndex]);
        String valueStr = "" + keyValues[traceIndex];
        int textWidth = metrics.getAverageCharWidth() * valueStr.length();
        textWidthMax = Math.max(textWidth, textWidthMax);
      }
    }
    int numLabelsEstimate = rectangle.width / (2 * textWidthMax);
    if (numLabelsEstimate < 2) {
      numLabelsEstimate = 2;
    }

    // Compute the number and location of the traces to annotate.
    double[] labelValues = Labels.computeLabels(bounds.getStartX(), bounds.getEndX(), numLabelsEstimate);
    int traceNo0 = Math.round((float) labelValues[0]);
    int traceNo1 = Math.round((float) labelValues[1]);
    int traceNoDelta = Math.round((float) labelValues[2]);
    if (traceNoDelta == 0) {
      if (labelValues[2] < 0) {
        traceNoDelta = -1;
      } else {
        traceNoDelta = 1;
      }
    }
    int numLabels = 1 + (traceNo1 - traceNo0) / traceNoDelta;

    // Loop thru each of the trace axis keys, rendering their annotations.
    for (int keyIndex = 0; keyIndex < keys.length; keyIndex++) {
      float[] keyValues = section.getTraceAxisKeyValues(keys[keyIndex]);

      // Check that at least 1 of the traces in the section falls within the viewable bounds.
      // If not, then skipping the rendering of annotations.
      boolean anyTraceInBounds = false;
      for (int traceIndex = 0; traceIndex < numTraces; traceIndex++) {
        int traceNo = traceIndex + 1;
        if (traceNo >= bounds.getStartX() && traceNo < bounds.getEndX()) {
          anyTraceInBounds = true;
          break;
        }
      }
      if (!anyTraceInBounds) {
        continue;
      }

      // Render the annotations for those traces computed for annotation.
      boolean anyLabelsDrawn = false;
      int py0 = rectangle.height - numKeys * 2 - numKeys * metrics.getHeight() - 2;
      int py = py0 + keyIndex * 2 + keyIndex * metrics.getHeight();
      for (int k = -1; k <= numLabels; k++) {
        int traceNo = traceNo0 + k * traceNoDelta;
        int traceIndex = traceNo - 1;
        if (traceIndex >= 0 && traceIndex < numTraces) {
          String annotation = "" + keyValues[traceIndex];
          Point2D.Double pixel = new Point2D.Double(0, 0);
          canvas.transformModelToPixel(modelSpace, (traceIndex + 1), 0, pixel);
          if (numLabels > 1 || anyLabelsDrawn == false) {
            int textWidth = metrics.getAverageCharWidth() * annotation.length();
            int px0 = (int) (pixel.x - textWidth / 2);
            int px1 = (int) (pixel.x + textWidth / 2);
            if (px0 >= pixelMin.x && px1 <= pixelMax.x) {
              graphics.drawText(annotation, px0, py);
              if (keyIndex == numKeys - 1) {
                graphics.drawLine((int) pixel.x, rectangle.height - 3, (int) pixel.x, rectangle.height);
              }
              anyLabelsDrawn = true;
            }
          }
        }
      }
    }
  }
}
