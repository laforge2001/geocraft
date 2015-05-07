package org.geocraft.ui.chartviewer.data;


public class GridImageData extends AbstractChartData {

  private final int _numCellsX;

  private final int _numCellsY;

  private final float _xStart;

  private final float _xEnd;

  private final float _yStart;

  private final float _yEnd;

  private final float[][] _attribute;

  private float _minimum;

  private float _maximum;

  public GridImageData(final String name, final float xStart, final float xEnd, final float yStart, final float yEnd, final float[][] attribute) {
    super(name);
    _xStart = xStart;
    _xEnd = xEnd;
    _numCellsX = attribute.length;
    _yStart = yStart;
    _yEnd = yEnd;
    _numCellsY = attribute[0].length;
    _minimum = Float.MAX_VALUE;
    _maximum = -Float.MAX_VALUE;
    _attribute = new float[_numCellsX][_numCellsY];
    for (int i = 0; i < _numCellsX; i++) {
      System.arraycopy(attribute[i], 0, _attribute[i], 0, _numCellsY);
      for (int j = 0; j < _numCellsY; j++) {
        _minimum = Math.min(_minimum, attribute[i][j]);
        _maximum = Math.max(_maximum, attribute[i][j]);
      }
    }
  }

  public int getNumCellsX() {
    return _numCellsX;
  }

  public int getNumCellsY() {
    return _numCellsY;
  }

  public float getStartX() {
    return _xStart;
  }

  public float getEndX() {
    return _xEnd;
  }

  public float getStartY() {
    return _yStart;
  }

  public float getEndY() {
    return _yEnd;
  }

  public float getValue(final float x, final float y) {
    int xCell = (int) (_numCellsX * (x - _xStart) / (_xEnd - _xStart));
    int yCell = (int) (_numCellsY * (y - _yStart) / (_yEnd - _yStart));
    return getValueByCell(xCell, yCell);
  }

  public float getValueByCell(final int xCell, final int yCell) {
    if (xCell >= 0 && xCell < _numCellsX && yCell >= 0 && yCell < _numCellsY) {
      return _attribute[xCell][yCell];
    }
    return Float.NaN;
  }

  public float getAttributeMinimum() {
    return _minimum;
  }

  public float getAttributeMaximum() {
    return _maximum;
  }
}
