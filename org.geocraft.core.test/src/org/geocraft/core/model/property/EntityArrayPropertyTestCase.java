/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.property;


import junit.framework.TestCase;

import org.geocraft.core.model.geometry.GridGeometry3d;
import org.geocraft.core.model.grid.Grid3d;


/**
 * Unit tests for the <code>EntityArrayProperty</code> class.
 */
public class EntityArrayPropertyTestCase extends TestCase {

  /** The entity array property to use for testing. */
  private EntityArrayProperty<Grid3d> _entityArrayProperty;

  /** The array of grid entities. */
  private Grid3d[] _gridArray;

  /** The individual grid entities. */
  private Grid3d _grid1, _grid2, _grid3;

  /** The name of the 1st grid entity. */
  private static final String GRID_NAME1 = "alpha";

  /** The name of the 2nd grid entity. */
  private static final String GRID_NAME2 = "beta";

  /** The name of the 3rd grid entity. */
  private static final String GRID_NAME3 = "gamma";

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    _entityArrayProperty = new EntityArrayProperty<Grid3d>("test key", Grid3d.class);

    GridGeometry3d geometry = new GridGeometry3d("geometry", 0, 0, 10, 20, 20, 10, 45);

    _grid1 = new Grid3d(GRID_NAME1, geometry);
    _grid2 = new Grid3d(GRID_NAME2, geometry);
    _grid3 = new Grid3d(GRID_NAME3, geometry);

    _gridArray = new Grid3d[] { _grid1, _grid2, _grid3 };
  }

  /**
   * Test for the <code>getValueObject</code> method.
   */
  public void testGetValueObject() {
    _entityArrayProperty.set(_gridArray);
    Grid3d[] values = (Grid3d[]) _entityArrayProperty.getValueObject();
    assertEquals(values[2], _gridArray[2]);
  }

  /**
   * Test for the the <code>pickle()</code> method.
   */
  public void testPickle() {
    _entityArrayProperty.set(_gridArray);
    String pickleString = "";
    for (int i = 0; i < _gridArray.length; i++) {
      Grid3d grid = _gridArray[i];
      pickleString += grid.getUniqueID();
      if (i < _gridArray.length - 1) {
        pickleString += ",";
      }
    }
    assertEquals(pickleString, _entityArrayProperty.pickle());
  }

  /**
   * Test for the <code>get()</code> method.
   */
  public void testGet() {
    _entityArrayProperty.set(_gridArray);
    Grid3d[] values = _entityArrayProperty.get();
    assertEquals(values[2], _gridArray[2]);
  }

  /**
   * Test for the <code>isEmpty()</code> method.
   */
  public void testIsEmpty() {
    assertTrue(_entityArrayProperty.isEmpty());
    _entityArrayProperty.set(_gridArray);
    assertFalse(_entityArrayProperty.isEmpty());
  }

  /**
   * Test for the <code>getKlass()</code> method.
   */
  public void testGetKlazz() {
    assertEquals(_entityArrayProperty.getKlazz(), Grid3d.class);
  }

}
