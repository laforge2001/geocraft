package org.geocraft.core.model.geometry;


public class MaskedGridGeometry2dIterator {

  private final MaskedGridGeometry2d _mgeom;

  private final boolean[][] _mask;

  private int _row;

  private int _col;

  private int _nextRow;

  private int _nextCol;

  private boolean _atEnd;

  //  public static void main(final String[] args) {
  //
  //    System.out.println("TEST MGG ITERATOR");
  //
  //    // create a test mask
  //    boolean[][] mask = new boolean[3][];
  //    mask[0] = new boolean[] { true, true, false, false, false, true };
  //    mask[1] = new boolean[] { false, true, false, false, false };
  //    mask[2] = new boolean[] { true, true, true };
  //
  //    MaskedGridGeometry2dIterator iter = new MaskedGridGeometry2dIterator(mask);
  //
  //    while (iter.hasNext()) {
  //      iter.next();
  //      System.out.println("row " + iter.getRow() + " col " + iter.getCol());
  //    }
  //
  //    System.out.println("TEST MGG ITERATOR -- END");
  //  }

  public MaskedGridGeometry2dIterator(final MaskedGridGeometry2d mgeom) {
    _mgeom = mgeom;
    _mask = _mgeom.getMask();
    reset();
  }

  public MaskedGridGeometry2dIterator(final boolean[][] mask) {
    _mask = mask;
    _mgeom = null;
    reset();
  }

  public void reset() {
    _atEnd = false;
    _nextCol = -1;
    _nextRow = 0;
    seekNext();
  }

  public boolean hasNext() {
    return !_atEnd;
  }

  // find the next value.  If we can't find the
  // next value, set atEnd to true;
  private void seekNext() {
    _nextCol++;
    // check rest of this row
    while (_nextRow < _mask.length) {
      for (; _nextCol < _mask[_nextRow].length; _nextCol++) {
        if (_mask[_nextRow][_nextCol] == true) {
          // found it
          return;
        }
      }
      _nextRow++;
      _nextCol = 0;
    }

    // no next value found
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
  public void set(final boolean b) {
    _mask[_row][_col] = b;
  }

  public int getRow() {
    return _row;
  }

  public int getCol() {
    return _col;
  }

  public double[] getXY() {
    return _mgeom.getGridGeometry().getLine(_row).transformBinToXY(_col);
  }
}
