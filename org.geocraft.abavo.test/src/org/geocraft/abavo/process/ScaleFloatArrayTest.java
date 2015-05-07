package org.geocraft.abavo.process;


import junit.framework.TestCase;

import org.geocraft.abavo.process.ScaleFloatArray;


/**
 * Unit tests for the ScaleFloatArray class.
 */
public class ScaleFloatArrayTest extends TestCase {

  /**
   * The main unit tests.
   */
  public void testMain() {
    float DELTA = 0.0000001f;

    // Test getting the scalar.
    float scalar = 2.5f;
    ScaleFloatArray scale = new ScaleFloatArray(scalar);
    assertEquals(scalar, scale.getScalar(), DELTA);

    // Test scaling the whole array.
    float[] array1 = { 1, -4, 2000000, 2.5f };
    scale.process(array1);
    assertEquals(2.5f, array1[0], DELTA);
    assertEquals(-10f, array1[1], DELTA);
    assertEquals(5000000f, array1[2], DELTA);
    assertEquals(6.25f, array1[3], DELTA);

    // Test scalaing part of the array.
    float[] array2 = { 1, -4, 2000000, 2.5f };
    scale.process(array2, 1, 2);
    assertEquals(1f, array2[0], DELTA);
    assertEquals(-10f, array2[1], DELTA);
    assertEquals(5000000f, array2[2], DELTA);
    assertEquals(2.5f, array2[3], DELTA);
  }
}
