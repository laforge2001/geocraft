/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.process;


import org.geocraft.core.model.datatypes.TraceData;


/**
 * The interface for a trace process.
 */
public interface ITraceProcess {

  /** Enumeration for process status. */
  static enum Status {
    Idle,
    Running,
    Cancelled,
    Completed
  }

  /**
   * Gets the process name.
   * @return the process name.
   */
  String getName();

  /**
   * Runs the process initialization.
   */
  void initialize();

  /**
   * Runs the process algorithm.
   * @param traceData the input trace data object.
   * @return the output trace data object.
   */
  TraceData[] process(TraceData[] traceData);

  /**
   * Runs the process cleanup.
   */
  void cleanup();

}
