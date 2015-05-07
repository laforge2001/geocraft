/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.internal;


import java.beans.PropertyChangeEvent;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ScrollBar;
import org.geocraft.ui.plot.IAxisRangeCanvas;
import org.geocraft.ui.plot.IAxisRangeRenderer;
import org.geocraft.ui.plot.IPlot;
import org.geocraft.ui.plot.axis.IAxis;
import org.geocraft.ui.plot.defs.AxisPlacement;
import org.geocraft.ui.plot.layout.CanvasLayoutModel;


/**
 * The basic implementation of a plot axis canvas.
 */
public class AxisRangeCanvas extends PlotCanvas implements IAxisRangeCanvas {

  /** The axis associated with the canvas. */
  protected IAxis _axis;

  /** The axis placement. */
  protected AxisPlacement _placement;

  protected int _thumb = 0;

  protected int _minimum = 0;

  protected int _maximum = 1;

  protected int _selection = 0;

  protected int _widthToSubtract = 0;

  protected int _labelDensity = 5;

  protected IAxisRangeRenderer _renderer;

  /**
   * Constructs an axis canvas with the specified axis and layout.
   * @param axis the axis to associate with the canvas.
   * @param layout the plot layout.
   */
  public AxisRangeCanvas(final Composite parent, final IPlot plot, final IAxis axis, final AxisPlacement placement, final CanvasLayoutModel layoutModel) {
    super(parent, plot, SWT.NONE);
    _renderer = new DefaultAxisRangeRenderer(this, plot, axis, placement, layoutModel);
    _axis = axis;
    _placement = placement;
    Font font = new Font(null, "SansSerif", 8, SWT.NORMAL);
    _textProperties.setFont(font);
    font.dispose();

    GridData constraints = new GridData();
    constraints.horizontalSpan = 1;
    constraints.verticalSpan = 1;
    constraints.horizontalAlignment = SWT.FILL;
    constraints.verticalAlignment = SWT.FILL;
    if (placement.equals(AxisPlacement.TOP)) {
      constraints.grabExcessHorizontalSpace = true;
      constraints.grabExcessVerticalSpace = false;
      constraints.heightHint = layoutModel.getTopAxisHeight();
    } else if (placement.equals(AxisPlacement.LEFT)) {
      constraints.grabExcessHorizontalSpace = false;
      constraints.grabExcessVerticalSpace = true;
      constraints.widthHint = layoutModel.getLeftAxisWidth();
    } else if (placement.equals(AxisPlacement.RIGHT)) {
      constraints.grabExcessHorizontalSpace = false;
      constraints.grabExcessVerticalSpace = true;
      constraints.widthHint = layoutModel.getRightAxisWidth();
    } else if (placement.equals(AxisPlacement.BOTTOM)) {
      constraints.grabExcessHorizontalSpace = true;
      constraints.grabExcessVerticalSpace = false;
      constraints.heightHint = layoutModel.getBottomAxisHeight();
    } else {
      throw new IllegalArgumentException("Invalid axis placement: " + placement);
    }
    setLayoutData(constraints);
    //    _formatter.setMaximumIntegerDigits(1);
    //    _formatter.setMinimumIntegerDigits(1);
    //    _formatter.setMaximumIntegerDigits(5);
    //    _formatter.setMinimumIntegerDigits(1);
  }

  public IAxis getAxis() {
    return _axis;
  }

  public void setAxis(final IAxis axis) {
    _axis = axis;
    redraw();
  }

  public void setLabelDensity(final int labelDensity) {
    _labelDensity = labelDensity;
  }

  public int getLabelDensity() {
    return _labelDensity;
  }

  public void render(final GC graphics, final Rectangle rectangle, final boolean isShown) {
    graphics.setBackground(getBackground());
    graphics.fillRectangle(rectangle);
    if (isShown) {
      //render(graphics, rectangle);
      _renderer.render(graphics, rectangle, _textProperties, _thumb, _minimum, _maximum, _selection, _widthToSubtract);
    }
  }

  //  // TODO this could be simplified. Code review? 
  //  public void render(final GC gc, final Rectangle rectangle) {
  //    int count = 0;
  //    int ix = 0;
  //    int idx;
  //    int iy = 0;
  //    int idy;
  //    int width = rectangle.width;
  //    int height = rectangle.height;
  //    double percent;
  //    String text;
  //    int textWidth = 0;
  //    int textHeight = 0;
  //    AxisScale scale = _axis.getScale();
  //    Orientation orientation = _axis.getOrientation();
  //    AxisDirection direction = _axis.getDirection();
  //    double start = _axis.getViewableStart();
  //    double end = _axis.getViewableEnd();
  //    if (_scrolled) {
  //      double delta = _maximum - _minimum;
  //      Point modelSpaceCanvasSize = _plot.getModelSpaceCanvas().getSize();
  //      if (_axis.getOrientation().equals(Orientation.HORIZONTAL)) {
  //        width -= _widthToSubtract;
  //        width = Math.min(width, modelSpaceCanvasSize.x);
  //      } else if (_axis.getOrientation().equals(Orientation.VERTICAL)) {
  //        height -= _widthToSubtract;
  //        height = Math.min(height, modelSpaceCanvasSize.y);
  //      }
  //      boolean switchPercents = false;
  //      if (orientation.equals(Orientation.VERTICAL)) {
  //        switchPercents = !switchPercents;
  //      }
  //      if (!direction.isStartToEnd()) {
  //        switchPercents = !switchPercents;
  //      }
  //      double startPercent = _selection / delta;
  //      double endPercent = (_selection + _thumb) / delta;
  //      if (switchPercents) {
  //        startPercent = (delta - _selection - _thumb) / delta;
  //        endPercent = (delta - _selection) / delta;
  //      }
  //      double length = end - start;
  //      double startTemp = start + startPercent * length;
  //      double endTemp = start + endPercent * length;
  //      start = startTemp;
  //      end = endTemp;
  //    }
  //    double step = 10;
  //    double stepOrigin = 0;
  //    double diff = end - start;
  //    Font textFont = _textProperties.getFont();
  //    Color textColor = new Color(gc.getDevice(), _textProperties.getColor());
  //    FontMetrics metrics;
  //    int x0 = rectangle.x;
  //    int y0 = rectangle.y;
  //    int x1 = x0 + width - 1;
  //    int y1 = y0 + height - 1;
  //
  //    // If step value is <= zero, do not draw.
  //    if (step <= 0) {
  //      return;
  //    }
  //    if (scale.equals(AxisScale.LOG)) {
  //      start = Math.log(start);
  //      end = Math.log(end);
  //      step = Math.log(step);
  //      stepOrigin = 1;
  //      stepOrigin = Math.log(stepOrigin);
  //      diff = end - start;
  //    }
  //
  //    int index0 = Integer.MAX_VALUE;
  //    int index1 = Integer.MIN_VALUE;
  //    double min = Math.min(start, end);
  //    double max = Math.max(start, end);
  //    double[] labels = Labels.computeLabels(min, max, _labelDensity);
  //    if (scale.equals(AxisScale.LINEAR)) {
  //      step = Math.abs(labels[2]);
  //    } else if (scale.equals(AxisScale.LOG)) {
  //      step = Math.log(10);
  //    }
  //
  //    index0 = (int) ((min - stepOrigin) / step);
  //    index1 = (int) ((max - stepOrigin) / step);
  //    if (stepOrigin + index0 * step < min) {
  //      index0++;
  //    }
  //    if (stepOrigin + index1 * step > max) {
  //      index1--;
  //    }
  //    if (index0 == Integer.MAX_VALUE || index1 == Integer.MIN_VALUE) {
  //      return;
  //    }
  //    count = 1 + Math.abs(index1 - index0);
  //
  //    gc.setAntialias(SWT.OFF);
  //    gc.setTextAntialias(SWT.ON);
  //    gc.setFont(textFont);
  //    gc.setForeground(textColor);
  //    textColor.dispose();
  //    metrics = gc.getFontMetrics();
  //    int canvasWidth = getSize().x;
  //    int canvasHeight = getSize().y;
  //    for (int i = 0; i < count; i++) {
  //      double anno = stepOrigin + (index0 + i) * step;
  //      percent = (anno - start) / diff;
  //      percent = Math.abs(percent);
  //      if (scale.equals(AxisScale.LOG)) {
  //        anno = Math.exp(anno);
  //      }
  //
  //      Float flt = new Float(anno);
  //      float value = flt.floatValue();
  //      text = String.format(Labels.getFormat(flt), flt);
  //      textWidth = metrics.getAverageCharWidth() * text.length();
  //      //if (orientation.equals(Orientation.VERTICAL) && textWidth > canvasWidth) {
  //      //  text = _formatter.format(flt.floatValue());
  //      //}
  //      //textWidth = metrics.getAverageCharWidth() * text.length();
  //      if (orientation.equals(Orientation.VERTICAL) && textWidth >= canvasWidth) {
  //        setSize(textWidth + 4, canvasHeight);
  //      }
  //      textHeight = metrics.getHeight();
  //
  //      if (orientation.equals(Orientation.HORIZONTAL)) {
  //        idx = (int) (percent * (width - 1) + 0.5);
  //        if (direction.isStartToEnd()) {
  //          ix = x0 + idx;
  //        } else {
  //          ix = x0 + width - 1 - idx;
  //        }
  //        int ixa = 0;
  //        int iya = 0;
  //        if (_placement.equals(AxisPlacement.TOP)) {
  //          ixa = ix - textWidth / 2;
  //          iya = y1 - textHeight - 4;
  //        }
  //        if (_placement.equals(AxisPlacement.BOTTOM)) {
  //          ixa = ix - textWidth / 2;
  //          iya = y0 + 4;
  //        }
  //        ixa = Math.max(0, ixa);
  //        if (ixa + textWidth > width) {
  //          ixa = width - textWidth;
  //        }
  //        gc.drawText(text, ixa, iya);
  //      } else if (orientation.equals(Orientation.VERTICAL)) {
  //        idy = (int) (percent * (height - 1) + 0.5);
  //        if (direction.isStartToEnd()) {
  //          iy = y0 + height - 1 - idy;
  //        } else {
  //          iy = y0 + idy;
  //        }
  //        int ixa = 0;
  //        int iya = 0;
  //        if (_placement.equals(AxisPlacement.LEFT) && _placement.equals(AxisPlacement.LEFT)) {
  //          ixa = x1 - textWidth - 4;
  //          iya = iy - textHeight / 2;
  //        }
  //        if (_placement.equals(AxisPlacement.RIGHT) && _placement.equals(AxisPlacement.RIGHT)) {
  //          ixa = x0 + 4;
  //          iya = iy - textHeight / 2;
  //        }
  //        iya = Math.max(0, iya);
  //        if (iya + textHeight > height) {
  //          iya = height - textHeight;
  //        }
  //        gc.drawText(text, ixa, iya);
  //      }
  //    }
  //  }

  public void canvasLayoutUpdated(final CanvasLayoutModel layout) {
    // TODO Auto-generated method stub
  }

  public void paintControl(final PaintEvent event) {
    Point size = getSize();
    render(event.gc, new Rectangle(0, 0, size.x, size.y), true);
  }

  public AxisPlacement getPlacement() {
    return _placement;
  }

  public void scrolled(final ScrollBar scrollBar, final int widthToSubtract) {
    _thumb = scrollBar.getThumb();
    _minimum = scrollBar.getMinimum();
    _maximum = scrollBar.getMaximum();
    _selection = scrollBar.getSelection();
    _widthToSubtract = widthToSubtract;
    redraw();
    update();
  }

  @Override
  public void redraw() {
    _redraw = true;
    super.redraw();
  }

  @Override
  public void propertyChange(final PropertyChangeEvent evt) {
    // TODO Auto-generated method stub

  }

  public void setTextProperties(final Font textFont, final RGB textColor) {
    _textProperties.setFont(textFont);
    _textProperties.setColor(textColor);
  }

  @Override
  public void scrolled(final ScrollBar scrollBar) {
    scrolled(scrollBar, 0);
  }

  public void setRenderer(IAxisRangeRenderer renderer) {
    _renderer = renderer;
  }

}
