package org.geocraft.core.model.grid;


public class Grid2dInputBuffer {

  private final Grid2d _grid2d;

  private int _cachedRow;

  private float[] _valuesCache;

  public Grid2dInputBuffer(final Grid2d grid2d) {
    _grid2d = grid2d;
    _cachedRow = -1;
    _valuesCache = null;
  }

  public float[] getValues(final int row, final int startCol, final int length) {
    if (startCol == 0 && length == _grid2d.getGridGeometry().getNumColumns(row)) {
      // request is for the whole line
      int lineNumber = _grid2d.getGridGeometry().getLine(row).getNumber();
      return _grid2d.getValues(lineNumber);
    }
    if (_cachedRow != row) {
      int lineNumber = _grid2d.getGridGeometry().getLine(row).getNumber();
      _valuesCache = _grid2d.getValues(lineNumber);
      _cachedRow = row;
    }
    float[] values = new float[length];
    System.arraycopy(_valuesCache, startCol, values, 0, length);
    return values;
  }

  public float getValue(final int row, final int col) {
    if (_cachedRow != row) {
      int lineNumber = _grid2d.getGridGeometry().getLine(row).getNumber();
      _valuesCache = _grid2d.getValues(lineNumber);
      _cachedRow = row;
    }
    return _valuesCache[col];
  }
}
