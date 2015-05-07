/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.util;


import junit.framework.TestCase;


public class OctTreeTest extends TestCase {

  public void testBuild() {
    OctTree root = new OctTree(0, 10, 20, 30, 60, 70);
    OctTree leaf = new OctTree(5, 25, 65, 1);
    root.add(leaf);

    assertEquals(root.findParent(leaf), root);
    assertFalse(root.isLeaf());
    assertTrue(leaf.isLeaf());
    assertFalse(root.isGrandParent());
    assertFalse(leaf.isGrandParent());
  }

  public void testSplit() {
    OctTree root = new OctTree(0, 10, 20, 30, 60, 70);

    for (int i = 0; i < 9; i++) {
      OctTree leaf = new OctTree(i, 25, 65, 1);
      root.add(leaf);
    }

    OctTree leaf = new OctTree(8, 28, 62, 1);
    OctTree parent = root.findParent(leaf);
    parent.add(leaf);
    OctTree p2 = root.findParent(leaf);
    assertEquals(parent, p2);
  }

  public void testContains() {
    OctTree parent = new OctTree(0.625, 0.6875, 0.875, 0.9375, 0.0625, 0.125);
    assertFalse(parent.contains(parent));

    OctTree data = new OctTree(0.628, 0.879, 0.068, 1);
    assertTrue(parent.contains(data));

    OctTree origin = new OctTree(0, 0, 0, 1);
    assertFalse(parent.contains(origin));
  }

  public void testRandom() {

    int SIZE = 10000;

    double[] x = new double[SIZE];
    double[] y = new double[SIZE];
    double[] z = new double[SIZE];
    double[] a = new double[SIZE];

    for (int i = 0; i < SIZE; i++) {
      x[i] = Math.random();
      y[i] = Math.random();
      z[i] = Math.random();
      a[i] = i;
    }

    OctTree root = new OctTree(0, 1, 0, 1, 0, 1);

    for (int i = 0; i < SIZE; i++) {
      OctTree oct = new OctTree(x[i], y[i], z[i], a[i]);
      OctTree parent = root.findParent(oct);
      parent.add(oct);
    }

    System.out.println("Finished creating OctTree");

    for (int i = 0; i < SIZE; i++) {
      OctTree result = root.search(x[i], y[i], z[i]);
      assertEquals("Failed on " + i, a[i], result._data);
    }

  }

}
