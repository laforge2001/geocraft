package org.geocraft.io.util.volume;
import junit.framework.TestCase;

import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.io.util.volume.TraceBlock1d;
import org.geocraft.io.util.volume.TraceBlock2d;
import org.geocraft.io.util.volume.TraceBlock3d;


/**
 * Test case for the <code>TraceBlock</code> class.
 */
public class TraceBlockTestCase extends TestCase {

  /**
   * Unit test for the <code>TraceBlock1d</code> class.
   */
  public void testTraceBlock1d() {
    int numVolumes = 2;
    float zStart = 1000;
    float zDelta = 4;
    int numSamples = 101;
    Unit zUnit = Unit.MILLISECONDS;
    Trace[] traces = new Trace[numVolumes];
    for (int h = 0; h < numVolumes; h++) {
      double x = 0;
      double y = 0;
      float[] data = new float[numSamples];
      for (int k = 0; k < numSamples; k++) {
        data[k] = k;
      }
      traces[h] = new Trace(zStart, zDelta, zUnit, x, y, data);
    }
    TraceBlock1d traceBlock = new TraceBlock1d(numVolumes, traces);

    // Validate the trace block.
    assertEquals(numVolumes, traceBlock.getNumVolumes());
    for (int h = 0; h < numVolumes; h++) {
      Trace trace = traceBlock.getTrace(h);
      assertEquals(numSamples, trace.getNumSamples());
      float[] data = trace.getData();
      for (int k = 0; k < numSamples; k++) {
        assertEquals((float) (k), data[k]);
      }
    }
  }

  /**
   * Unit test for the <code>TraceBlock2d</code> class.
   */
  public void testTraceBlock2d() {
    int numVolumes = 2;
    int numTraces = 5;
    float zStart = 1000;
    float zDelta = 4;
    int numSamples = 101;
    Unit zUnit = Unit.MILLISECONDS;
    Trace[][] traces = new Trace[numVolumes][numTraces];
    for (int h = 0; h < numVolumes; h++) {
      for (int i = 0; i < numTraces; i++) {
        double x = i;
        double y = i;
        float[] data = new float[numSamples];
        for (int k = 0; k < numSamples; k++) {
          data[k] = i + k;
        }
        traces[h][i] = new Trace(zStart, zDelta, zUnit, x, y, data);
      }
    }
    TraceBlock2d traceBlock = new TraceBlock2d(numVolumes, numTraces, traces);

    // Validate the trace block.
    assertEquals(numVolumes, traceBlock.getNumVolumes());
    assertEquals(numTraces, traceBlock.getNumTraces());
    for (int h = 0; h < numVolumes; h++) {
      Trace[] traces2 = traceBlock.getTraces(h);
      for (int i = 0; i < numTraces; i++) {
        Trace trace = traces2[i];
        assertEquals(numSamples, trace.getNumSamples());
        float[] data = trace.getData();
        for (int k = 0; k < numSamples; k++) {
          assertEquals((float) (i + k), data[k]);
        }
      }
    }
  }

  /**
   * Unit test for the <code>TraceBlock3d</code> class.
   */
  public void testTraceBlock3d() {
    int numVolumes = 2;
    int numInlines = 3;
    int numXlines = 5;
    float zStart = 1000;
    float zDelta = 4;
    int numSamples = 101;
    Unit zUnit = Unit.MILLISECONDS;
    Trace[][][] traces = new Trace[numVolumes][numXlines][numXlines];
    for (int h = 0; h < numVolumes; h++) {
      for (int i = 0; i < numInlines; i++) {
        for (int j = 0; j < numXlines; j++) {
          double x = i;
          double y = j;
          float[] data = new float[numSamples];
          for (int k = 0; k < numSamples; k++) {
            data[k] = i + j + k;
          }
          traces[h][i][j] = new Trace(zStart, zDelta, zUnit, x, y, data);
        }
      }
    }
    TraceBlock3d traceBlock = new TraceBlock3d(numVolumes, numInlines, numXlines, traces);

    // Validate the trace block.
    assertEquals(numVolumes, traceBlock.getNumVolumes());
    assertEquals(numInlines, traceBlock.getNumInlines());
    assertEquals(numXlines, traceBlock.getNumXlines());
    for (int h = 0; h < numVolumes; h++) {
      Trace[][] traces2 = traceBlock.getTraces(h);
      for (int i = 0; i < numInlines; i++) {
        for (int j = 0; j < numXlines; j++) {
          Trace trace = traces2[i][j];
          assertEquals(numSamples, trace.getNumSamples());
          float[] data = trace.getData();
          for (int k = 0; k < numSamples; k++) {
            assertEquals((float) (i + j + k), data[k]);
          }
        }
      }
    }
  }
}
