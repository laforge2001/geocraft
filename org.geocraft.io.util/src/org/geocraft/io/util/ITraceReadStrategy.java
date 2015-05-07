/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.util;


import org.geocraft.core.model.datatypes.Trace;


/**
 * The interface for defining a strategy for reading traces from a volume
 * based on x,y coordinates. (Perhaps the x,y coordinates of traces from
 * another volume).
 */
public interface ITraceReadStrategy {

  /**
   * Returns the traces from a volume relative to the specified input traces.
   * The traces returned depend on the strategy: <i>NearestTrace</i> or
   * <i>InterpolatedTrace</i>.
   * @param tracesIn the input traces from which to obtain the x,y coordinates.
   * @return the traces, relative to the input traces.
   */
  Trace[] read(Trace[] tracesIn);
}
