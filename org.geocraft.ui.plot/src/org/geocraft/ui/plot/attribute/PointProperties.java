/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.attribute;


import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.common.model.AbstractBean;
import org.geocraft.ui.plot.defs.PointStyle;


public class PointProperties extends AbstractBean {

  /** The point style. */
  protected PointStyle _pointStyle;

  /** The point color (as RGB). */
  protected RGB _pointColor;

  /** The point size. */
  protected int _pointSize;

  /**
   * Constructs a default point properties (Style=FilledCircle, Color=Black, Size=1).
   */
  public PointProperties() {
    _pointStyle = PointStyle.FILLED_CIRCLE;
    _pointColor = new RGB(0, 0, 0);
    _pointSize = 1;
  }

  /**
   * Constructs a point properties, copied from the specified point properties.
   * @param pointProps the point properties to copy.
   */
  public PointProperties(final PointProperties pointProps) {
    this(pointProps.getStyle(), pointProps.getColor(), pointProps.getSize());
  }

  /**
   * Constructs a point properties.
   * @param style the point style.
   * @param color the point color.
   * @param size the point size.
   */
  public PointProperties(final PointStyle style, final RGB color, final int size) {
    setStyle(style);
    setColor(color);
    setSize(size);
  }

  public int getSize() {
    return _pointSize;
  }

  public PointStyle getStyle() {
    return _pointStyle;
  }

  public RGB getColor() {
    return _pointColor;
  }

  public void setSize(final int size) {
    firePropertyChange("pointSize", _pointSize, _pointSize = size);
  }

  public void setStyle(final PointStyle style) {
    firePropertyChange("pointStyle", _pointStyle, _pointStyle = style);
  }

  public void setColor(final RGB color) {
    firePropertyChange("pointColor", _pointColor, _pointColor = color);
  }

  public void dispose() {
    // No resources to dispose.
  }

}
