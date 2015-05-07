package beta.cop.model.grid;


import java.util.Iterator;



/**
 * Iterates through samples in a RegularGrid. 
 * Uses the underlying DistributedArray to provide the iterator.
 */

public class GridSampleIterator implements Iterator<int[]> {

  /** The regular grid that will be iterated over */
  private IRegularGrid _grid;

  /** The length of each axis (dimension) of the DistribtuedArray */
  private int[] _localLengths;

  /** The number of dimensions in the DistributedArray */
  private int _ndim;

  /** This array is used to convert between positions in the DistributedArray and
   * the offset in the backing array where that position is located
   */
  private long _offsetMultiplier[];

  /** Current local offset of this iterator */
  private long _offset = 0;

  /** Maximum local offset that this iterator can reach */
  private long _maxOffset;

  /** Global position corresponding to the current local offset */
  private int[] _position;

  /**
   * Creates a new DistributedArrayPositionIterator.
   * @param gridIn the grid over which to iterate.
   */
  public GridSampleIterator(IRegularGrid gridIn) {
    _grid = gridIn;
    _ndim = _grid.getNumDimensions();
    _position = new int[_ndim];
    //    _da = _grid.getGrid();
    _localLengths = _grid.getLocalLengths();
    // The offset multiplier is used to convert between an offset and a position
    // Based on logic used in Fortran multi-dimensional arrays
    _offsetMultiplier = new long[_ndim];
    _offsetMultiplier[0] = 1;
    for (int i = 1; i < _ndim; i++) {
      _offsetMultiplier[i] = _offsetMultiplier[i - 1] * _localLengths[i - 1];
    }
    // Calculate the maximum offset for this array
    _maxOffset = 1;
    for (int i = 0; i < _ndim; i++) {
      _maxOffset *= _localLengths[i];
    }
  }

  /**
   * Calculate the offset corresponding to the given position
   * @param position position in the distributed array for which an offset is needed
   * @return  offset offset in the distributed array corresponding to the given position
   */
  private long positionToOffset(int[] position) {
    long offset = 0;
    for (int i = 0; i < _ndim; i++) {
      offset += _offsetMultiplier[i] * _grid.globalToLocal(i, position[i]);
    }
    return offset;
  }

  /**
   * get the maximum offset 
   * @return maximum offset
   */
  private long getMaxOffset() {
    return _maxOffset;
  }

  // Part of the Iterator interface.
  public boolean hasNext() {
    if (_offset >= _maxOffset) {
      return false;
    }
    return true;
  }

  /**
   * Part of Iterator interface.  Determines the next position[] and loads this
   * information into the array supplied at the time this Iterator was constructed.
   *
   * @return  the next position.
   */
  public int[] next() {
    // TODO: What is the standard for hasNext() / next() ? Must they always be called in sequence ?
    offsetToPosition(_offset, _position);
    _offset++;
    return _position.clone();
  }

  /**
   * Return the position in the DistributedArray corresponding to a given offset
   * @param offset offset for which a position is desired
   * @param position output position corresponding to the given offset
   */
  public void offsetToPosition(long offset, int[] position) {
    long remainder = offset;
    for (int i = _ndim - 1; i >= 0; i--) {
      position[i] = (int) (remainder / _offsetMultiplier[i]);
      remainder = remainder % _offsetMultiplier[i];
      // convert from local to global
      position[i] = _grid.localToGlobal(i, position[i]);
    }

  }

  /**
   * Return a float sample from the current position
   * @return float value
   */
  public float getSampleFloat() {
    return _grid.getFloat(_position);
  }

  /**
   * Return an integer sample from the current position
   * @return integer value
   */
  public int getSampleInt() {
    return _grid.getInt(_position);
  }

  /** 
   * Return a double sample from the current position
   * @return double value
   */
  public double getSampleDouble() {
    return _grid.getDouble(_position);
  }

  /**
   * Store a float value at the current position
   * @param val value to store// 
   */
  public void putSampleFloat(float val) {
    _grid.putSample(val, _position);
  }

  /**
   * Store an int value at the current position
   * @param val value to store
   */
  public void putSampleInt(int val) {
    _grid.putSample(val, _position);
  }

  /**
   * Store a double value at the current position
   * @param val value to store
   */
  public void putSampleDouble(double val) {
    _grid.putSample(val, _position);
  }

  /**
   * Optional method in the Iterator interface -- not implemented.
   */
  public void remove() {
    throw new RuntimeException("Optional method Iterator.remove() is not implemented");
  }

  /**
   * not implemented.
   * @return
   */
  // public Iterator iterator() {
  //  return new GridSampleIterator(_grid);
  //}
}
