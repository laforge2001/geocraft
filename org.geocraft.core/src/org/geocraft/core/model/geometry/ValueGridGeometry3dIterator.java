package org.geocraft.core.model.geometry;


public class ValueGridGeometry3dIterator {

  private ValueGridGeometry3d _mgeom = null;

  private int[][] _valueArray = null;

  private int _minRow;

  private int _maxRow;

  private int _minCol;

  private int _maxCol;

  private int _iterationValue;

  private int _row;

  private int _col;

  private int _nextRow;

  private int _nextCol;

  private boolean _atEnd;

  public ValueGridGeometry3dIterator(final ValueGridGeometry3d mgeom) {
    _mgeom = mgeom;
    _valueArray = _mgeom.getValueArray();
    _minRow = 0;
    _maxRow = _mgeom.getNumRows() - 1;
    _minCol = 0;
    _maxCol = _mgeom.getNumColumns() - 1;

    reset();
  }

  public void setIterationValue(final int iterationValue) {
    _iterationValue = iterationValue;
  }

  public int getIterationValue() {
    return _iterationValue;
  }

  public void reset() {
    _atEnd = false;
    _nextCol = _minCol - 1;
    _nextRow = _minRow;
    seekNext();
  }

  public boolean hasNext() {
    return !_atEnd;
  }

  private void seekNext() {
    _nextCol++;

    while (_nextRow <= _maxRow) {
      for (; _nextCol <= _maxCol; _nextCol++) {
        if (_valueArray[_nextRow][_nextCol] == _iterationValue) {
          return;
        }
      }
      _nextRow++;
      _nextCol = _minCol;
    }
    _atEnd = true;
  }

  public void next() {
    if (_atEnd) {
      return;
    }
    _row = _nextRow;
    _col = _nextCol;
    seekNext();
  }

  // set the value of the current iteration position.  This will
  // affect the associated MaskedGridGeometry2d since the mask is shared.
  public void set(final int v) {
    _valueArray[_row][_col] = v;
  }

  public int getRow() {
    return _row;
  }

  public int getCol() {
    return _col;
  }

  public double[] getXY() {
    return _mgeom.getGridGeometry().transformRowColToXY(_row, _col);
  }

  public void setRowRange(final int minRow, final int maxRow) {
    _minRow = minRow;
    _maxRow = maxRow;
  }

  public void setColumnRange(final int minCol, final int maxCol) {
    _minCol = minCol;
    _maxCol = maxCol;
  }

  public int minRow() {
    return _minRow;
  }

  public int maxRow() {
    return _maxRow;
  }

  public int minCol() {
    return _minCol;
  }

  public int maxCol() {
    return _maxCol;
  }

}
