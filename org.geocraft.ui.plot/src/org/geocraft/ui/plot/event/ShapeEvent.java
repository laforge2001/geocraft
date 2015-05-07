/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.event;


import org.geocraft.ui.plot.defs.PlotEventType;
import org.geocraft.ui.plot.object.IPlotShape;


/**
 * The plot point event class.
 * Passed to listeners when a plot shape is added, removed, selected, deselected, moved or updated.
 */
public class ShapeEvent extends AbstractPlotEvent {

  /** The plot shape. */
  protected IPlotShape _shape;

  /**
   * Constructs a plot shape event.
   * @param shape the plot shape.
   * @param type the event type.
   */
  public ShapeEvent(final IPlotShape shape, final PlotEventType type) {
    super(type);
    _shape = shape;
  }

  /**
   * Gets the plot shape.
   * @return the plot shape.
   */
  public IPlotShape getShape() {
    return _shape;
  }
}
