/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.geomath.algorithm.volume.mvxp;


import org.eclipse.core.runtime.IProgressMonitor;
import org.geocraft.core.common.math.MVXP;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.io.util.TraceConsumer;
import org.geocraft.io.util.TraceProducer;


public class MVXPWorker extends TraceConsumer {

  /** The output 3D volume. */
  private PostStack3d _outputVolume;

  /** The starting z index. */
  private int _zStartIndex;

  /** The ending z index. */
  private int _zEndIndex;

  /** The window length (in samples). */
  private int _windowLength;

  /** The scaling factor. */
  private float _scaleFactor;

  /** The clipping factor. */
  private float _clipFactor;

  /** The progress monitor (shared across workers). */
  private IProgressMonitor _monitor;

  public MVXPWorker(final TraceProducer producer, final PostStack3d outputVolume, final int zStartIndex, final int zEndIndex, final int windowLength, final float scaleFactor, final float clipFactor, final IProgressMonitor monitor) {
    super(producer);
    _outputVolume = outputVolume;
    _zStartIndex = zStartIndex;
    _zEndIndex = zEndIndex;
    _windowLength = windowLength;
    _scaleFactor = scaleFactor;
    _clipFactor = clipFactor;
    _monitor = monitor;
  }

  @Override
  public void processTraces(Trace[] tracesIn) {

    // Allocate an array of output traces.
    Trace[] tracesOut = new Trace[tracesIn.length];

    // Loop thru the traces in the trace collection obtained from the iterator.
    for (int i = 0; i < tracesIn.length; i++) {
      if (tracesIn[i].isLive()) {
        // If the trace is live, run mvxp over the trace
        float[] tvals = MVXP.mvxpTrace(tracesIn[i], _zStartIndex, _zEndIndex, _windowLength, _scaleFactor, _clipFactor);
        tracesOut[i] = new Trace(tracesIn[i], tvals);
      } else {
        // Otherwise, simply pass it along.
        tracesOut[i] = tracesIn[i];
      }
      // Update the progress monitor.
      _monitor.worked(1);
      if (_monitor.isCanceled()) {
        break;
      }
    }

    // Create a new trace collection and put it into the output volume.
    _outputVolume.putTraces(new TraceData(tracesOut));

    // Update the progress monitor message.
    _monitor.subTask(getMessage());
  }

}
