/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.ellipse;


import java.text.NumberFormat;


public class RegionsBoundaryModel {

  public static final String ID = "id";

  public static final String OUTER_X = "outerX";

  public static final String OUTER_Y = "outerY";

  public static final String INNER_X = "innerX";

  public static final String INNER_Y = "innerY";

  private RegionsBoundary _id;

  private double _outerX;

  private double _outerY;

  private double _innerX;

  private double _innerY;

  private final NumberFormat _formatter;

  public RegionsBoundaryModel(final RegionsBoundary id, final double outerX, final double outerY, final double innerX, final double innerY) {
    _id = id;
    _outerX = outerX;
    _outerY = outerY;
    _innerX = innerY;
    _innerY = innerY;

    // Create a number formatter.
    _formatter = NumberFormat.getInstance();
    _formatter.setMaximumFractionDigits(3);
    _formatter.setGroupingUsed(false);
  }

  public RegionsBoundary getId() {
    return _id;
  }

  public void setId(final RegionsBoundary id) {
    _id = id;
  }

  public double getOuterX() {
    return _outerX;
  }

  public void setOuterX(final double outerX) {
    _outerX = outerX;
  }

  public double getOuterY() {
    return _outerY;
  }

  public void setOuterY(final double outerY) {
    _outerY = outerY;
  }

  public double getInnerX() {
    return _innerX;
  }

  public void setInnerX(final double innerX) {
    _innerX = innerX;
  }

  public double getInnerY() {
    return _innerY;
  }

  public void setInnerY(final double innerY) {
    _innerY = innerY;
  }

  public String getText(final int columnIndex) {
    switch (columnIndex) {
      case 0:
        return _id.getName();
      case 1:
        return _formatter.format(_outerX);
      case 2:
        return _formatter.format(_outerY);
      case 3:
        return _formatter.format(_innerX);
      case 4:
        return _formatter.format(_innerY);
      default:
        throw new IllegalArgumentException("Invalid column index.");
    }
  }

}
