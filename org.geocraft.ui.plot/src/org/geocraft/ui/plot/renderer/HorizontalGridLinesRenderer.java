/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.renderer;


import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.geocraft.core.common.util.Labels;
import org.geocraft.ui.plot.attribute.LineProperties;
import org.geocraft.ui.plot.axis.IAxis;
import org.geocraft.ui.plot.defs.AxisScale;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.Orientation;
import org.geocraft.ui.plot.model.ICoordinateTransform;


/**
 * This class renders a grid lines of a horizontal axis into a given rectangle.
 */
public class HorizontalGridLinesRenderer implements IGridLinesRenderer {

  /** The coordinate transform. */
  private final ICoordinateTransform _coordTransform;

  /**
   * Constructs a renderer for drawing grid lines of a horizontal
   * axis based on a given coordinate transform.
   * @param coordTransform the coordinate transform.
   */
  public HorizontalGridLinesRenderer(final ICoordinateTransform coordTransform) {
    _coordTransform = coordTransform;
  }

  public void render(final GC gc, final Rectangle rectangle, final IAxis axis, final LineProperties lineProperties,
      final int gridDensity) {
    // Ensure that the axis orientation is horizontal.
    if (!axis.getOrientation().equals(Orientation.HORIZONTAL)) {
      throw new IllegalArgumentException("Axis must be horizontal.");
    }

    // Turn off anti-aliasing.
    gc.setAntialias(SWT.OFF);
    gc.setTextAntialias(SWT.OFF);
    gc.setAdvanced(false);

    // Determine the min,max viewable range of the axis.
    double viewableStart = axis.getViewableStart();
    double viewableEnd = axis.getViewableEnd();
    double min = Math.min(viewableStart, viewableEnd);
    double max = Math.max(viewableStart, viewableEnd);

    // Determine the min,max range of the drawing rectangle.
    int y0 = 0;
    int y1 = rectangle.height;

    boolean drawStartAndEndOnly = false;

    // Update the GC with the line properties.
    int lineWidth = lineProperties.getWidth();
    LineStyle lineStyle = lineProperties.getStyle();
    Color lineColor = new Color(gc.getDevice(), lineProperties.getColor());
    gc.setForeground(lineColor);
    if (lineStyle.equals(LineStyle.SOLID)) {
      gc.setLineWidth(lineWidth);
      gc.setLineCap(SWT.CAP_FLAT);
      gc.setLineJoin(SWT.JOIN_ROUND);
    } else if (lineStyle.equals(LineStyle.DASHED)) {
      int[] dashes = { 2 };
      gc.setLineWidth(lineWidth);
      gc.setLineCap(SWT.CAP_FLAT);
      gc.setLineJoin(SWT.JOIN_ROUND);
      gc.setLineDash(dashes);
    } else {
      drawStartAndEndOnly = true;
    }
    lineColor.dispose();

    if (!drawStartAndEndOnly) {
      AxisScale scale = axis.getScale();
      switch (scale) {
        case LINEAR:
          // Compute the locations at which to draw the grid lines.
          double[] labels = Labels.computeLabels(min, max, gridDensity);
          int count = 1 + (int) Math.round((labels[1] - labels[0]) / labels[2]);

          // Draw grid lines at each of the locations.
          for (int i = 0; i < count; i++) {
            double value = labels[0] + i * labels[2];
            // Transform the axis location to a pixel location.
            double px = _coordTransform.transformModelToPixel(axis, value);
            int x = Math.round((float) px);
            gc.drawLine(x, y0, x, y1);
          }
          break;
        case LOG:
          min = Math.log(min);
          max = Math.log(max);
          double step = Math.log(10);
          double stepOrigin = Math.log(1);

          int index0 = (int) ((min - stepOrigin) / step);
          int index1 = (int) ((max - stepOrigin) / step);
          //          if (stepOrigin + index0 * step < min) {
          //            index0++;
          //          }
          //          if (stepOrigin + index1 * step > max) {
          //            index1--;
          //          }
          if (index0 == Integer.MAX_VALUE || index1 == Integer.MIN_VALUE) {
            return;
          }
          count = 1 + Math.abs(index1 - index0);
          for (int i = 0; i < count; i++) {
            double value = stepOrigin + (index0 + i) * step;
            value = Math.exp(value);
            // Transform the axis location to a pixel location.
            double px = _coordTransform.transformModelToPixel(axis, value);
            int x = Math.round((float) px);
            gc.drawLine(x, y0, x, y1);
            for (int j = 0; j < 10; j++) {
              double subvalue = value + j * value;
              px = _coordTransform.transformModelToPixel(axis, subvalue);
              x = Math.round((float) px);
              gc.drawLine(x, y0, x, y1);
            }
          }
          break;
        default:
          throw new RuntimeException("Invalid axis scale: " + scale);
      }
    }

    // Draw solid grid lines along the left and right sides.
    gc.setLineStyle(SWT.LINE_SOLID);
    int x = 0;
    gc.drawLine(x, y0, x, y1);
    x = rectangle.width - 1;
    gc.drawLine(x, y0, x, y1);
  }
}
