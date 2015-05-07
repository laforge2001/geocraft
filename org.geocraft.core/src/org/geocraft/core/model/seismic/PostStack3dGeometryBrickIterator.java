/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.seismic;


import org.geocraft.core.model.datatypes.FloatRange;


//
// This iterator is used to optimize iteration over a PostStack3d volume's geometry.
// Information from the iterator can be used to perform geometric intersection, fetch
// data, etc.
//

public class PostStack3dGeometryBrickIterator {

  public enum AxisIterationOrder {
    AXIS1_AXIS2_AXIS3,
    AXIS1_AXIS3_AXIS2,
    AXIS2_AXIS1_AXIS3,
    AXIS2_AXIS3_AXIS1,
    AXIS3_AXIS1_AXIS2,
    AXIS3_AXIS2_AXIS1,
  };

  FloatRange _fullInlineRange;

  FloatRange _fullXlineRange;

  FloatRange _fullZRange;

  AxisIterationOrder _axisIterationOrder = AxisIterationOrder.AXIS1_AXIS2_AXIS3;

  int _axis1;

  int _axis2;

  int _axis3;

  int[] _cursorStart = new int[3];

  int[] _cursorMaxShape = new int[3];

  int[] _maxBrickIndex = new int[3];

  boolean _done = false;

  public PostStack3dGeometryBrickIterator(final FloatRange fullInlineRange, final FloatRange fullXlineRange, final FloatRange fullZRange) {
    _fullInlineRange = fullInlineRange;
    _fullXlineRange = fullXlineRange;
    _fullZRange = fullZRange;

    _maxBrickIndex[0] = fullInlineRange.getNumSteps() - 1;
    _maxBrickIndex[1] = fullXlineRange.getNumSteps() - 1;
    _maxBrickIndex[2] = fullZRange.getNumSteps() - 1;

    setAxisIterationOrder(AxisIterationOrder.AXIS1_AXIS2_AXIS3);

    reset();

  }

  public void setAxisIterationOrder(final AxisIterationOrder order) {
    _axisIterationOrder = order;

    switch (_axisIterationOrder) {
      case AXIS1_AXIS2_AXIS3:
        _axis1 = 0; // slow axis
        _axis2 = 1; // medium axis
        _axis3 = 2; // fast axis
        break;
      case AXIS1_AXIS3_AXIS2:
        _axis1 = 0;
        _axis2 = 2;
        _axis3 = 1;
        break;
      case AXIS2_AXIS1_AXIS3:
        _axis1 = 1;
        _axis2 = 0;
        _axis3 = 2;
        break;
      case AXIS2_AXIS3_AXIS1:
        _axis1 = 1;
        _axis2 = 2;
        _axis3 = 0;
        break;
      case AXIS3_AXIS1_AXIS2:
        _axis1 = 2;
        _axis2 = 0;
        _axis3 = 1;
        break;
      case AXIS3_AXIS2_AXIS1:
        _axis1 = 2;
        _axis2 = 1;
        _axis3 = 0;
        break;
      default:
        break;
    }
  }

  public void setCursorMaxShape(final int[] cursorShape) {
    _cursorMaxShape[0] = cursorShape[0];
    _cursorMaxShape[1] = cursorShape[1];
    _cursorMaxShape[2] = cursorShape[2];
  }

  public void setCursorMaxShape(final int ilNumValues, final int xlNumValues, final int zNumValues) {
    _cursorMaxShape[0] = ilNumValues;
    _cursorMaxShape[1] = xlNumValues;
    _cursorMaxShape[2] = zNumValues;
  }

  public void setCursorMaxShape(final int maxNumValues) {
    // Automatically set the cursor shape such that a rectangular cursor is
    // created that favors orientation information, is rectangular,
    // has maxNumValues or fewer.

    long maxNumValuesL = maxNumValues;

    int c3 = _maxBrickIndex[_axis3] + 1;
    int c2 = _maxBrickIndex[_axis2] + 1;
    int c1 = _maxBrickIndex[_axis1] + 1;

    long c1L = c1;
    long c2L = c2;
    long c3L = c3;

    long c23L = c2L * c3L;
    long c123L = c1L * c2L * c3L;

    if (maxNumValues < c3) {
      // Less than one trace worth.
      // set the cursor shape to <1,1,maxNumCursorValues>
      _cursorMaxShape[_axis1] = 1;
      _cursorMaxShape[_axis2] = 1;
      _cursorMaxShape[_axis3] = maxNumValues;
    } else if (maxNumValuesL < c23L) {
      // Less than one plane worth
      // set the cursor shape to <1, maxNumCursorValues / c3, c3>
      _cursorMaxShape[_axis1] = 1;
      _cursorMaxShape[_axis2] = maxNumValues / c3;
      _cursorMaxShape[_axis3] = c3;
    } else if (maxNumValuesL < c123L) {
      // bigger or equal to one plane, but less than the whole volume
      // set the cursor shape to <maxNumCursorValues / (c2*c3), c2, c3>
      _cursorMaxShape[_axis1] = (int) (maxNumValuesL / c23L);
      _cursorMaxShape[_axis2] = c2;
      _cursorMaxShape[_axis3] = c3;
    } else {
      // bigger than the whole volume. Set it to the whole volume
      _cursorMaxShape[_axis1] = c1;
      _cursorMaxShape[_axis2] = c2;
      _cursorMaxShape[_axis3] = c3;
    }
  }

  /**
   * Minimize cursor shape while maintaining same number of iterations
   */
  public void optimizeCursorMaxShape() {
    for (int i = 0; i < 3; i++) {
      int numIterations = _maxBrickIndex[i] / _cursorMaxShape[i] + 1;
      int r = numIterations * _cursorMaxShape[i] - (_maxBrickIndex[i] + 1);
      _cursorMaxShape[i] -= r / numIterations;
    }
  }

  public int[] getCursorMaxShape() {
    return _cursorMaxShape;
  }

  public void reset() {
    _cursorStart[0] = 0;
    _cursorStart[1] = 0;
    _cursorStart[2] = 0;
    _done = false;
  }

  public boolean hasNext() {
    return !_done;
  }

  public boolean next() {
    // Try moving in axis3 direction
    if (_cursorStart[_axis3] + _cursorMaxShape[_axis3] > _maxBrickIndex[_axis3]) {
      // Can't move in axis3
      if (_cursorStart[_axis2] + _cursorMaxShape[_axis2] > _maxBrickIndex[_axis2]) {
        // Can't move in axis2
        if (_cursorStart[_axis1] + _cursorMaxShape[_axis1] > _maxBrickIndex[_axis1]) {
          // Can't move in axis1 => we're done
          _done = true;
          return false;
        }

        // Can move in axis1. Do it and reset axis2 and axis3
        _cursorStart[_axis1] += _cursorMaxShape[_axis1];
        _cursorStart[_axis2] = 0;
        _cursorStart[_axis3] = 0;
      } else {
        // Can move in axis2. Do it and reset axis3
        _cursorStart[_axis2] += _cursorMaxShape[_axis2];
        _cursorStart[_axis3] = 0;
      }
    } else {
      // Can move in axis3, so do it
      _cursorStart[_axis3] += _cursorMaxShape[_axis3];
    }

    return true;
  }

  public int[] getCursorStart() {
    return _cursorStart;
  }

  /**
   * return the shape of the CURRENT cursor position.  The shape may NOT be max shape
   * when on volume edges.  Use this when processing bricks
   * @return
   */
  public int[] getCursorShape() {
    int[] shape = new int[3];
    int endIndex0 = Math.min(_cursorStart[0] + _cursorMaxShape[0] - 1, _maxBrickIndex[0]);
    shape[0] = endIndex0 - _cursorStart[0] + 1;
    int endIndex1 = Math.min(_cursorStart[1] + _cursorMaxShape[1] - 1, _maxBrickIndex[1]);
    shape[1] = endIndex1 - _cursorStart[1] + 1;
    int endIndex2 = Math.min(_cursorStart[2] + _cursorMaxShape[2] - 1, _maxBrickIndex[2]);
    shape[2] = endIndex2 - _cursorStart[2] + 1;
    return shape;
  }

  public float getCursorInlineStart() {
    return getCursorInlineRange().getStart();
  }

  public float getCursorInlineEnd() {
    return getCursorInlineRange().getEnd();
  }

  public FloatRange getCursorInlineRange() {
    float delta = _fullInlineRange.getDelta();
    float start = _fullInlineRange.getStart() + _cursorStart[0] * delta;
    int endIndex = Math.min(_cursorStart[0] + _cursorMaxShape[0] - 1, _maxBrickIndex[0]);
    float end = _fullInlineRange.getStart() + endIndex * delta;
    return new FloatRange(start, end, delta);
  }

  public float getCursorXlineStart() {
    return getCursorXlineRange().getStart();
  }

  public float getCursorXlineEnd() {
    return getCursorXlineRange().getEnd();
  }

  public FloatRange getCursorXlineRange() {
    float delta = _fullXlineRange.getDelta();
    float start = _fullXlineRange.getStart() + _cursorStart[1] * delta;
    int endIndex = Math.min(_cursorStart[1] + _cursorMaxShape[1] - 1, _maxBrickIndex[1]);
    float end = _fullXlineRange.getStart() + endIndex * delta;
    return new FloatRange(start, end, delta);
  }

  public float getCursorZStart() {
    return getCursorZRange().getStart();
  }

  public float getCursorZEnd() {
    return getCursorZRange().getEnd();
  }

  public FloatRange getCursorZRange() {
    float delta = _fullZRange.getDelta();
    float start = _fullZRange.getStart() + _cursorStart[2] * delta;
    int endIndex = Math.min(_cursorStart[2] + _cursorMaxShape[2] - 1, _maxBrickIndex[2]);
    float end = _fullZRange.getStart() + endIndex * delta;
    return new FloatRange(start, end, delta);
  }

  public int getNumIterations() {
    int n = _maxBrickIndex[0] / _cursorMaxShape[0] + 1;
    n *= _maxBrickIndex[1] / _cursorMaxShape[1] + 1;
    n *= _maxBrickIndex[2] / _cursorMaxShape[2] + 1;
    return n;
  }

  /**
   * @return
   */
  public AxisIterationOrder getAxisIterationOrder() {
    return _axisIterationOrder;
  }
}
