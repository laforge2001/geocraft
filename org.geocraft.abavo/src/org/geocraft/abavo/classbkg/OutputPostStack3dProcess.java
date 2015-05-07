/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.classbkg;


import org.geocraft.abavo.process.ITraceProcess;
import org.geocraft.core.model.datatypes.TraceData;
import org.geocraft.core.model.seismic.PostStack3d;


/**
 * This class is a process that can be added to an <code>ABavoBaseTask</code>
 * to perform output of a poststack3d volume. The poststack3d is passed
 * into the constructor and must be created beforehand using one of the
 * factory methods. Traces are then passed into the process and written
 * to the output volume. Afterwards, the output volume is closed during
 * the cleanup.
 */
public class OutputPostStack3dProcess implements ITraceProcess {

  /** The process name. */
  private final String _name;

  /** The poststack3d volume to write. */
  private final PostStack3d _outputVolume;

  /**
   * Creates the poststack3d output process with the specified name.
   * @param name the process name.
   * @param outputVolume the output poststack3d volume.
   */
  public OutputPostStack3dProcess(final String name, final PostStack3d outputVolume) {
    _name = name;
    _outputVolume = outputVolume;
  }

  public String getName() {
    return _name;
  }

  public void initialize() {
    // No action required.
  }

  public void cleanup() {
    // Close the output volume.
    _outputVolume.close();
  }

  public synchronized TraceData[] process(final TraceData[] traceDataIn) {
    // Loop thru the array of trace data objects.
    for (TraceData traceData : traceDataIn) {

      // Write the traces to the output poststack3d volume.
      _outputVolume.putTraces(traceData);
    }

    // Pass the input traces thru.
    return traceDataIn;
  }

}
