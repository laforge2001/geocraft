/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.horizon.clip;


import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.geocraft.core.common.math.Clip;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.geomath.algorithm.util.TestHelperUtil;


public class ClipTest extends TestCase {

  static final float NULL_VALUE = 1e30f;

  static final float[][] INPUT_DATA = new float[][] { { 0, 2, 4, 6, 8 }, { 10, 12, NULL_VALUE, 16, 18 },
      { 20, 22, 24, 26, 28 }, };

  /**
   * Tests clipping to limits.
   */
  public void testReplaceWithLimits() {
    // Create a new grid.
    GridGeometry3d geometry = new GridGeometry3d("Geometry", 0, 0, 1, 1, 3, 5, 0.0);
    Grid3d grid = new Grid3d("foo", geometry);

    // Add data to the property.
    grid.setValues(INPUT_DATA, NULL_VALUE, Unit.FOOT);

    // Run the algorithm.
    GridClip clip = new GridClip();
    float[][] results = clip.clipGridData(new NullProgressMonitor(), null, grid, null,
        Clip.ClipType.REPLACE_WITH_LIMITS, 10, 20, 0);

    // Check the results.
    float[][] expected = new float[][] { { 10, 10, 10, 10, 10 }, { 10, 12, NULL_VALUE, 16, 18 },
        { 20, 20, 20, 20, 20 }, };
    assertEquals(true, TestHelperUtil.arrayIsEqual(results, expected));
  }

  /**
   * Tests clipping to nulls.
   */
  public void testReplaceWithNulls() {
    // Create a new grid.
    GridGeometry3d geometry = new GridGeometry3d("Geometry", 0, 0, 1, 1, 3, 5, 0.0);
    Grid3d grid = new Grid3d("foo", geometry);

    // Add data to the property.
    grid.setValues(INPUT_DATA, NULL_VALUE, Unit.FOOT);

    // Run the algorithm.
    GridClip clip = new GridClip();
    float[][] results = clip.clipGridData(new NullProgressMonitor(), null, grid, null,
        Clip.ClipType.REPLACE_WITH_NULLS, 9, 20, 0);

    // Check the results.
    float[][] expected = new float[][] { { NULL_VALUE, NULL_VALUE, NULL_VALUE, NULL_VALUE, NULL_VALUE },
        { 10, 12, NULL_VALUE, 16, 18 }, { 20, NULL_VALUE, NULL_VALUE, NULL_VALUE, NULL_VALUE }, };
    assertEquals(true, TestHelperUtil.arrayIsEqual(results, expected));
  }

  /**
   * Tests clipping to constant.
   */
  public void testReplaceWithConstant() {
    // Create a new grid.
    GridGeometry3d geometry = new GridGeometry3d("Geometry", 0, 0, 1, 1, 3, 5, 0.0);
    Grid3d grid = new Grid3d("foo", geometry);

    // Add data to the property.
    grid.setValues(INPUT_DATA, NULL_VALUE, Unit.FOOT);

    // Run the algorithm.
    GridClip clip = new GridClip();
    float[][] results = clip.clipGridData(new NullProgressMonitor(), null, grid, null,
        Clip.ClipType.REPLACE_WITH_CONSTANT, 9, 20, 2000);

    // Check the results.
    float[][] expected = new float[][] { { 2000, 2000, 2000, 2000, 2000 }, { 10, 12, NULL_VALUE, 16, 18 },
        { 20, 2000, 2000, 2000, 2000 }, };
    assertEquals(true, TestHelperUtil.arrayIsEqual(results, expected));
  }

}
