package org.geocraft.core.io;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.mapper.IPostStack3dMapper;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PostStack3d.StorageOrder;


public class PostStack3dLoadHelper {

  public static final int ALLOWED_NUMBER_OF_ATTEMPTS = 3;

  public static final int NUM_TEST_SAMPLES = 3;

  private static final StorageOrder DEFAULT_STORAGE_ORDER = StorageOrder.INLINE_XLINE_Z;

  private static int _tryNumber = 1;

  private static int numValidTraces(final TraceData testMe) {
    int numValidTraces = 0;
    for (int i = 0; i < testMe.getNumTraces(); ++i) {
      if (testMe.getTrace(i).getStatus() != Trace.Status.Missing) {
        ++numValidTraces;
      }
    }
    if (numValidTraces > 0) {
      return numValidTraces;
    }
    return 1;
  }

  private static StorageOrder findOrderRetry(final PostStack3d ps3d, final IPostStack3dMapper mapper) {
    _tryNumber++;
    // System.out.println("Retry #" + _tryNumber);
    return getStorageOrder(ps3d, mapper);
  }

  /**
   * The map parameter contains a sorted list of the nanosecond times. And if the last occurence of the first element of the list
   * equals the number of samples, then we know that the groups are mutually exclusive and we have our answer. If they are mixed,
   * then we return null. It is up to the calling function to contain logic to determine if this function needs to be called again
   * in the case "null" is returned.
   * 
   * @param map
   * @param numSamples
   * @return the storage order.
   */
  private static StorageOrder findOrder(final Map<Double, String> map, final int numSamples, final PostStack3d ps3d,
      final IPostStack3dMapper mapper, final IProgressMonitor monitor) {
    ArrayList<String> valuesList = new ArrayList<String>(map.values());
    if (valuesList.lastIndexOf(valuesList.get(0)) == numSamples - 1) {
      try {
        if (valuesList.get(0).equals("inline")) {
          resetTryValue();
          if (monitor != null) {
            monitor.done();
          }
          return StorageOrder.INLINE_XLINE_Z;
        }
        resetTryValue();
        if (monitor != null) {
          monitor.done();
        }
        return StorageOrder.XLINE_INLINE_Z;
      } finally {
        if (monitor != null) {
          monitor.done();
        }
      }
    }
    return findOrderRetry(ps3d, mapper);
  }

  private static double calculateInline(final PostStack3d ps3d, final float inlineValue, final IPostStack3dMapper mapper) {
    long startTime = System.nanoTime();
    TraceData inlineTraceData = mapper.getInline(ps3d, inlineValue, ps3d.getXlineStart(), ps3d.getXlineEnd(), ps3d
        .getZStart(), ps3d.getZEnd());
    long estimatedTime = System.nanoTime() - startTime;
    double time = estimatedTime / numValidTraces(inlineTraceData);
    return time;
  }

  private static double calculateXline(final PostStack3d ps3d, final float xlineValue, final IPostStack3dMapper mapper) {
    Long startTime2 = System.nanoTime();
    TraceData xlineTraceData = mapper.getXline(ps3d, xlineValue, ps3d.getInlineStart(), ps3d.getInlineEnd(), ps3d
        .getZStart(), ps3d.getZEnd());
    long estimatedTime2 = System.nanoTime() - startTime2;
    double time = estimatedTime2 / numValidTraces(xlineTraceData);
    return time;
  }

  private static void calculateInlineResult(final PostStack3d ps3d, final float inlineValue,
      final IPostStack3dMapper mapper, final TempResult result) {
    long startTime = System.nanoTime();
    TraceData inlineTraceData = mapper.getInline(ps3d, inlineValue, ps3d.getXlineStart(), ps3d.getXlineEnd(), ps3d
        .getZStart(), ps3d.getZEnd());
    long estimatedTime = System.nanoTime() - startTime;
    //double time = estimatedTime / numValidTraces(inlineTraceData);
    //System.out.println("CALC INLINE: " + inlineValue + " NANO_PER_TRACE " + time);
    result.time += estimatedTime;
    result.numTraces += numValidTraces(inlineTraceData);
  }

  private static void calculateXlineResult(final PostStack3d ps3d, final float xlineValue,
      final IPostStack3dMapper mapper, final TempResult result) {
    Long startTime = System.nanoTime();
    TraceData xlineTraceData = mapper.getXline(ps3d, xlineValue, ps3d.getInlineStart(), ps3d.getInlineEnd(), ps3d
        .getZStart(), ps3d.getZEnd());
    long estimatedTime = System.nanoTime() - startTime;
    //double time = estimatedTime2 / numValidTraces(xlineTraceData);
    //System.out.println("CALC XLINE: " + xlineValue + " NANO_PER_TRACE " + time);
    result.time += estimatedTime;
    result.numTraces += numValidTraces(xlineTraceData);
  }

  private static float getNextInlineValue(final PostStack3d ps3d, final Random random) {
    return ps3d.getInlineStart() + ps3d.getInlineDelta() * random.nextInt(ps3d.getNumInlines());
  }

  private static float getNextXlineValue(final PostStack3d ps3d, final Random random) {
    return ps3d.getXlineStart() + ps3d.getXlineDelta() * random.nextInt(ps3d.getNumXlines());
  }

  private static void setProgress(final IProgressMonitor monitor, final int work, final String message) {
    if (monitor != null) {
      monitor.subTask(message);
      monitor.worked(work);
    }
  }

  private static void resetTryValue() {
    _tryNumber = 1;
  }

  private static void setProgressDeterminate(final IProgressMonitor monitor) {
    if (monitor != null) {
      // TODO: progress.setIndeterminate(false);
    }
  }

  public static synchronized StorageOrder getStorageOrder(final PostStack3d ps3d, final IPostStack3dMapper mapper,
      final IProgressMonitor monitor) {
    if (_tryNumber < ALLOWED_NUMBER_OF_ATTEMPTS) {
      Random randomGenerator = new Random();
      Map<Double, String> map = Collections.synchronizedMap(new TreeMap<Double, String>());
      setProgress(monitor, 1, "Attempt " + _tryNumber + "/" + ALLOWED_NUMBER_OF_ATTEMPTS + ": Accessing Volume...");
      // a dummy xline read to open up the volume
      calculateXline(ps3d, getNextXlineValue(ps3d, randomGenerator), mapper);
      setProgressDeterminate(monitor);
      TempResult inlineResult = new TempResult();
      TempResult xlineResult = new TempResult();
      for (int i = 0; i < NUM_TEST_SAMPLES; ++i) {
        float inlineValue = getNextInlineValue(ps3d, randomGenerator);
        float xlineValue = getNextXlineValue(ps3d, randomGenerator);
        setProgress(monitor, 1, "Calculating Inline for " + ps3d.getDisplayName() + "...");
        calculateInlineResult(ps3d, inlineValue, mapper, inlineResult);
        //map.put(calculateInline(ps3d, inlineValue, mapper), "inline");
        setProgress(monitor, 1, "Calculating Xline for " + ps3d.getDisplayName() + "...");
        calculateXlineResult(ps3d, xlineValue, mapper, xlineResult);
        //map.put(calculateXline(ps3d, xlineValue, mapper), "xline");
        //System.out.println("inline=" + inlineResult.numTraces + " " + inlineResult.time + " xline="
        //    + xlineResult.numTraces + " " + xlineResult.time);
      }
      double inlineRate = inlineResult.numTraces * 1.0 / inlineResult.time;
      double xlineRate = xlineResult.numTraces * 1.0 / xlineResult.time;
      //System.out.println("INLINE: NUMTRACES=" + inlineResult.numTraces + " TIME=" + inlineResult.time + " RATE="
      //    + inlineRate * 1.0E9);
      //System.out.println("XLINE: NUMTRACES=" + xlineResult.numTraces + " TIME=" + xlineResult.time + " RATE="
      //    + xlineRate * 1.0E9);
      if (inlineRate > xlineRate) {
        return StorageOrder.INLINE_XLINE_Z;
      }
      return StorageOrder.XLINE_INLINE_Z;
      //return findOrder(map, NUM_TEST_SAMPLES, ps3d, mapper, monitor);
    }
    resetTryValue(); // reset the try number so other classes can run the static method
    return DEFAULT_STORAGE_ORDER;
  }

  /**
   * If the storageOrder data member is not set, then this function will calculate what the storage order is by taking
   * NUM_EXP_SAMPLES of random inlines and xlines in the PostStack3d. Then it calculates the number of traces read per nanosecond
   * for each. Whichever group has the lesser values will be returned as the storage order. If the storage order cannot be
   * determined, then we run the algorithm again.
   */
  public static StorageOrder getStorageOrder(final PostStack3d ps3d, final IPostStack3dMapper mapper) {
    return getStorageOrder(ps3d, mapper, null);
  }

  public static StorageOrder getDefaultStorageOrder() {
    return DEFAULT_STORAGE_ORDER;
  }

}
