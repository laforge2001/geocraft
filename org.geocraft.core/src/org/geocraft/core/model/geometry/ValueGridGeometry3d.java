package org.geocraft.core.model.geometry;


//
// Convention: 0 --> belongs to no region
//             1..n -_> belongs to indicated region
//
public class ValueGridGeometry3d {

  private final GridGeometry3d _geom;

  private final int[][] _valueArray;

  public ValueGridGeometry3d(final GridGeometry3d geom, final int initialValue) {
    _geom = geom;

    int nr = _geom.getNumRows();
    int nc = _geom.getNumColumns();
    _valueArray = new int[nr][nc];
    for (int r = 0; r < nr; r++) {
      for (int c = 0; c < nc; c++) {
        _valueArray[r][c] = initialValue;
      }
    }
  }

  public int getValue(final int r, final int c) {
    return _valueArray[r][c];
  }

  public int getValue(final int r, final int c, final int defaultValue) {
    if (r < 0 || c < 0 || r >= _valueArray.length || c >= _valueArray[0].length) {
      return defaultValue;
    }
    return _valueArray[r][c];
  }

  public void setValue(final int r, final int c, final int v) {
    _valueArray[r][c] = v;
  }

  public int getNumRows() {
    return _valueArray.length;
  }

  public int getNumColumns() {
    return _valueArray[0].length;
  }

  public int[][] getValueArray() {
    return _valueArray;
  }

  public GridGeometry3d getGridGeometry() {
    return _geom;
  }

}
