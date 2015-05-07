/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.classbkg;


import org.geocraft.abavo.process.ITraceProcess;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.seismic.PostStack2dLine;


/**
 * This class is a process that can be added to an <code>ABavoBaseTask</code>
 * to perform output of a poststack2d volume. The poststack2d is passed
 * into the constructor and must be created beforehand using one of the
 * factory methods. Traces are then passed into the process and written
 * to the output volume. Afterwards, the output volume is closed during
 * the cleanup.
 */
public class OutputPostStack2dProcess implements ITraceProcess {

  /** The process name. */
  private final String _name;

  /** The poststack2d volume to write. */
  private final PostStack2dLine _outputVolume;

  /**
   * Creates the poststack2d output process with the specified name.
   * @param name the process name.
   * @param outputVolume the output poststack2d volume.
   */
  public OutputPostStack2dProcess(final String name, final PostStack2dLine outputVolume) {
    _name = name;
    _outputVolume = outputVolume;
  }

  public String getName() {
    return _name;
  }

  public void initialize() {
    // No action required.
  }

  public boolean isTraceGenerator() {
    return false;
  }

  public TraceData[] process(final TraceData[] traceDataIn) {
    // Loop thru the array of trace data objects.
    for (TraceData traceData : traceDataIn) {

      // Write the traces to the output poststack2d volume.
      _outputVolume.putTraces(traceData);
    }

    // Pass the input traces thru.
    return traceDataIn;
  }

  public void cleanup() {
    // Close the output volume.
    _outputVolume.close();
  }

}
