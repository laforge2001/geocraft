/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.model;


import org.geocraft.core.common.math.MathUtil;
import org.geocraft.core.common.util.HashCode;
import org.geocraft.ui.plot.axis.AxisRange;


public class ModelSpaceBounds {

  private final AxisRange _xRange;

  private final AxisRange _yRange;

  private final AxisRange _zRange;

  public ModelSpaceBounds(final AxisRange xRange, final AxisRange yRange) {
    _xRange = xRange;
    _yRange = yRange;
    _zRange = new AxisRange(0, 0);
  }

  public ModelSpaceBounds(final double xmin, final double xmax, final double ymin, final double ymax) {
    _xRange = new AxisRange(xmin, xmax);
    _yRange = new AxisRange(ymin, ymax);
    _zRange = new AxisRange(0, 0);
  }

  public ModelSpaceBounds(final AxisRange xRange, final AxisRange yRange, final AxisRange zRange) {
    _xRange = xRange;
    _yRange = yRange;
    _zRange = zRange;
  }

  public ModelSpaceBounds(final double xmin, final double xmax, final double ymin, final double ymax, final double zmin, final double zmax) {
    _xRange = new AxisRange(xmin, xmax);
    _yRange = new AxisRange(ymin, ymax);
    _zRange = new AxisRange(zmin, zmax);
  }

  public AxisRange getRangeX() {
    return _xRange;
  }

  public AxisRange getRangeY() {
    return _yRange;
  }

  public AxisRange getRangeZ() {
    return _zRange;
  }

  public double getStartX() {
    return _xRange.getStart();
  }

  public double getEndX() {
    return _xRange.getEnd();
  }

  public double getStartY() {
    return _yRange.getStart();
  }

  public double getEndY() {
    return _yRange.getEnd();
  }

  public double getStartZ() {
    return _zRange.getStart();
  }

  public double getEndZ() {
    return _zRange.getEnd();
  }

  public boolean isValidInX() {
    return !Double.isNaN(_xRange.getStart()) && !Double.isNaN(_xRange.getEnd());
  }

  public boolean isValidInY() {
    return !Double.isNaN(_yRange.getStart()) && !Double.isNaN(_yRange.getEnd());
  }

  public boolean isValidInZ() {
    return !Double.isNaN(_zRange.getStart()) && !Double.isNaN(_zRange.getEnd());
  }

  public boolean isValidInXY() {
    return isValidInX() && isValidInY();
  }

  public boolean isValid() {
    return isValidInX() && isValidInY() && isValidInZ();
  }

  @Override
  public String toString() {
    return String.format("Model Space Bounds %f %f %f %f %f %f", getStartX(), getStartY(), getStartZ(), getEndX(),
        getEndY(), getEndZ());
  }

  @Override
  public boolean equals(final Object other) {

    if (other == null) {
      return false;
    } else if (!(other instanceof ModelSpaceBounds)) {
      return false;
    }

    ModelSpaceBounds bounds = (ModelSpaceBounds) other;

    if (!MathUtil.isEqual(getStartX(), bounds.getStartX())) {
      return false;
    } else if (!MathUtil.isEqual(getStartY(), bounds.getStartY())) {
      return false;
    } else if (!MathUtil.isEqual(getStartZ(), bounds.getStartZ())) {
      return false;
    } else if (!MathUtil.isEqual(getEndX(), bounds.getEndX())) {
      return false;
    } else if (!MathUtil.isEqual(getEndY(), bounds.getEndY())) {
      return false;
    } else if (!MathUtil.isEqual(getEndZ(), bounds.getEndZ())) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    HashCode hashCode = new HashCode();
    hashCode.add(getStartX());
    hashCode.add(getStartY());
    hashCode.add(getStartZ());
    hashCode.add(getEndX());
    hashCode.add(getEndY());
    hashCode.add(getEndZ());
    return hashCode.getHashCode();
  }
}
