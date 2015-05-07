package org.geocraft.core.model;


public abstract class PointSetAction implements Runnable {

  private PointSet _pointSet;

  private int _pointIndex;

  protected void initialize(final PointSet pointSet, final int pointIndex) {
    _pointSet = pointSet;
    _pointIndex = pointIndex;
  }

  protected PointSet getPointSet() {
    return _pointSet;
  }

  protected int getPointIndex() {
    return _pointIndex;
  }

}
