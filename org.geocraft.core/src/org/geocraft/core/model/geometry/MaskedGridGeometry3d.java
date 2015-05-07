package org.geocraft.core.model.geometry;


import org.geocraft.core.model.grid.Grid3d;


public class MaskedGridGeometry3d {

  private final GridGeometry3d _geom;

  private final boolean[][] _mask;

  public MaskedGridGeometry3d(final GridGeometry3d geom) {
    _geom = geom;

    int nr = _geom.getNumRows();
    int nc = _geom.getNumColumns();
    _mask = new boolean[nr][nc];
    for (int r = 0; r < nr; r++) {
      for (int c = 0; c < nc; c++) {
        _mask[r][c] = true;
      }
    }
  }

  public MaskedGridGeometry3d(final Grid3d grid3d) {

    _geom = grid3d.getGeometry();

    float[][] roValues = grid3d.getReadOnlyValues();

    int nr = _geom.getNumRows();
    int nc = _geom.getNumColumns();
    float nv = grid3d.getNullValue();

    _mask = new boolean[nr][nc];

    for (int r = 0; r < nr; r++) {
      for (int c = 0; c < nc; c++) {
        _mask[r][c] = roValues[r][c] != nv;
      }
    }
  }

  public int getNumRows() {
    return _mask.length;
  }

  public int getNumColumns() {
    return _mask[0].length;
  }

  public boolean[][] getMask() {
    return _mask;
  }

  public void unaryAnd(final MaskedGridGeometry3d other) {
    // TODO - make this ok - check for overlapping geometry??
    if (_mask.length != other._mask.length || _mask[0].length != other._mask[0].length) {
      throw new IllegalArgumentException("Masks are of unequal size and cannot be anded together");
    }

    for (int r = 0; r < _mask.length; r++) {
      for (int c = 0; c < _mask[0].length; c++) {
        _mask[r][c] &= other._mask[r][c];
      }
    }
  }

  public GridGeometry3d getGridGeometry() {
    return _geom;
  }
}
