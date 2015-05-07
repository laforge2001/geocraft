/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.process;


import junit.framework.TestCase;

import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.datatypes.Trace.Status;


/**
 * Unit tests for the TraceAlignment class.
 * TODO: Need a test of the align method, but it is complex math.
 */
public class TraceAlignmentTest extends TestCase {

  /**
   * Test the shift method.
   */
  public void testShift() {
    int corrWindow = 80;
    int maxShift = 12;
    float corrThreshold = 0.5f;
    int filterLength = 40;
    float ampThreshold = 20;
    int ampThresholdWindow = 40;
    TraceAlignment process = new TraceAlignment(corrWindow, maxShift, corrThreshold, filterLength, ampThreshold, ampThresholdWindow);
    float[] in = { 1, 5, -4, -2, 3, 2 };
    double[] shift = { 0, 0.1, -1, -.1, 0.25, 0 };
    float[] shifted = process.shiftTrace(in, shift, 6);

    // The 1st sample should remain unchanged.
    assertEquals(0, Float.compare(in[0], shifted[0]));
    // The 2nd sample should be slightly less than 5.
    assertTrue(shifted[1] < in[1]);
    // The 3rd sample should be 5, since it was an integer sample shift.
    assertEquals(0, Float.compare(in[1], shifted[2]));
    // The 4th sample should be slightly less than -2.
    assertTrue(shifted[3] < in[3]);
    // The 5th sample should be slightly greater than 3.
    assertTrue(shifted[4] > in[4]);
    // The 65th sample should remain unchanged.
    assertEquals(0, Float.compare(in[5], shifted[5]));
  }

  /**
   * Test the shift method.
   */
  public void testProcess() {
    int corrWindow = 80;
    int maxShift = 12;
    float corrThreshold = 0.5f;
    int filterLength = 40;
    float ampThreshold = 20;
    int ampThresholdWindow = 40;
    TraceAlignment process = new TraceAlignment(corrWindow, maxShift, corrThreshold, filterLength, ampThreshold, ampThresholdWindow);
    float[] dataN = { 0, 0, 1, 0, 0, 1, 0 };
    float[] dataF = { 0, 1, 0, 0, 2, 0, 0 };
    Trace traceN = new Trace(0, 4, Unit.MILLISECONDS, 0, 0, dataN, Status.Live);
    Trace traceF = new Trace(0, 4, Unit.MILLISECONDS, 0, 0, dataF, Status.Live);
    int numSamples = dataN.length;
    Trace[] aligned = process.process(numSamples, traceN, 0, numSamples - 1, traceF, 0, numSamples - 1);

    // Simple test of trace alignment - using spikes.

    // Test that the near data is unchanged.
    float[] alignedNear = aligned[0].getData();
    for (int i = 0; i < numSamples; i++) {
      assertEquals(dataN[i], alignedNear[i]);
    }
    float[] alignedFar = aligned[1].getData();
    assertEquals(dataF[0], alignedFar[1]);
    assertEquals(dataF[1], alignedFar[2]);
    assertEquals(dataF[2], alignedFar[3]);
    assertEquals(dataF[3], alignedFar[4]);
    assertEquals(dataF[4], alignedFar[5]);
    assertEquals(dataF[5], alignedFar[6]);
  }

}
