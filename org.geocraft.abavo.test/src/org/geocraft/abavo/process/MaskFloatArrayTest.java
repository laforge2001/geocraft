package org.geocraft.abavo.process;


import junit.framework.TestCase;

import org.geocraft.abavo.defs.ABavoDataMode;


/**
 * Unit tests for the MaskFloatArray class.
 */
public class MaskFloatArrayTest extends TestCase {

  /**
   * The main unit tests.
   */
  public void testMain() {
    float[] array = { 1, -3, 8, 7, -2, -1 };

    // Test the 'All' mask.
    MaskFloatArray.process(array, ABavoDataMode.ALL_DATA);
    assertEquals(1f, array[0]);
    assertEquals(-3f, array[1]);
    assertEquals(8f, array[2]);
    assertEquals(7f, array[3]);
    assertEquals(-2f, array[4]);
    assertEquals(-1f, array[5]);

    // Test the 'Peaks & Troughs' mask.
    array = new float[] { 1, -3, 8, 7, -2, -1 };
    MaskFloatArray.process(array, ABavoDataMode.PEAKS_AND_TROUGHS);
    assertTrue(Float.isNaN(array[0]));
    assertEquals(-3f, array[1]);
    assertEquals(8f, array[2]);
    assertTrue(Float.isNaN(array[3]));
    assertEquals(-2f, array[4]);
    assertTrue(Float.isNaN(array[5]));

    //    // Test the 'Peaks & Troughs - Block' mask.
    //    array = new float[] { 1, -3, 8, 7, -2, -1 };
    //    int[] iarray = MaskFloatArray.maskPeaksAndTroughsBlock(array);
    //    assertTrue(Float.isNaN(array[0]));
    //    assertEquals(-3f, array[1]);
    //    assertEquals(8f, array[2]);
    //    assertEquals(8f, array[3]);
    //    assertEquals(-2f, array[4]);
    //    assertEquals(-2f, array[5]);

    //  Test different array.
    array = new float[] { -1, 3, -7, -8, -1, -2 };
    MaskFloatArray.process(array, ABavoDataMode.PEAKS_AND_TROUGHS);
    assertTrue(Float.isNaN(array[0]));
    assertEquals(3f, array[1]);
    assertTrue(Float.isNaN(array[2]));
    assertEquals(-8f, array[3]);
    assertTrue(Float.isNaN(array[4]));
    assertTrue(Float.isNaN(array[5]));
  }

}
