/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.attribute;


import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.common.model.AbstractBean;
import org.geocraft.ui.plot.defs.LineStyle;


/**
 * Simple implementation of the plot line properties interface.
 */
public class LineProperties extends AbstractBean {

  /** The line style. */
  private LineStyle _lineStyle;

  /** The line color (as RGB). */
  private RGB _lineColor;

  /** The line width. */
  private int _lineWidth;

  /**
   * The empty constructor.
   */
  public LineProperties() {
    _lineStyle = LineStyle.SOLID;
    _lineColor = new RGB(0, 0, 0);
    _lineWidth = 1;
  }

  /**
   * The default constructor.
   * @param style the line style.
   * @param color the line color.
   * @param width the line width.
   */
  public LineProperties(final LineStyle style, final RGB color, final int width) {
    setStyle(style);
    setColor(color);
    setWidth(width);
  }

  /**
   * The copy constructor.
   * 
   * @param lineProps the line properties to copy.
   */
  public LineProperties(final LineProperties lineProps) {
    this(lineProps.getStyle(), lineProps.getColor(), lineProps.getWidth());
  }

  public LineStyle getStyle() {
    return _lineStyle;
  }

  public int getWidth() {
    return _lineWidth;
  }

  public RGB getColor() {
    return _lineColor;
  }

  public void setColor(final RGB color) {
    firePropertyChange("lineColor", _lineColor, _lineColor = color);
  }

  public void setStyle(final LineStyle style) {
    firePropertyChange("lineStyle", _lineStyle, _lineStyle = style);
  }

  public void setWidth(final int width) {
    firePropertyChange("lineWidth", _lineWidth, _lineWidth = width);
  }

  public void dispose() {
    // No resources to dispose.
  }
}
