/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.ui.common.util.algorithm;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.core.common.math.MathUtil;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.seismic.PostStack3d.StorageOrder;
import org.geocraft.core.service.ServiceProvider;


/**
 * Helper algorithm methods.
 */
public class AlgorithmUtil {

  /** A default range. */
  public static final float[] DEFAULT_RANGE = new float[] { Float.MAX_VALUE, -1 * Float.MAX_VALUE };

  /**
   * Build an array with all the not null values in a volume entity. The number of traces to be analyzed needs to be provided. The traces are read in the fastest read storage order.
   * 
   * @param entity the volume entity
   * @param nrTraces the number of traces
   * @param progress a progress object
   * @param fromCenter if the data should be read from the center of the volume
   * @param inline the inline trace index to be read, when fromCenter is false
   * @param xline the xline trace index to be read, when fromCenter is false
   * @return the computed array
   */
  public static double[] computeFastestTraceVolumeValues(final PostStack3d entity, final int nrTraces,
      final IProgressMonitor progress, final boolean fromCenter, final float inline, final float xline) {
    StorageOrder order = entity.getPreferredOrder();
    TraceData trace = null;
    float[][] traces = new float[nrTraces][];

    progress.beginTask("Compute volume values", nrTraces * 5);
    for (int i = 0; i < nrTraces; i++) {
      progress.subTask("Processing trace " + (i + 1));
      try {
        if (fromCenter) {
          trace = getCenterTrace(entity, nrTraces, order, i);
        } else {
          if (order == StorageOrder.INLINE_XLINE_Z) {
            trace = entity.getInline(inline, entity.getXlineStart(), entity.getXlineEnd(), entity.getZStart(), entity
                .getZEnd());
          } else if (order == StorageOrder.XLINE_INLINE_Z) {
            trace = entity.getXline(xline, entity.getInlineStart(), entity.getInlineEnd(), entity.getZStart(), entity
                .getZEnd());
          } else {
            assert false;
          }
        }
      } catch (Exception ex) {
        ServiceProvider.getLoggingService().getLogger(AlgorithmUtil.class).warn(ex.getMessage(), ex);
      }

      progress.worked(3);
      if (trace == null) {
        return new double[0];
      }
      float[] data = trace.getData();
      List<Float> newData = new ArrayList<Float>();
      // eliminate the null values
      for (float element : data) {
        if (element != PostStack3d.NULL_VALUE) {
          newData.add(element);
        }
      }
      progress.worked(1);

      float[] values = new float[newData.size()];
      for (int k = 0; k < values.length; k++) {
        values[k] = newData.get(k).floatValue();
      }
      traces[i] = values;
      newData.clear();
      progress.worked(1);
    }

    double[] v = MathUtil.convert2D(traces);
    return v;
  }

  /**
   * Calculates the range values for a given trace. 
   * @param trace the trace values
   * @return an array having as the values the minimum and the maximum 
   */
  public static float[] getTraceRange(final float[] traceValues) {
    if (traceValues == null) {
      return DEFAULT_RANGE;
    }
    float nullValue = PostStack3d.NULL_VALUE;
    float min = Float.MAX_VALUE;
    float max = -1 * Float.MAX_VALUE;
    for (float value : traceValues) {
      if (value != nullValue && !Float.isNaN(value)) {
        min = Math.min(min, value);
        max = Math.max(max, value);
      }
    }
    return new float[] { min, max };
  }

  /**
   * Return the trace located in the center of the volume
   * 
   * @param entity the volume
   * @param nrTraces the total number of traces that will be returned
   * @param order the storage order
   * @param i the current trace number
   * @return the trace
   */
  private static TraceData getCenterTrace(final PostStack3d entity, final int nrTraces, final StorageOrder order,
      final int i) {
    TraceData trace = null;
    if (order == StorageOrder.INLINE_XLINE_Z) {
      // compute the center
      int index = i - nrTraces / 2 + entity.getNumInlines() / 2;
      // compute the trace index to be read
      float pos = entity.getInlineStart() + index * entity.getInlineDelta();
      trace = entity.getInline(pos, entity.getXlineStart(), entity.getXlineEnd(), entity.getZStart(), entity.getZEnd());
    } else if (order == StorageOrder.XLINE_INLINE_Z) {
      // compute the center
      int index = i - nrTraces / 2 + entity.getNumXlines() / 2;
      float pos = entity.getXlineStart() + index * entity.getXlineDelta();
      // compute the trace index to be read
      trace = entity
          .getXline(pos, entity.getInlineStart(), entity.getInlineEnd(), entity.getZStart(), entity.getZEnd());
    } else {
      assert false;
    }
    return trace;
  }
}
