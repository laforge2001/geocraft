/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.io;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.geocraft.core.model.grid.Grid2d;
import org.geocraft.core.model.mapper.IGrid2dMapper;
import org.geocraft.core.model.mapper.InMemoryMapper;


public class Grid2dInMemoryMapper extends InMemoryMapper<Grid2d> implements IGrid2dMapper {

  private final Map<Integer, float[]> _values;

  public Grid2dInMemoryMapper() {
    super(Grid2d.class);
    _values = Collections.synchronizedMap(new HashMap<Integer, float[]>());
  }

  public float[] getValues(final Grid2d grid, final int lineNumber) {
    int numBins = grid.getGridGeometry().getLineByNumber(lineNumber).getNumBins();
    float[] values = new float[numBins];
    for (int i = 0; i < numBins; i++) {
      values[i] = grid.getNullValue();
    }
    if (_values.containsKey(lineNumber)) {
      float[] copy = _values.get(lineNumber);
      System.arraycopy(copy, 0, values, 0, numBins);
    }
    return values;
  }

  public void putValues(final Grid2d grid, final int lineNumber, final float[] values) {
    int numBins = grid.getGridGeometry().getLineByNumber(lineNumber).getNumBins();
    float[] copy = new float[numBins];
    System.arraycopy(values, 0, copy, 0, numBins);
    _values.put(lineNumber, copy);
  }

}
