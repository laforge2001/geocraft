/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.datatypes;


import junit.framework.TestCase;


/**
 * Test case for the <code>FloatRange</code> class.
 */
public class FloatRangeTestCase extends TestCase {

  /** The starting value of the integer range. */
  private static final float START = -6.5f;

  /** The ending value of the integer range. */
  private static final float END = 7.1f;

  /** The delta value of the integer range. */
  private static final float DELTA = 3.4f;

  /** The integer range to use for testing. */
  private FloatRange _range;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    _range = new FloatRange(START, END, DELTA);
  }

  /**
   * Test all the methods on the <code>FloatRange</code> class.
   */
  public void testAll() {
    // Compare the start, end and delta values.
    assertEquals(START, _range.getStart());
    assertEquals(END, _range.getEnd());
    assertEquals(DELTA, _range.getDelta());

    // Compare the # of values in the range.
    assertEquals(5, _range.getNumSteps());

    // Compare each of the values in the range.
    for (int i = 0; i < _range.getNumSteps(); i++) {
      assertEquals(START + i * DELTA, _range.getValue(i));
    }
    for (int i = 0; i < _range.getNumSteps(); i++) {
      assertEquals(START + i * DELTA, _range.toArray()[i]);
    }

    // Check an index that is out of bounds.
    try {
      _range.getValue(-3);
      fail(); // Fail, because this out-of-bounds index should have thrown an exception.
    } catch (Exception ex) {
      // This exception is expected.
    }
    assertEquals("[" + START + "," + END + "," + DELTA + "]", _range.toString());

    // Compare an integer range that is different.
    assertFalse(_range.equals(new FloatRange(5f, 35f, 5f)));

    // Create a new range based on the test range and compare it.
    FloatRange range2 = new FloatRange(_range);
    assertTrue(_range.equals(range2));
  }
}
