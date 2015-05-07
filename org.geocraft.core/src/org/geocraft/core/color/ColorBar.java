/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.core.color;


import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.color.map.IColorMap;


/**
 * The basic implementation of a color bar.
 */
public class ColorBar extends ColorMapModel {

  /** Defines the possible editing modes. */
  public enum Mode {
    NONE,
    CIRCULAR,
    MARKER
  }

  /** Defines the sub modes for interacting with the dynamic marker. */
  public enum MarkerMode {
    NONE,
    MOTION,
    EXTEND_UP,
    EXTEND_DOWN
  }

  /** The start z value (probably the minimum value). */
  private double _startValue = Double.NaN;

  /** The end z value (probably the maximum value). */
  private double _endValue = Double.NaN;

  /** The interval between the colors. */
  private double _stepValue = Double.NaN;

  /** Flag indicating if start/end values are reversed. */
  private boolean _reversedRange = false;

  /** The null color. */
  protected RGB _nullColor = new RGB(0, 0, 0);

  /**
   * Constructs a color bar.
   * @param colorMapModel the color map model.
   * @param startValue the start value.
   * @param endValue the end value.
   * @param stepValue the step value.
   */
  public ColorBar(final ColorMapModel colorMapModel, final double startValue, final double endValue, final double stepValue) {
    super(colorMapModel);
    _startValue = startValue;
    _endValue = endValue;
    _stepValue = stepValue;
  }

  /**
   * Constructs a color bar, copied from another color bar.
   * @param colorBar the color bar to copy.
   */
  public ColorBar(final ColorBar colorBar) {
    this(colorBar, colorBar.getStartValue(), colorBar.getEndValue(), colorBar.getStepValue());
    setReversedRange(colorBar.isReversedRange());
  }

  /**
   * Construct a color bar.
   * @param colors the array of colors to use.
   * @param startValue the start value.
   * @param endValue the end value.
   * @param stepValue the step value.
   */
  public ColorBar(final RGB[] colors, final double startValue, final double endValue, final double stepValue) {
    super(colors);
    _startValue = startValue;
    _endValue = endValue;
    _stepValue = stepValue;
  }

  /**
   * Constructs a color bar.
   * @param numColors the number of colors.
   * @param colormap the color map.
   * @param direction the color map directions.
   * @param startValue the start value.
   * @param endValue the end value.
   * @param stepValue the step value.
   */
  public ColorBar(final int numColors, final IColorMap colormap, final double startValue, final double endValue, final double stepValue) {
    super(numColors, colormap);
    _startValue = startValue;
    _endValue = endValue;
    _stepValue = stepValue;
  }

  public double getStartValue() {
    return _startValue;
  }

  public double getEndValue() {
    return _endValue;
  }

  public double getStepValue() {
    return _stepValue;
  }

  public void setStartValue(final double startValue) {
    _startValue = startValue;
    updated();
  }

  public void setEndValue(final double endValue) {
    _endValue = endValue;
    updated();
  }

  public void setStepValue(final double stepValue) {
    _stepValue = stepValue;
    updated();
  }

  public void setRange(final double start, final double end, final double step) {
    _startValue = start;
    _endValue = end;
    _stepValue = step;
    updated();
  }

  public RGB getColor(final double value, final boolean truncate) {

    int index = getColorIndex(value);
    int ncolors = getNumColors();
    RGB result = _nullColor;

    if (index < 0) {
      if (truncate) {
        result = _colors[0];
      }
    } else if (index >= ncolors) {
      if (truncate) {
        result = _colors[ncolors - 1];
      }
    } else {
      result = _colors[index];
    }

    return result;
  }

  public int getColorIndex(final double value) {

    int index;
    double pcntg;
    int ncolors = getNumColors();

    // Find position in the zmin,max range.
    // TODO: think about this!
    double startValue = _startValue;
    double endValue = _endValue;
    if (_reversedRange) {
      startValue = _endValue;
      endValue = _startValue;
    }
    pcntg = (value - startValue) / (endValue - startValue);
    index = (int) Math.round(pcntg * (ncolors - 1));

    return index;
  }

  public boolean isReversedRange() {
    return _reversedRange;
  }

  public void setReversedRange(boolean reversedRange) {
    _reversedRange = reversedRange;
    updated();
  }

  @Override
  public void dispose() {
    // No action required.
  }

  public boolean isSame(final ColorBar other) {

    if (getStartValue() != other.getStartValue() || getEndValue() != other.getEndValue()) {
      return false;
    } else if (getNumColors() != other.getNumColors()) {
      return false;
    } else if (getStepValue() != other.getStepValue()) {
      return false;
    }

    if (!super.isSame(other)) {
      return false;
    }

    return true;
  }
}
