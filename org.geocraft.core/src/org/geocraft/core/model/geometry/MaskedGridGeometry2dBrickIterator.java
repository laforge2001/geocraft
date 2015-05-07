package org.geocraft.core.model.geometry;


//
// Implement the brick iterator by using the MaskedGridGeometry2dIterator
//

/**
 * Iterate over a masked 2d grid.  Each iteration position represents a contiguous chunk of
 * data on the same row.
 */
public class MaskedGridGeometry2dBrickIterator {

  private final MaskedGridGeometry2d _mgeom;

  private final boolean[][] _mask;

  private int _row;

  private int _startCol;

  private int _length;

  private int _nextRow;

  private int _nextStartCol;

  private int _nextLength;

  private int _maxBrickSize = 1000;

  private boolean _atEnd;

  public static void main(final String[] args) {

    System.out.println("TEST MGG BRICK ITERATOR");

    // create a test mask
    boolean[][] mask = new boolean[3][];
    mask[0] = new boolean[] { true, false, true, false, true, false };
    mask[1] = new boolean[] { true, true, true, true, true };
    mask[2] = new boolean[] { true, false, false };

    MaskedGridGeometry2dBrickIterator iter = new MaskedGridGeometry2dBrickIterator(mask);

    iter.setMaxBrickSize(10);
    System.out.println("max brick size = 10");
    while (iter.hasNext()) {
      iter.next();
      System.out.println("row " + iter.getRow() + " startCol " + iter.getStartCol() + " length " + iter.getLength());
    }

    iter.reset();
    iter.setMaxBrickSize(2);

    System.out.println("max brick size = 2");
    while (iter.hasNext()) {
      iter.next();
      System.out.println("row " + iter.getRow() + " startCol " + iter.getStartCol() + " length " + iter.getLength());
      System.out.println("isLastOfRow = " + iter.isLastOfRow());
    }
    System.out.println("TEST MGG BRICK ITERATOR -- END");
  }

  public MaskedGridGeometry2dBrickIterator(final MaskedGridGeometry2d mgeom) {
    _mgeom = mgeom;
    _mask = _mgeom.getMask();
    reset();
  }

  public MaskedGridGeometry2dBrickIterator(final boolean[][] mask) {
    _mgeom = null;
    _mask = mask;
    reset();
  }

  public void setMaxBrickSize(final int v) {
    _maxBrickSize = v;
  }

  public void reset() {
    _atEnd = false;
    _nextRow = 0;
    _nextStartCol = -1;
    _nextLength = 1;
    seekNext();
  }

  public boolean hasNext() {
    return !_atEnd;
  }

  // find the next value.  If we can't find the
  // next value, set atEnd to true;
  private void seekNext() {
    _nextStartCol += _nextLength;
    // check rest of this row
    while (_nextRow < _mask.length) {
      for (; _nextStartCol < _mask[_nextRow].length; _nextStartCol++) {
        if (_mask[_nextRow][_nextStartCol] == true) {
          // found the start column.  How long is the run of true values?
          _nextLength = 1;
          while (_nextStartCol + _nextLength < _mask[_nextRow].length && _nextLength < _maxBrickSize
              && _mask[_nextRow][_nextStartCol + _nextLength] == true) {
            _nextLength++;
          }
          return;
        }
      }
      _nextRow++;
      _nextStartCol = 0;
    }

    // no next value found
    _atEnd = true;
  }

  public void next() {
    if (_atEnd) {
      return;
    }
    _row = _nextRow;
    _startCol = _nextStartCol;
    _length = _nextLength;
    seekNext();
  }

  /**
   * Set the mask value within the brick to b
   * @param offset
   * @param b
   */
  public void set(final int offset, final boolean b) {
    _mask[_row][_startCol + offset] = b;
  }

  public double[] getXY(final int offset) {
    return _mgeom.getGridGeometry().getLine(_row).transformBinToXY(_startCol + offset);
  }

  public int getRow() {
    return _row;
  }

  public int getStartCol() {
    return _startCol;
  }

  public int getLength() {
    return _length;
  }

  public int getLineNumber() {
    return _mgeom.getGridGeometry().getLine(_row).getNumber();
  }

  public boolean isLastOfRow() {
    if (_atEnd) {
      return true;
    }
    if (_nextRow == _row) {
      // there is more on this row
      return false;
    }
    return true;
  }
}
