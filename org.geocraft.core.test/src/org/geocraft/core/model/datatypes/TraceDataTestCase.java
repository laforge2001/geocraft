/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.datatypes;


import junit.framework.TestCase;


/**
 * Unit tests for the <code>TraceData</code> class.
 */
public class TraceDataTestCase extends TestCase {

  /**
   * Test the various constructors.
   */
  public void testConstructors() {
    // Test the first constructor.
    int numTraces = 2;
    int numSamples = 101;
    float zStart = 1000;
    float zDelta = 4;
    Unit zUnit = Unit.MILLISECONDS;
    float zEnd = zStart + (numSamples - 1) * zDelta;
    Trace[] traces = new Trace[numTraces];
    double x = 0;
    double y = 0;
    for (int i = 0; i < numTraces; i++) {
      float[] data = new float[numSamples];
      for (int k = 0; k < numSamples; k++) {
        data[k] = i * numSamples + k;
      }
      traces[i] = new Trace(zStart, zDelta, zUnit, x, y, data);
    }
    TraceData traceData1 = new TraceData(traces);

    assertEquals(zStart, traceData1.getStartZ());
    assertEquals(zEnd, traceData1.getEndZ());
    assertEquals(zUnit, traceData1.getUnitOfZ());
    assertEquals(numSamples, traceData1.getNumSamples());
    assertEquals(numTraces, traceData1.getNumTraces());

    int index = 0;
    for (int i = 0; i < numTraces; i++) {
      for (int k = 0; k < numSamples; k++) {
        float expectedValue = i * numSamples + k;
        assertEquals(expectedValue, traceData1.getData()[index]);
        index++;
      }
    }

    // Replace a trace with one containing a different # of samples.
    traces[1] = new Trace(zStart, zDelta, zUnit, x, y, new float[numSamples + 1]);
    try {
      new TraceData(traces);
      fail("Constructor failed to throw a validation exception.");
    } catch (Exception ex) {
      // Success: the constructor should have thrown an exception.
    }

    // Replace a trace with one containing a different z unit of measurement.
    traces[1] = new Trace(zStart, zDelta, Unit.METER, x, y, new float[numSamples]);
    try {
      new TraceData(traces);
      fail("Constructor failed to throw a validation exception.");
    } catch (Exception ex) {
      // Success: the constructor should have thrown an exception.
    }

    // Replace a trace with one containing a different delta z value.
    traces[1] = new Trace(zStart, zDelta + 1, zUnit, x, y, new float[numSamples]);
    try {
      new TraceData(traces);
      fail("Constructor failed to throw a validation exception.");
    } catch (Exception ex) {
      // Success: the constructor should have thrown an exception.
    }

    // Replace a trace with one containing a different starting z value.
    traces[1] = new Trace(zStart + 1, zDelta, zUnit, x, y, new float[numSamples]);
    try {
      new TraceData(traces);
      fail("Constructor failed to throw a validation exception.");
    } catch (Exception ex) {
      // Success: the constructor should have thrown an exception.
    }

    // Use an zero-length trace array.
    try {
      traces = new Trace[0];
      new TraceData(traces);
      fail("Constructor failed to throw a validation exception.");
    } catch (Exception ex) {
      // Success: the constructor should have thrown an exception.
    }
  }
}
