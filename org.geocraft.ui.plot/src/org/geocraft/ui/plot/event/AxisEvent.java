/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.event;


import org.geocraft.ui.plot.axis.IAxis;
import org.geocraft.ui.plot.defs.PlotEventType;


/**
 * The event fired to listeners when an axis is updated.
 */
public class AxisEvent extends AbstractPlotEvent {

  /** The axis that was updated. */
  private final IAxis _axis;

  /**
   * The default constructor.
   * 
   * @param plotAxis the axis that was updated.
   */
  public AxisEvent(final IAxis axis) {
    super(PlotEventType.AXIS_UPDATED);
    _axis = axis;
  }

  /**
   * Gets the axis that was updated.
   * 
   * @return the axis that was updated.
   */
  public IAxis getAxis() {
    return _axis;
  }
}
