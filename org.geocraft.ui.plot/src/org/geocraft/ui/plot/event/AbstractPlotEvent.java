/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.event;


import org.geocraft.ui.plot.defs.PlotEventType;


/**
 * The abstract base class for plot events.
 */
public abstract class AbstractPlotEvent {

  /** The event type. */
  private final PlotEventType _eventType;

  /**
   * The default constructor.
   * 
   * @param eventType
   *            the event type.
   */
  public AbstractPlotEvent(final PlotEventType eventType) {
    _eventType = eventType;
  }

  /**
   * Gets the event type.
   * 
   * @return the event type.
   */
  public PlotEventType getEventType() {
    return _eventType;
  }
}
