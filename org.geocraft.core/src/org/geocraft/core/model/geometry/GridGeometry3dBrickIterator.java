package org.geocraft.core.model.geometry;


public class GridGeometry3dBrickIterator {

  private GridGeometry3d _geom = null;

  private AxisIterationOrder _axisIterationOrder = AxisIterationOrder.ROW_COL;

  private final int _numRows;

  private final int _numCols;

  private int _axis1;

  private int _axis2;

  private final int[] _cursorStart = new int[2];

  private final int[] _cursorMaxShape = new int[2];

  private final int[] _maxBrickIndex = new int[2];

  private boolean _done = false;

  public enum AxisIterationOrder {
    ROW_COL,
    COL_ROW,
  };

  public GridGeometry3dBrickIterator(final GridGeometry3d geom) {
    _geom = geom;

    _numRows = _geom.getNumRows();
    _numCols = _geom.getNumColumns();

    _maxBrickIndex[0] = _numRows - 1;
    _maxBrickIndex[1] = _numCols - 1;

    setAxisIterationOrder(AxisIterationOrder.ROW_COL);

    reset();
  }

  public void setAxisIterationOrder(final AxisIterationOrder order) {
    _axisIterationOrder = order;

    switch (_axisIterationOrder) {
      case ROW_COL:
        _axis1 = 0; // slow axis
        _axis2 = 1; // fast axis
        break;
      case COL_ROW:
        _axis1 = 1;
        _axis2 = 0;
        break;
      default:
        break;
    }
  }

  public void setCursorMaxShape(final int[] cursorShape) {
    _cursorMaxShape[0] = cursorShape[0];
    _cursorMaxShape[1] = cursorShape[1];
  }

  public void setCursorMaxShape(final int rowNumValues, final int colNumValues) {
    _cursorMaxShape[0] = rowNumValues;
    _cursorMaxShape[1] = colNumValues;
  }

  public void setCursorMaxShape(final int maxNumValues) {
    // Automatically set the cursor shape such that a rectangular cursor is
    // created that favors orientation information, is rectangular,
    // has maxNumValues or fewer.

    long maxNumValuesL = maxNumValues;

    int c2 = _maxBrickIndex[_axis2] + 1;
    int c1 = _maxBrickIndex[_axis1] + 1;

    long c1L = c1;
    long c2L = c2;

    long c12L = c1L * c2L;

    if (maxNumValues < c2) {
      // Less than one trace worth.
      // set the cursor shape to <1,maxNumValues>
      _cursorMaxShape[_axis1] = 1;
      _cursorMaxShape[_axis2] = maxNumValues;
    } else if (maxNumValuesL < c12L) {
      // Less than one plane worth
      // set the cursor shape to <maxNumCursorValues / c2, c2>
      _cursorMaxShape[_axis1] = maxNumValues / c2;
      _cursorMaxShape[_axis2] = c2;
    } else {
      // bigger than the whole grid. Set it to the whole grid
      _cursorMaxShape[_axis1] = c1;
      _cursorMaxShape[_axis2] = c2;
    }
  }

  /**
   * Minimize cursor shape while maintaining same number of iterations
   */
  public void optimizeCursorMaxShape() {
    for (int i = 0; i < 2; i++) {
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
    _done = false;
  }

  public boolean hasNext() {
    return !_done;
  }

  public boolean next() {

    if (_cursorStart[_axis2] + _cursorMaxShape[_axis2] > _maxBrickIndex[_axis2]) {
      // Can't move in axis2, try axis 1
      if (_cursorStart[_axis1] + _cursorMaxShape[_axis1] > _maxBrickIndex[_axis1]) {
        // Can't move in axis1 => we're done
        _done = true;
        return false;
      }

      // Can move in axis1. Do it and reset axis2
      _cursorStart[_axis1] += _cursorMaxShape[_axis1];
      _cursorStart[_axis2] = 0;
    } else {
      // Can move in axis2.
      _cursorStart[_axis2] += _cursorMaxShape[_axis2];
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
    int[] shape = new int[2];
    int endIndex0 = Math.min(_cursorStart[0] + _cursorMaxShape[0] - 1, _maxBrickIndex[0]);
    shape[0] = endIndex0 - _cursorStart[0] + 1;
    int endIndex1 = Math.min(_cursorStart[1] + _cursorMaxShape[1] - 1, _maxBrickIndex[1]);
    shape[1] = endIndex1 - _cursorStart[1] + 1;
    return shape;
  }

  public int getCursorRowStart() {
    return _cursorStart[0];
  }

  public int getCursorRowEnd() {
    return Math.min(_cursorStart[0] + _cursorMaxShape[0] - 1, _maxBrickIndex[0]);
  }

  public int getCursorRowLength() {
    return getCursorRowEnd() - _cursorStart[0] + 1;
  }

  public int getCursorColumnStart() {
    return _cursorStart[1];
  }

  public int getCursorColumnEnd() {
    return Math.min(_cursorStart[1] + _cursorMaxShape[1] - 1, _maxBrickIndex[1]);
  }

  public int getCursorColumnLength() {
    return getCursorColumnEnd() - _cursorStart[1] + 1;
  }

  public int getNumIterations() {
    int n = _maxBrickIndex[0] / _cursorMaxShape[0] + 1;
    n *= _maxBrickIndex[1] / _cursorMaxShape[1] + 1;
    return n;
  }

  /**
   * @return
   */
  public AxisIterationOrder getAxisIterationOrder() {
    return _axisIterationOrder;
  }

}
