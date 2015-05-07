package org.geocraft.core.model.grid;


import org.geocraft.core.model.geometry.GridGeometry2d;


//
// Output buffer with manual control over writing to the Grid2d
//

public class Grid2dOutputBuffer {

  private float[][] _buffer;

  private final GridGeometry2d _geom;

  private final Grid2d _grid2d;

  public Grid2dOutputBuffer(final GridGeometry2d geom, final float initialValue, final Grid2d grid2d) {
    _geom = geom;
    _grid2d = grid2d;
    createBuffer(initialValue);
  }

  private void createBuffer(final float initialValue) {

    int numRows = _geom.getNumRows();

    _buffer = new float[numRows][];
    for (int r = 0; r < numRows; r++) {
      _buffer[r] = new float[_geom.getNumColumns(r)];
      for (int c = 0; c < _buffer[r].length; c++) {
        _buffer[r][c] = initialValue;
      }
    }
  }

  public void putValue(final int row, final int col, final float value) {
    _buffer[row][col] = value;
  }

  public void putValues(final int row, final int col, final float[] values) {
    System.arraycopy(values, 0, _buffer[row], col, values.length);
  }

  public void write(final int row) {
    int lineNumbers[] = _geom.getLineNumbers();
    _grid2d.putValues(lineNumbers[row], _buffer[row]);
  }

  public void writeAll() {
    int lineNumbers[] = _geom.getLineNumbers();
    for (int i = 0; i < lineNumbers.length; i++) {
      _grid2d.putValues(lineNumbers[i], _buffer[i]);
    }
  }
}
