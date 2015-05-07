/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.horizon.merge;


import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.geomath.algorithm.util.TestHelperUtil;


/**
 * Unit tests for the Merge algorithm.
 */
public class MergeTest extends TestCase {

  static final float NULL_VALUE = 1e30f;

  static final float[][] INPUT_DATA1 = new float[][] { { 0, 2, 4, NULL_VALUE, 8 }, { 10, 12, NULL_VALUE, 16, 18 },
      { 20, 22, 24, 26, 28 }, };

  static final float[][] INPUT_DATA2 = new float[][] { { 28, 26, 24, 22, 20 }, { 18, 16, NULL_VALUE, 12, 10 },
      { 8, 6, 4, NULL_VALUE, 0 }, };

  /**
   * Tests the MIN merge.
   */
  public void testOperationMIN() {
    // Run the algorithm.
    float[][] results = Merge.mergeGridData(new NullProgressMonitor(), null, getGrids(), Merge.Operation.MIN);

    // Check the results.
    float[][] expected = new float[][] { { 0, 2, 4, 22, 8 }, { 10, 12, NULL_VALUE, 12, 10 }, { 8, 6, 4, 26, 0 }, };
    assertEquals(true, TestHelperUtil.arrayIsEqual(results, expected));
  }

  /**
   * Tests the MAX merge.
   */
  public void testOperationMAX() {
    // Run the algorithm.
    float[][] results = Merge.mergeGridData(new NullProgressMonitor(), null, getGrids(), Merge.Operation.MAX);

    // Check the results.
    float[][] expected = new float[][] { { 28, 26, 24, 22, 20 }, { 18, 16, NULL_VALUE, 16, 18 },
        { 20, 22, 24, 26, 28 }, };
    assertEquals(true, TestHelperUtil.arrayIsEqual(results, expected));
  }

  /**
   * Tests the OR merge.
   */
  public void testOperationOR() {
    // Run the algorithm.
    float[][] results = Merge.mergeGridData(new NullProgressMonitor(), null, getGrids(), Merge.Operation.OR);

    // Check the results.
    float[][] expected = new float[][] { { 0, 2, 4, 22, 8 }, { 10, 12, NULL_VALUE, 16, 18 }, { 20, 22, 24, 26, 28 }, };
    assertEquals(true, TestHelperUtil.arrayIsEqual(results, expected));
  }

  /**
   * Returns 2 input grids to use for unit testing.
   */
  private Grid3d[] getGrids() {
    // Create a new grid.
    GridGeometry3d geometry = new GridGeometry3d("Geometry", 0, 0, 1, 1, 3, 5, 0.0);
    Grid3d grid1 = new Grid3d("foo", geometry);
    Grid3d grid2 = new Grid3d("bar", geometry);

    // Add data to the property.
    grid1.setValues(INPUT_DATA1, NULL_VALUE, Unit.FOOT);
    grid2.setValues(INPUT_DATA2, NULL_VALUE, Unit.FOOT);

    return new Grid3d[] { grid1, grid2 };
  }

}
