/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.datatypes;

import junit.framework.TestCase;

/**
 * Test case for the <code>IntRange</code> class.
 */
public class IntRangeTestCase extends TestCase {

	/** The starting value of the integer range. */
	private static final int START = 6;

	/** The ending value of the integer range. */
	private static final int END = 27;

	/** The delta value of the integer range. */
	private static final int DELTA = 3;

	/** The integer range to use for testing. */
	private IntRange _range;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_range = new IntRange(START, END, DELTA);
	}

	/**
	 * Test all the methods on the <code>IntRange</code> class.
	 */
	public void testAll() {
		// Compare the start, end and delta values.
		assertEquals(START, _range.getStart());
		assertEquals(END, _range.getEnd());
		assertEquals(DELTA, _range.getDelta());

		// Compare the # of values in the range.
		assertEquals(8, _range.getNumSteps());

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
			fail(); // Fail, because this out-of-bounds index should have thrown
					// an exception.
		} catch (Exception ex) {
			// This exception is expected.
		}
		assertEquals("[" + START + "," + END + "," + DELTA + "]",
				_range.toString());

		// Compare an integer range that is different.
		assertFalse(_range.equals(new IntRange(5, 35, 5)));

		assertFalse(_range.contains(10));
		assertTrue(_range.contains(9));

		// Create a new range based on the test range and compare it.
		IntRange range2 = new IntRange(_range);
		assertTrue(_range.equals(range2));
	}
}
