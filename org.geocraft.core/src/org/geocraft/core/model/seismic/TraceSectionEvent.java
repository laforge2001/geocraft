/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.model.seismic;


/**
 * The event sent by a section viewer when the <code>TRACE_SECTION_DISPLAYED</code> topic is
 * broadcast via the message bus. This event contains the new trace section displayed (so it
 * can be displayed by interested observers) along with the trace section previously displayed
 * (so it can be removed).
 */
public class TraceSectionEvent {

  /** The section currently displayed. */
  private TraceSection _newSection;

  /** The section previously displayed. */
  private TraceSection _oldSection;

  public TraceSectionEvent(TraceSection newSection, TraceSection oldSection) {
    _newSection = newSection;
    _oldSection = oldSection;
  }

  /**
   * Returns the trace section currently displayed.
   */
  public TraceSection getNewSection() {
    return _newSection;
  }

  /**
   * Returns the trace section previously displayed.
   */
  public TraceSection getOldSection() {
    return _oldSection;
  }
}
