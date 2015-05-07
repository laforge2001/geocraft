package org.geocraft.core.model.geometry;


import org.geocraft.core.model.grid.Grid2d;
import org.geocraft.core.model.seismic.PostStack2d;


public class MaskedGridGeometry2d {

  private final GridGeometry2d _geom;

  private final boolean[][] _mask;

  // create from a grid geometry.  Set all values to true
  public MaskedGridGeometry2d(final GridGeometry2d geom) {
    _geom = geom;

    int numRows = _geom.getNumRows();
    _mask = new boolean[numRows][];
    for (int r = 0; r < numRows; r++) {
      _mask[r] = new boolean[_geom.getNumColumns(r)];
      for (int c = 0; c < _mask[r].length; c++) {
        _mask[r][c] = true;
      }
    }
  }

  // Create from Grid2d.  The mask is true only where the grid2d has a non-null value
  public MaskedGridGeometry2d(final Grid2d grid2d) {

    _geom = grid2d.getGridGeometry();

    // lines are rows, traces are cols
    int numRows = _geom.getNumRows();
    _mask = new boolean[numRows][];
    float nullValue = grid2d.getNullValue();
    for (int row = 0; row < numRows; row++) {
      LineGeometry lg = _geom.getLine(row);
      float[] values = grid2d.getValues(lg.getNumber());
      _mask[row] = new boolean[values.length];
      for (int col = 0; col < values.length; col++) {
        _mask[row][col] = (values[col] != nullValue);
      }
    }
  }

  private int lineNumberToIndex(final int lineNumber) {
    int[] lineNumbers = _geom.getLineNumbers();
    for (int i = 0; i < lineNumbers.length; i++) {
      if (lineNumber == lineNumbers[i]) {
        return i;
      }
    }
    return -1;
  }

  // get reference to the mask.  Should not edit this
  public boolean[][] getMask() {
    return _mask;
  }

  public boolean[] getValues(final int lineNumber) {
    int row = lineNumberToIndex(lineNumber);
    return _mask[row];
  }

  public GridGeometry2d getGridGeometry() {
    return _geom;
  }

  public void intersectWithPostStack2d(final PostStack2d ps2d) {
    // loop through line numbers we have.
    int[] myLineNumbers = _geom.getLineNumbers();
    for (int r = 0; r < _geom.getNumRows(); r++) {
      if (!ps2d.containsPostStack2d(myLineNumbers[r])) {
        setRowToFalse(r);
      }
    }
  }

  public boolean containsLineNumber(final int lineNumber) {
    return (lineNumberToIndex(lineNumber) >= 0);
  }

  public void setRowToFalse(final int r) {
    for (int c = 0; c < _mask[r].length; c++) {
      _mask[r][c] = false;
    }
  }

  public void setLineToFalse(final int lineNumber) {
    int r = lineNumberToIndex(lineNumber);
    if (r != -1) {
      for (int c = 0; c < _mask[r].length; c++) {
        _mask[r][c] = false;
      }
    }
  }

  // filter the current mask with the provided mask
  public void unaryAnd(final MaskedGridGeometry2d mg2d) {
    // Loop over line geometries in mg2d.  If line numbers match, process
    // them
    int[] lineNumbers = _geom.getLineNumbers();
    for (int r = 0; r < lineNumbers.length; r++) {
      try {
        LineGeometry lg = mg2d.getGridGeometry().getLineByNumber(lineNumbers[r]);
        boolean[] values = mg2d.getValues(lineNumbers[r]);
        for (int c = 0; c < _mask[r].length; c++) {
          _mask[r][c] &= values[c];
        }
        // lineExists in mg2d.  Perform boolean operation on 
      } catch (IllegalArgumentException e) {
        // line doesn't exist in mg2d.  Set corresponding mask values to false
        for (int c = 0; c < _mask[r].length; c++) {
          _mask[r][c] = false;
        }
      }
    }
  }
}
