/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.renderer.seismic;


import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.core.model.seismic.TraceSection;


public class ReadTracesResult {

  private final TraceSection _traceSection;

  private final Trace[] _traces;

  public ReadTracesResult(final TraceSection traceSection, final Trace[] traces) {
    _traceSection = traceSection;
    _traces = new Trace[traces.length];
    System.arraycopy(traces, 0, _traces, 0, traces.length);
  }

  public TraceSection getTraceSection() {
    return _traceSection;
  }

  public Trace[] getTraces() {
    Trace[] traces = new Trace[_traces.length];
    System.arraycopy(_traces, 0, traces, 0, traces.length);
    return traces;
  }
}
