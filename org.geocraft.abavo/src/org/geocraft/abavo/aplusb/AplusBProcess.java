/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.abavo.aplusb;


import org.geocraft.abavo.input.AbstractInputProcess;
import org.geocraft.abavo.process.ITraceProcess;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.datatypes.TraceData;


public class AplusBProcess implements ITraceProcess {

  private final float _scalar;

  private final float _offset;

  public AplusBProcess(final float scalar, final float offset) {
    _scalar = scalar;
    _offset = offset;
  }

  public AplusBProcess(final AplusBProcess aplusbProcess) {
    this(aplusbProcess._scalar, aplusbProcess._offset);
  }

  @Override
  public String getName() {
    return "Scale and Offset";
  }

  @Override
  public void initialize() {
    // No initialization required.
  }

  @Override
  public void cleanup() {
    // No cleanup required.
  }

  @Override
  public TraceData[] process(final TraceData[] traceDataIn) {
    // If the trace data array size is zero, simple return.
    if (traceDataIn.length <= 0) {
      return traceDataIn;
    }

    // If the trace data array size is not exactly 2, throw an exception.
    if (traceDataIn.length != 2) {
      throw new RuntimeException("Number of input trace data objects must be 2!");
    }

    // The 1st trace data is A.
    TraceData traceDataA = traceDataIn[AbstractInputProcess.PRE_PROCESSES_A_TRACE];

    // The 2nd trace data is B.
    TraceData traceDataB = traceDataIn[AbstractInputProcess.PRE_PROCESSES_B_TRACE];

    // Check that the number of traces match.
    if (traceDataA.getNumTraces() != traceDataB.getNumTraces()) {
      throw new RuntimeException("Number of input traces do not match!");
    }

    // Allocate an output array of traces.
    Trace[] tracesC = new Trace[traceDataA.getNumTraces()];
    for (int i = 0; i < traceDataA.getNumTraces(); i++) {
      Trace traceA = traceDataA.getTrace(i);
      Trace traceB = traceDataB.getTrace(i);
      float[] a = traceA.getDataReference();
      float[] b = traceB.getDataReference();
      float[] c = new float[a.length];
      // Loop thru each sample.
      for (int j = 0; j < traceA.getNumSamples(); j++) {
        // Get the classification value of each sample.
        c[j] = a[j] + b[j] * _scalar + _offset;
      }
      // Populate the array of output traces.
      tracesC[i] = new Trace(traceA, c);
    }

    // Return the output trace data.
    TraceData[] traceDataOut = new TraceData[1];
    traceDataOut[0] = new TraceData(tracesC);
    return traceDataOut;
  }
}
