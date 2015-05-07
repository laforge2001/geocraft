/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.ellipse;


import java.text.NumberFormat;

import org.geocraft.abavo.ellipse.EllipseRegionsModel.EllipseType;
import org.geocraft.core.common.model.AbstractBean;


public class EllipseModel extends AbstractBean {

  public static final String TYPE = "type";

  public static final String SLOPE = "slope";

  public static final String LENGTH = "length";

  public static final String WIDTH = "width";

  public static final String CENTER_X = "centerX";

  public static final String CENTER_Y = "centerX";

  private EllipseType _type;

  private double _slope;

  private double _length;

  private double _width;

  private double _centerX;

  private double _centerY;

  private final NumberFormat _formatter;

  public EllipseModel(final EllipseType type, final double slope, final double length, final double width, final double centerX, final double centerY) {
    setType(type);
    setSlope(slope);
    setLength(length);
    setWidth(width);
    setCenterX(centerX);
    setCenterY(centerY);

    // Create a number formatter.
    _formatter = NumberFormat.getInstance();
    _formatter.setMaximumFractionDigits(3);
    _formatter.setGroupingUsed(false);
  }

  public EllipseType getType() {
    return _type;
  }

  public void setType(final EllipseType type) {
    firePropertyChange(TYPE, _type, _type = type);
  }

  public double getSlope() {
    return _slope;
  }

  public void setSlope(final double slope) {
    firePropertyChange(SLOPE, _slope, _slope = slope);
  }

  public double getLength() {
    return _length;
  }

  public void setLength(final double length) {
    firePropertyChange(LENGTH, _length, _length = length);
  }

  public double getWidth() {
    return _width;
  }

  public void setWidth(final double width) {
    firePropertyChange(WIDTH, _width, _width = width);
  }

  public double getCenterX() {
    return _centerX;
  }

  public void setCenterX(final double centerX) {
    firePropertyChange(CENTER_X, _centerX, _centerX = centerX);
  }

  public double getCenterY() {
    return _centerY;
  }

  public void setCenterY(final double centerY) {
    firePropertyChange(CENTER_Y, _centerY, _centerY = centerY);
  }

  public String getText(final int columnIndex) {
    switch (columnIndex) {
      case 0:
        return _type.toString();
      case 1:
        return _formatter.format(_slope);
      case 2:
        return _formatter.format(_length);
      case 3:
        return _formatter.format(_width);
      case 4:
        return _formatter.format(_centerX);
      case 5:
        return _formatter.format(_centerY);
      default:
        throw new IllegalArgumentException("Invalid column index.");
    }
  }
}
