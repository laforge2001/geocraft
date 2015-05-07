package org.geocraft.ui.chartviewer.data;


import org.geocraft.core.common.math.MathUtil;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.seismic.PostStack3d;


public class PolarChartData extends AbstractChartData {

  private final int _numCells;

  private final int _numAngleVals;

  private final int _numAngles;

  private final float[][] _xPnts;

  private final float[][] _yPnts;

  private final float[][] _attribute;

  private float _minimum;

  private float _maximum;

  /** The value representing a null */
  private float _nullValue;

  private boolean _ignoreNulls = false;

  private Grid3d _grid;

  private PostStack3d _volume;

  public PolarChartData(final String name, final float[][] xPnts, final float[][] yPnts, final float[][] attribute, final Grid3d grid, final PostStack3d volume) {
    super(name);
    _xPnts = xPnts;
    _yPnts = yPnts;
    _numAngleVals = attribute.length;
    _numAngles = attribute[0].length;
    _numCells = _numAngleVals * _numAngles;
    _minimum = Float.MAX_VALUE;
    _maximum = -Float.MAX_VALUE;
    _attribute = new float[_numAngleVals][_numAngles];

    // Set the null value
    _grid = grid;
    if (grid != null) {
      _nullValue = _grid.getNullValue();
    }
    _volume = volume;
    if (volume != null) {
      _nullValue = 0;
      _ignoreNulls = true;
    }

    for (int i1 = 0; i1 < _numAngles; i1++) {
      for (int i2 = 0; i2 < _numAngleVals; i2++) {
        _attribute[i2][i1] = attribute[i2][i1];
        if (!isNull(_attribute[i2][i1])) {
          _minimum = Math.min(_minimum, attribute[i2][i1]);
          _maximum = Math.max(_maximum, attribute[i2][i1]);
        }
      }
    }
  }

  public int getNumAngles() {
    return _numAngles;
  }

  public int getNumAngleVals() {
    return _numAngleVals;
  }

  public int getNumCells() {
    return _numCells;
  }

  public float[][] getXPnts() {
    return _xPnts;
  }

  public float[][] getYPnts() {
    return _yPnts;
  }

  public float getValueByCell(final int angleValIndx, final int angleIndx) {
    if (angleValIndx >= 0 && angleValIndx < _numAngleVals && angleIndx >= 0 && angleIndx < _numAngles) {
      return _attribute[angleValIndx][angleIndx];
    }
    return Float.NaN;
  }

  /**
   * Gets the 2D array of attribute values
   */
  public float[][] getValues() {
    return _attribute;
  }

  public float getAttributeMinimum() {
    return _minimum;
  }

  public float getAttributeMaximum() {
    return _maximum;
  }

  /*
   *  Gets null value associated with the data
   */
  public float getNullValue() {
    return _grid.getNullValue();
  }

  /*
   *  Gets the grid associated with data
   */
  public Grid3d getGrid() {
    return _grid;
  }

  /*
   *  Gets the volume associated with data
   */
  public PostStack3d getVolume() {
    return _volume;
  }

  /**
   * Returns a flag indicating if the value at the specified angleValIndx, angleVal in the grid is null.
   * 
   * @param row the row to check.
   * @param col the column to check.
   * @return <i>true</i> if the value at the specified angleValIndx,angleVal is null; otherwise <i>false</i>.
   */
  public boolean isNull(final int angleValIndx, final int angleVal) {
    // Don't check for a null if we are ignoring them
    if (_ignoreNulls) {
      return false;
    }
    return isNull(getValueByCell(angleValIndx, angleVal));
  }

  /**
   * Returns a flag indicating if a specified value matches the null value of the grid.
   * 
   * @param value the value to check.
   * @return <i>true</i> if the value matches the null value of the grid; otherwise <i>false</i>.
   */
  public boolean isNull(final float value) {
    // Don't check for a null if we are ignoring them
    if (_ignoreNulls) {
      return false;
    }
    return MathUtil.isEqual(value, _nullValue);
  }
}
