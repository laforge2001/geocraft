/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.chartviewer.data;


import org.eclipse.swt.graphics.RGB;
import org.geocraft.core.common.math.MathUtil;


public class HistogramData extends AbstractChartData {

  private final float _nullValue;

  private final int _numCells;

  private float _xStart;

  private float _xEnd;

  private final int[] _histogram;

  private float _minimum;

  private float _maximum;

  private int _totalCount;

  private final RGB _rgb;

  public HistogramData(final String name, final float[] values, final float nullValue, final int numCells, final float xStart, final float xEnd, final RGB rgb) {
    super(name);
    _xStart = xStart;
    _xEnd = xEnd;
    _nullValue = nullValue;
    _numCells = numCells;
    if (MathUtil.isEqual(_xStart, _xEnd)) {
      _xStart -= 0.5f;
      _xEnd += 0.5f;
    }
    _minimum = Float.MAX_VALUE;
    _maximum = -Float.MAX_VALUE;
    _histogram = new int[_numCells];
    _totalCount = 0;
    for (float value : values) {
      _minimum = Math.min(_minimum, value);
      _maximum = Math.max(_maximum, value);
      if (!MathUtil.isEqual(value, _nullValue)) {
        int cell = getCell(value);
        if (cell >= 0 && cell < _numCells) {
          _histogram[cell]++;
          _totalCount++;
        }
      }
    }
    _rgb = rgb;
  }

  public int getNumCells() {
    return _numCells;
  }

  public float getStartX() {
    return _xStart;
  }

  public float getEndX() {
    return _xEnd;
  }

  private int getCell(final float x) {
    int cell = (int) (_numCells * (x - _xStart) / (_xEnd - _xStart));
    //cell = Math.max(0, cell);
    //cell = Math.min(_numCells - 1, cell);
    return cell;
  }

  public int getCount(final float x) {
    int cell = getCell(x);
    return getCountByCell(cell);
  }

  public int getCountByCell(final int cell) {
    if (cell >= 0 && cell < _numCells) {
      return _histogram[cell];
    }
    return 0;
  }

  public float getPercentage(final float x) {
    int cell = getCell(x);
    return getPercentageByCell(cell);
  }

  public float getPercentageByCell(final int cell) {
    if (cell >= 0 && cell < _numCells) {
      return 100f * _histogram[cell] / _totalCount;
    }
    return 0;
  }

  public float getAttributeMinimum() {
    return _minimum;
  }

  public float getAttributeMaximum() {
    return _maximum;
  }

  public RGB getRGB() {
    return _rgb;
  }

}
