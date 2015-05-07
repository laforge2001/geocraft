package org.geocraft.core.model.grid;


import java.util.Arrays;


public class CubeGridAreaOfInterest {

  private final CubeGridGeometry _geometry;

  private boolean[][] _validEntries = null;

  int _rows;

  int _cols;

  public CubeGridAreaOfInterest(final CubeGridGeometry geometry) {
    _geometry = geometry;
    _cols = geometry.getNumXGridValues();
    _rows = geometry.getNumYGridValues();
    _validEntries = new boolean[_rows][_cols];

    //initialize everything to false
    for (boolean[] b : _validEntries) {
      Arrays.fill(b, false);
    }
  }

  public boolean contains(final int row, final int col) {
    if (row < _rows && col < _cols) {
      return _validEntries[row][col];
    }
    return false;
  }

  public CubeGridGeometry getGeometry() {
    return _geometry;
  }

  public void updateEntries(final int row, final int col, final boolean isValid) {
    _validEntries[row][col] = isValid;
  }

  public int getNumRows() {
    return _rows;
  }

  public int getNumCols() {
    return _cols;
  }

}
