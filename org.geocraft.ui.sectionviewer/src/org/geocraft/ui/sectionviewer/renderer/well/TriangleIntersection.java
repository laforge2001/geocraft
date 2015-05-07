/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.renderer.well;


public class TriangleIntersection {

  private final boolean _exists;

  private double _tracePercentage0;

  private double _tracePercentage1;

  private double _zPercentage0;

  private double _zPercentage1;

  public TriangleIntersection() {
    _exists = false;
  }

  public TriangleIntersection(final double tracePercentage0, final double tracePercentage1, final double zPercentage0, final double zPercentage1) {
    _exists = true;
    _tracePercentage0 = tracePercentage0;
    _tracePercentage1 = tracePercentage1;
    _zPercentage0 = zPercentage0;
    _zPercentage1 = zPercentage1;
  }

  public boolean exists() {
    return _exists;
  }

  public double[] getTracePercentages() {
    return new double[] { _tracePercentage0, _tracePercentage1 };
  }

  public double[] getZValues() {
    return new double[] { _zPercentage0, _zPercentage1 };
  }

  @Override
  public String toString() {
    return _exists + " " + _tracePercentage0 + " " + _tracePercentage1 + " " + _zPercentage0 + " " + _zPercentage1;
  }
}
